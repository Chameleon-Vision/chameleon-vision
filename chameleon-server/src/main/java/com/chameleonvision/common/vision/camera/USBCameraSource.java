package com.chameleonvision.common.vision.camera;

import com.chameleonvision.common.configuration.CameraConfiguration;
import com.chameleonvision.common.vision.frame.FrameProvider;
import com.chameleonvision.common.vision.frame.FrameStaticProperties;
import com.chameleonvision.common.vision.frame.provider.USBFrameProvider;
import com.chameleonvision.common.vision.processes.VisionSource;
import com.chameleonvision.common.vision.processes.VisionSourceSettables;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.UsbCamera;

import edu.wpi.cscore.VideoMode;
import edu.wpi.first.cameraserver.CameraServer;

import java.util.*;

public class USBCameraSource implements VisionSource {
    private static final int PS3EYE_VID = 0x1415;
    private static final int PS3EYE_PID = 0x2000;
    CvSink cvSink;
    UsbCamera camera;
    final boolean isPS3Eye;
    private final USBCameraSettables usbCameraSettables;
    USBFrameProvider usbFrameProvider;

    public USBCameraSource(String path, CameraConfiguration config) {
        this.camera = new UsbCamera(config.nickname, path);
        this.isPS3Eye = camera.getInfo().productId == PS3EYE_PID && camera.getInfo().vendorId == PS3EYE_VID;
        this.cvSink = CameraServer.getInstance().getVideo(this.camera);
        this.usbCameraSettables = new USBCameraSettables(config);
        this.usbFrameProvider = new USBFrameProvider(cvSink, usbCameraSettables.getFrameStaticProperties());
    }

    @Override
    public FrameProvider getFrameProvider() {
        return usbFrameProvider;
    }

    @Override
    public VisionSourceSettables getSettables() {
        return this.usbCameraSettables;
    }

    public class USBCameraSettables extends VisionSourceSettables {
        protected USBCameraSettables(CameraConfiguration configuration){
            super(configuration);
        }

        @Override
        public int getExposure() {
            return camera.getProperty("exposure").get();
        }

        @Override
        public void setExposure(int exposure) {
            camera.setExposureManual(exposure);
        }

        @Override
        public int getBrightness() {
            return camera.getBrightness();
        }

        @Override
        public void setBrightness(int brightness) {
            camera.setBrightness(brightness);
        }

        @Override
        public int getGain() {
            return camera.getProperty("gain").get();
        }

        @Override
        public void setGain(int gain) {
            if (isPS3Eye) {
                camera.getProperty("gain_automatic").set(0);
                camera.getProperty("gain").set(gain);
            }
        }

        @Override
        public VideoMode getCurrentVideoMode() {
            return camera.getVideoMode();
        }

        @Override
        public void setCurrentVideoMode(VideoMode videoMode) {
            camera.setVideoMode(videoMode);
        }

        @Override
        public HashMap<Integer, VideoMode> getAllVideoModes() {
            if (videoModes == null) {
                videoModes = new HashMap<>();
                List<VideoMode> videoModesList = Arrays.asList(camera.enumerateVideoModes());
                for (VideoMode videoMode : videoModesList) {
                    videoModes.put(videoModesList.indexOf(videoMode), videoMode);
                }
            }
            return videoModes;
        }

    }
}
