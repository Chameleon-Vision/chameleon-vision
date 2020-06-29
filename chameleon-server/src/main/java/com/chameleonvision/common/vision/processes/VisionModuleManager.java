package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.configuration.CameraConfiguration;
import com.chameleonvision.common.configuration.ConfigManager;
import com.chameleonvision.common.vision.pipeline.CVPipeline;
import com.chameleonvision.common.vision.pipeline.CVPipelineSettings;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** VisionModuleManager has many VisionModules, and provides camera configuration data to them. */
public class VisionModuleManager {
    protected final List<VisionModule> visionModules = new ArrayList<>();
    private final ConfigManager configManager = ConfigManager.getInstance();

    public VisionModuleManager(List<VisionSource> visionSources) {
        for (var visionSource : visionSources) {
            HashMap<String, CameraConfiguration> cameraConfigs = configManager.loadCameraConfigs();
            var pipelineManager = new PipelineManager();

            for (CameraConfiguration cameraConfig : cameraConfigs.values()) {
                for (CVPipelineSettings cvPipeSettings : cameraConfig.pipelineSettings)
                    try {
                        Class<?> classRef = cvPipeSettings.pipelineType.clazz;
                        var cvPipe = (CVPipeline) classRef.getConstructor(classRef).newInstance();
                        cvPipe.setSettings(cvPipeSettings);
                        pipelineManager.addPipeline(cvPipe);
                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {}
            }

            visionModules.add(new VisionModule(pipelineManager, visionSource));
        }
    }

    public void startModules() {
        for (var visionModule : visionModules) {
            visionModule.start();
        }
    }

}
