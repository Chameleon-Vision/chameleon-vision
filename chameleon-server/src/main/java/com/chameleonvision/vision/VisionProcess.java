package com.chameleonvision.vision;

import com.chameleonvision.Debug;
import com.chameleonvision.config.CameraCalibrationConfig;
import com.chameleonvision.config.CameraConfig;
import com.chameleonvision.config.ConfigManager;
import com.chameleonvision.config.FullCameraConfiguration;
import com.chameleonvision.util.LoopingRunnable;
import com.chameleonvision.util.MathHandler;
import com.chameleonvision.vision.camera.CameraStreamer;
import com.chameleonvision.vision.camera.USBCameraCapture;
import com.chameleonvision.vision.pipeline.*;
import com.chameleonvision.vision.pipeline.impl.CVPipeline2d;
import com.chameleonvision.vision.pipeline.impl.CVPipeline3d;
import com.chameleonvision.vision.pipeline.impl.DriverVisionPipeline;
import com.chameleonvision.web.SocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.cscore.VideoMode;
import edu.wpi.first.networktables.*;
import edu.wpi.first.wpiutil.CircularBuffer;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public class VisionProcess {

    private final USBCameraCapture cameraCapture;
    private final CameraStreamerRunnable streamRunnable;
    private final VisionProcessRunnable visionRunnable;
    private final CameraConfig fileConfig;
    public final CameraStreamer cameraStreamer;
    public PipelineManager pipelineManager;

    private volatile CVPipelineResult lastPipelineResult;

    private BlockingQueue<Mat> streamFrameQueue = new LinkedBlockingDeque<>(1);

    // network table stuff
    private final NetworkTable defaultTable;
    private NetworkTableEntry ntPipelineEntry;
    public NetworkTableEntry ntDriverModeEntry;
    private int ntDriveModeListenerID;
    private int ntPipelineListenerID;
    private NetworkTableEntry ntYawEntry;
    private NetworkTableEntry ntPitchEntry;
    private NetworkTableEntry ntAuxListEntry;
    private NetworkTableEntry ntAreaEntry;
    private NetworkTableEntry ntLatencyEntry;
    private NetworkTableEntry ntValidEntry;
    private ObjectMapper objectMapper = new ObjectMapper();

    private long lastUIUpdateMs = 0;

    private final LinkedHashMap<Size, CameraCalibrationConfig> cameraCalibrationConfigs = new LinkedHashMap<>();

    VisionProcess(USBCameraCapture cameraCapture, FullCameraConfiguration config) {
        this.cameraCapture = cameraCapture;

        for (CameraCalibrationConfig calibration : config.calibration) {
            cameraCalibrationConfigs.put(calibration.resolution, calibration);
        }

        fileConfig = config.fileConfig;

        pipelineManager = new PipelineManager(this, config.pipelines);

        // Thread to put frames on the dashboard
        this.cameraStreamer = new CameraStreamer(cameraCapture, config.cameraConfig.name, pipelineManager.getCurrentPipeline().settings.streamDivisor);
        this.streamRunnable = new CameraStreamerRunnable(30, cameraStreamer);

        // Thread to process vision data
        this.visionRunnable = new VisionProcessRunnable();

        // network table
        defaultTable = NetworkTableInstance.getDefault().getTable("/chameleon-vision/" + cameraCapture.getProperties().getNickname());
    }

    public void start() {
        System.out.println("Starting NetworkTables.");
        initNT(defaultTable);

        System.out.println("Starting vision thread.");
        var visionThread = new Thread(visionRunnable);
        visionThread.setName(getCamera().getProperties().name + " - Vision Thread");
        visionThread.start();

        System.out.println("Starting stream thread.");
        var streamThread = new Thread(streamRunnable);
        streamThread.setName(getCamera().getProperties().name + " - Stream Thread");
        streamThread.start();
    }

    /**
     * Removes the old value change listeners
     * calls {@link #initNT}
     *
     * @param newTable passed to {@link #initNT}
     */
    public void resetNT(NetworkTable newTable) {
        ntDriverModeEntry.removeListener(ntDriveModeListenerID);
        ntPipelineEntry.removeListener(ntPipelineListenerID);
        initNT(newTable);
    }

    public void setCameraNickname(String newName) {
        getCamera().getProperties().setNickname(newName);
        var newTable = NetworkTableInstance.getDefault().getTable("/chameleon-vision/" + newName);
        resetNT(newTable);
    }

    private void initNT(NetworkTable newTable) {
        ntPipelineEntry = newTable.getEntry("pipeline");
        ntDriverModeEntry = newTable.getEntry("driver_mode");
        ntPitchEntry = newTable.getEntry("pitch");
        ntYawEntry = newTable.getEntry("yaw");
        ntAreaEntry = newTable.getEntry("area");
        ntLatencyEntry = newTable.getEntry("latency");
        ntValidEntry = newTable.getEntry("is_valid");
        ntAuxListEntry = newTable.getEntry("aux_targets");
        ntDriveModeListenerID = ntDriverModeEntry.addListener(this::setDriverMode, EntryListenerFlags.kUpdate);
        ntPipelineListenerID = ntPipelineEntry.addListener(this::setPipeline, EntryListenerFlags.kUpdate);
        ntDriverModeEntry.setBoolean(false);
        ntPipelineEntry.setNumber(pipelineManager.getCurrentPipelineIndex());
        pipelineManager.ntIndexEntry = ntPipelineEntry;
    }

    private void setDriverMode(EntryNotification driverModeEntryNotification) {
        setDriverMode(driverModeEntryNotification.value.getBoolean());
    }

    public void setDriverMode(boolean driverMode) {
        pipelineManager.setDriverMode(driverMode);
        SocketHandler.sendFullSettings();
    }

    /**
     * Method called by the nt entry listener to update the next pipeline.
     * @param notification the notification
     */
    private void setPipeline(EntryNotification notification) {
        var wantedPipelineIndex = (int) notification.value.getDouble();
        pipelineManager.setCurrentPipeline(wantedPipelineIndex);
    }

    public void setDriverModeEntry(boolean isDriverMode) {

        // if it's null, we haven't even started the program yet, so just return
        // otherwise, set it.
        if(ntDriverModeEntry != null) {
            ntDriverModeEntry.setBoolean(isDriverMode);
        }
    }

    private void updateUI(CVPipelineResult data) {
        // 30 "FPS" update rate
        long currentMillis = System.currentTimeMillis();
        if (currentMillis - lastUIUpdateMs > 1000/30) {
            lastUIUpdateMs = currentMillis;

            if(cameraCapture.getProperties().name.equals(ConfigManager.settings.currentCamera)) {
                HashMap<String, Object> WebSend = new HashMap<>();
                HashMap<String, Object> point = new HashMap<>();
                HashMap<String, Object> calculated = new HashMap<>();
                List<Double> center = new ArrayList<>();
                if (data.hasTarget) {
                    if(data instanceof CVPipeline2d.CVPipeline2dResult) {
                        CVPipeline2d.CVPipeline2dResult result = (CVPipeline2d.CVPipeline2dResult) data;
                        CVPipeline2d.Target2d bestTarget = result.targets.get(0);
                        center.add(bestTarget.rawPoint.center.x);
                        center.add(bestTarget.rawPoint.center.y);
                        calculated.put("pitch", bestTarget.pitch);
                        calculated.put("yaw", bestTarget.yaw);
                        calculated.put("area", bestTarget.area);
                    } else if (data instanceof CVPipeline3d.CVPipeline3dResult) {
                        // TODO: (2.1) 3d stuff in UI
                    } else {
                        center.add(null);
                        center.add(null);
                        calculated.put("pitch", null);
                        calculated.put("yaw", null);
                        calculated.put("area", null);
                    }
                } else {
                    center.add(null);
                    center.add(null);
                    calculated.put("pitch", null);
                    calculated.put("yaw", null);
                    calculated.put("area", null);
                }
                point.put("fps", visionRunnable.fps);
                point.put("calculated", calculated);
                point.put("rawPoint", center);
                WebSend.put("point", point);
                SocketHandler.broadcastMessage(WebSend);
            }
        }
    }

    private void updateNetworkTableData(CVPipelineResult data) {
        ntValidEntry.setBoolean(data.hasTarget);
        if(data.hasTarget && !(data instanceof DriverVisionPipeline.DriverPipelineResult)) {
            if(data instanceof CVPipeline2d.CVPipeline2dResult) {

                //noinspection unchecked
                List<CVPipeline2d.Target2d> targets = (List<CVPipeline2d.Target2d>) data.targets;
                ntLatencyEntry.setDouble(MathHandler.roundTo(data.processTime * 1e-6, 3));
                ntPitchEntry.setDouble(targets.get(0).pitch);
                ntYawEntry.setDouble(targets.get(0).yaw);
                ntAreaEntry.setDouble(targets.get(0).area);
                try {
                    ntAuxListEntry.setString(objectMapper.writeValueAsString(targets));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else if (data instanceof CVPipeline3d.CVPipeline3dResult) {
                // TODO: (2.1) 3d stuff...
            }
        } else {
            ntPitchEntry.setDouble(0.0);
            ntYawEntry.setDouble(0.0);
            ntAreaEntry.setDouble(0.0);
            ntLatencyEntry.setDouble(0.0);
            ntAuxListEntry.setString("");
        }
    }

    public void setVideoMode(VideoMode newMode) {
        cameraCapture.setVideoMode(newMode);
        cameraStreamer.setNewVideoMode(newMode);
    }

    public VideoMode getCurrentVideoMode() {
        return cameraCapture.getCurrentVideoMode();
    }

    public List<VideoMode> getPossibleVideoModes() {
        return cameraCapture.getProperties().videoModes;
    }

    public USBCameraCapture getCamera() {
        return cameraCapture;
    }

    public CVPipelineSettings getDriverModeSettings() {
        return pipelineManager.driverModePipeline.settings;
    }

    public void addCalibration(CameraCalibrationConfig cal) {
        cameraCapture.addCalibrationData(cal);
        cameraCalibrationConfigs.put(cal.resolution, cal);
        fileConfig.saveCalibration((List<CameraCalibrationConfig>) cameraCalibrationConfigs.values());
    }

    /**
     * VisionProcessRunnable will process images as quickly as possible
     */
    private class VisionProcessRunnable implements Runnable {

        volatile Double fps = 0.0;
        private CircularBuffer fpsAveragingBuffer = new CircularBuffer(7);

        @Override
        public void run() {
            var lastUpdateTimeNanos = System.nanoTime();
            while(!Thread.interrupted()) {

                // blocking call, will block until camera has a new frame.
                Pair<Mat, Long> camData = cameraCapture.getFrame();

                Mat camFrame = camData.getLeft();
                if (camFrame.cols() > 0 && camFrame.rows() > 0) {
                    CVPipelineResult result = pipelineManager.getCurrentPipeline().runPipeline(camFrame);
                    camFrame.release();

                    if (result != null) {
                        result.setTimestamp(camData.getRight());
                        lastPipelineResult = result;
                        updateNetworkTableData(lastPipelineResult);
                        updateUI(lastPipelineResult);
                    }
                }

                try {
                    streamFrameQueue.clear();
                    streamFrameQueue.add(lastPipelineResult.outputMat);
                } catch (Exception e) {
                    Debug.printInfo("Vision running faster than stream.");
                }

                var deltaTimeNanos = System.nanoTime() - lastUpdateTimeNanos;
                fpsAveragingBuffer.addFirst(1.0 / (deltaTimeNanos * 1E-09));
                lastUpdateTimeNanos = System.nanoTime();
                fps = getAverageFPS();
            }
        }

        double getAverageFPS() {
            var temp = 0.0;
            for(int i = 0; i < 7; i++) {
                temp += fpsAveragingBuffer.get(i);
            }
            temp /= 7.0;
            return temp;
        }

    }

    private class CameraStreamerRunnable extends LoopingRunnable {

        final CameraStreamer streamer;
        private Mat bufferMat = new Mat();

        private CameraStreamerRunnable(int cameraFPS, CameraStreamer streamer) {
            // add 2 FPS to allow for a bit of overhead
            super(1000L/(cameraFPS + 2));
            this.streamer = streamer;
        }

        @Override
        protected void process() {
            if (!streamFrameQueue.isEmpty()) {
                try {

                    bufferMat = streamFrameQueue.take();

                    try {
                        streamer.runStream(bufferMat);
                        bufferMat.release();
                    } catch (Exception e) {
                        // do nothing
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
