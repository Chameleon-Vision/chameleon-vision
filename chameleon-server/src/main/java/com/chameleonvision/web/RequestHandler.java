package com.chameleonvision.web;

import com.chameleonvision.config.ConfigManager;
import com.chameleonvision.network.NetworkIPMode;
import com.chameleonvision.vision.VisionManager;
import com.chameleonvision.vision.VisionProcess;
import com.chameleonvision.vision.camera.USBCameraCapture;
import com.chameleonvision.vision.pipeline.CVPipelineSettings;
import com.chameleonvision.vision.pipeline.PipelineManager;
import com.chameleonvision.vision.pipeline.impl.Calibrate3dPipeline;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.cscore.VideoMode;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHandler {

    public static void onGeneralSettings(Context ctx) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map map = objectMapper.readValue(ctx.body(), Map.class);

            // TODO: change to function, to restart NetworkTables
            ConfigManager.settings.teamNumber = (int) map.get("teamNumber");

            ConfigManager.settings.connectionType = NetworkIPMode.values()[(int) map.get("connectionType")];
            ConfigManager.settings.ip = (String) map.get("ip");
            ConfigManager.settings.netmask = (String) map.get("netmask");
            ConfigManager.settings.gateway = (String) map.get("gateway");
            ConfigManager.settings.hostname = (String) map.get("hostname");
            ConfigManager.saveGeneralSettings();
            SocketHandler.sendFullSettings();
            ctx.status(200);
        } catch (JsonProcessingException e) {
            ctx.status(500);
        }
    }

    public static void onDuplicatePipeline(Context ctx) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map newPipelineData = objectMapper.readValue(ctx.body(), Map.class);

            int newCam = -1;
            try {
                newCam = (Integer) newPipelineData.get("camera");
            } catch (Exception e) {
                // ignored
            }

            var pipeline = (CVPipelineSettings) newPipelineData.get("pipeline");

            if (newCam == -1) {
                if (VisionManager.getCurrentCameraPipelineNicknames().contains(pipeline.nickname)) {
                    ctx.status(400); // BAD REQUEST
                } else {
                    VisionManager.getCurrentUIVisionProcess().pipelineManager.addPipeline(pipeline);
                    ctx.status(200);
                }
            } else {
                var cam = VisionManager.getVisionProcessByIndex(newCam);
                if (cam != null && cam.pipelineManager.pipelines.stream().anyMatch(c -> c.settings.nickname.equals(pipeline.nickname))) {
                    ctx.status(400); // BAD REQUEST
                } else {
                    cam.pipelineManager.addPipeline(pipeline);
                    ctx.status(200);
                }
            }

        } catch (JsonProcessingException e) {
            ctx.status(500);
        }
    }

    public static void onCameraSettings(Context ctx) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map camSettings = objectMapper.readValue(ctx.body(), Map.class);

            VisionProcess currentVisionProcess = VisionManager.getCurrentUIVisionProcess();
            USBCameraCapture currentCamera = currentVisionProcess.getCamera();

            double newFOV;
            try {
                newFOV = (Double) camSettings.get("fov");
            } catch (Exception ignored) {
                newFOV = (Integer) camSettings.get("fov");
            }
            currentCamera.getProperties().setFOV(newFOV);
            VisionManager.saveCurrentCameraSettings();
            SocketHandler.sendFullSettings();
            ctx.status(200);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }

    public static void onCalibrationStart(Context ctx) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map data = objectMapper.readValue(ctx.body(), Map.class);
        int resolutionIndex = (Integer) data.get("resolution");
        int index = VisionManager.getCurrentUIVisionProcess().pipelineManager.getCurrentPipeline().settings.videoModeIndex;
        VisionManager.getCurrentUIVisionProcess().pipelineManager.calib3dPipe.settings.videoModeIndex = index;
        VisionManager.getCurrentUIVisionProcess().pipelineManager.setCalibrationMode(true);
    }

    public static void onSnapshot(Context ctx) {
        Calibrate3dPipeline calPipe = VisionManager.getCurrentUIVisionProcess().pipelineManager.calib3dPipe;

        calPipe.takeSnapshot();

        HashMap<String, Object> toSend = new HashMap<String, Object>();
        toSend.put("snapshotCount", calPipe.getSnapshotCount());
        toSend.put("hasEnough", calPipe.hasEnoughSnapshots());

        ctx.json(toSend);
        ctx.status(200);
    }

    public static void onCalibrationFinish(Context ctx) {
        PipelineManager pipeManager = VisionManager.getCurrentUIVisionProcess().pipelineManager;
        System.out.println("Finishing Cal");
        if (pipeManager.calib3dPipe.tryCalibration()) {
            ctx.status(200);
        } else {
            System.err.println("CALFAIL");
            ctx.status(500);
        }
        pipeManager.setCalibrationMode(false);
    }

    public static void onCalibrationCancellation(Context ctx) {
        VisionManager.getCurrentUIVisionProcess().pipelineManager.setCalibrationMode(false);
        ctx.status(200);
    }

    public static void onPnpModel(Context ctx) throws JsonProcessingException {
        System.out.println(ctx.body());
        ObjectMapper objectMapper = new ObjectMapper();
        List points = objectMapper.readValue(ctx.body(), List.class);
        System.out.println(points);
    }
}
