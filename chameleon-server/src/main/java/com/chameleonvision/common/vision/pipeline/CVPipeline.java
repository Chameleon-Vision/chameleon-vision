package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.vision.frame.Frame;

public interface CVPipeline {
    CVPipelineResult run(Frame frame, CVPipelineSettings settings);
}
