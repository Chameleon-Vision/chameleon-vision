package com.chameleonvision.common.dataflow.providers;

import com.chameleonvision.common.vision.processes.VisionModule;

public abstract class Provider {
    protected VisionModule parentModule;

    public Provider(VisionModule module) {
        parentModule = module;
    }
}
