package com.chameleonvision.common.vision.processes;

import java.util.ArrayList;
import java.util.List;

/** VisionModuleManager has many VisionModules, and provides camera configuration data to them. */
public class VisionModuleManager {
    protected final List<VisionModule> visionModules = new ArrayList<>();
    private VisionModule UIvisionModule;

    public VisionModuleManager(List<VisionSource> visionSources) {
        for (var visionSource : visionSources) {

            // TODO: loading existing pipelines from config
            var pipelineManager = new PipelineManager();

            visionModules.add(new VisionModule(pipelineManager, visionSource));
        }
        UIvisionModule = visionModules.get(0);
    }

    public void startModules() {
        for (var visionModule : visionModules) {
            visionModule.start();
        }
    }

    public VisionModule getUIvisionModule() {
        return UIvisionModule;
    }

    public void changeCamera(int index) {
        UIvisionModule = visionModules.get(index);
    }
}
