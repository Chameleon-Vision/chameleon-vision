package com.chameleonvision.web;

import com.chameleonvision.config.ConfigManager;
import com.chameleonvision.network.NetworkIPMode;
import com.chameleonvision.vision.VisionManager;
import com.chameleonvision.vision.VisionProcess;
import com.chameleonvision.vision.camera.USBCameraCapture;
import com.chameleonvision.vision.pipeline.CVPipelineSettings;
import com.chameleonvision.vision.pipeline.impl.Calibrate3dPipeline;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.cscore.VideoMode;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.io.IOException;
import java.util.HashMap;
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
        VideoMode mode = VisionManager.getCurrentUIVisionProcess().getPossibleVideoModes().get(resolutionIndex);
        VisionManager.getCurrentUIVisionProcess().pipelineManager.enableCalibrationMode(mode);
    }

    public static void onSnapshot(Context ctx) {
        var calPipe = VisionManager.getCurrentUIVisionProcess().pipelineManager.calib3dPipe;

        calPipe.takeSnapshot();

        Boolean hasEnough = calPipe.getCount() >= Calibrate3dPipeline.MIN_COUNT - 1;

        // manual serialization ftw
//            String toSend = String.format("{\n\t\"snapshotCount\" : \"%d\",\n\t\"hasEnough\" : \"%s\"\n}", calPipe.getCount(), hasEnough.toString());
        HashMap<String, Object> toSend = new HashMap<String, Object>();
        toSend.put("snapshotCount", calPipe.getCount());
        toSend.put("hasEnough", hasEnough);

        ctx.json(toSend);
//            ctx.res.getOutputStream().print(toSend);
        ctx.status(200);
    }

    public static void onCalibrationFinish(Context ctx) {
        VisionManager.getCurrentUIVisionProcess().pipelineManager.calib3dPipe.calibrate();
        VisionManager.getCurrentUIVisionProcess().pipelineManager.setCalibrationMode(false);
    }

    public static void onCalibrationCancellation(Context ctx) {
        VisionManager.getCurrentUIVisionProcess().pipelineManager.setCalibrationMode(false);
    }
}
