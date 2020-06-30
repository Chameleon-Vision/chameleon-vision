package com.chameleonvision.common.vision.processes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** VisionModuleManager has many VisionModules, and provides camera configuration data to them. */
public class VisionModuleManager {
    public static final List<VisionModule> visionModules = new ArrayList<>();
    public static VisionModule UIvisionModule;

    public VisionModuleManager(List<VisionSource> visionSources) {
        for (var visionSource : visionSources) {
            var pipelineManager =
                    new PipelineManager(
                            visionSource.getCameraConfiguration().pipelineSettings,
                            visionSource.getCameraConfiguration().driveModeSettings);

            var module = new VisionModule(pipelineManager, visionSource);
            visionModules.add(module);
        }
        UIvisionModule = visionModules.get(0);
    }

    public void startModules() {
        for (var visionModule : visionModules) {
            visionModule.start();
        }
    }

    public static VisionModule getUIvisionModule() {
        return UIvisionModule;
    }

    public static List<String> getCameraNickNameList() {
        return visionModules.stream().map(VisionModule::getSourceNickname).collect(Collectors.toList());
    }

    public static void changeCamera(int index) {
        UIvisionModule = visionModules.get(index);
    }
}
