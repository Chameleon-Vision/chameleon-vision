package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.vision.frame.Frame;

public interface CVPipeline<R extends CVPipelineResult, S extends CVPipelineSettings> {
    R run(Frame frame, S settings);
}
