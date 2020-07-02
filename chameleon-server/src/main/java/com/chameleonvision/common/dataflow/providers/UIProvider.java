package com.chameleonvision.common.dataflow.providers;

import com.chameleonvision.common.configuration.ConfigManager;
import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.common.vision.frame.FrameDivisor;
import com.chameleonvision.common.vision.pipeline.ReflectivePipelineSettings;
import com.chameleonvision.common.vision.processes.VisionModule;
import java.lang.reflect.Field;

public class UIProvider extends Provider {
    private static final Logger logger = new Logger(UIProvider.class, LogGroup.VisionProcess);

    public UIProvider(VisionModule module) {
        super(module);
    }

    public void onMessage(String key, Object value) {
        switch (key) {
            case "changePipelineName":
                {
                    parentModule.pipelineManager.getCurrentPipeline().getSettings().pipelineNickname =
                            (String) value;
                    break;
                }
            case "addNewPipeline":
                {
                    ReflectivePipelineSettings pipe = new ReflectivePipelineSettings();
                    pipe.pipelineNickname = (String) value;
                    parentModule.pipelineManager.addPipeline(pipe);
                    break;
                }
            case "currentPipeline":
                {
                    parentModule.pipelineManager.changeCurrentPipeline((Integer) value);
                    break;
                }
            case "command":
                {
                    switch ((String) value) {
                        case "deleteCurrentPipeline":
                            parentModule.pipelineManager.removeCurrentPipeline();
                            break;
                        case "save":
                            ConfigManager.getInstance().save();
                            break;
                    }
                    break;
                }
            default:
                {
                    setField(parentModule.pipelineManager.getCurrentPipeline().getSettings(), key, value);
                    break;
                }
        }
        switch (key) {
                //            TODO oriantation
            case "cameraExposure":
                {
                    parentModule.getVisionSource().getSettables().setExposure((Integer) value);
                    break;
                }
            case "cameraBrightness":
                {
                    parentModule.getVisionSource().getSettables().setBrightness((Integer) value);
                    break;
                }
            case "cameraGain":
                {
                    parentModule.getVisionSource().getSettables().setGain((Integer) value);
                    break;
                }
            case "cameraVideoModeIndex":
                {
                    var videoMode =
                            parentModule.getVisionSource().getSettables().getAllVideoModes().get((Integer) value);
                    parentModule.getVisionSource().getSettables().setCurrentVideoMode(videoMode);
                    break;
                }
            case "outputFrameDivisor":
                {
                    parentModule.pipelineManager.getCurrentPipeline().getSettings().outputFrameDivisor =
                            FrameDivisor.values()[(Integer) value];
                    parentModule.setStreamResolution();
                    break;
                }
        }
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getField(fieldName);
            if (field.getType().isEnum())
                field.set(obj, field.getType().getEnumConstants()[(Integer) value]);
            else field.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            logger.error(ex.getMessage());
        }
    }
}
