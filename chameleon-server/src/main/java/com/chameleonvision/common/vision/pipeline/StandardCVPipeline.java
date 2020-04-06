package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.vision.frame.Frame;
import java.util.List;

public class StandardCVPipeline implements CVPipeline {
    @Override
    public CVPipelineResult run(Frame frame, CVPipelineSettings settings) {

        // TODO: Implement all the things
        return new CVPipelineResult(0, List.of(), frame);
    }
}
