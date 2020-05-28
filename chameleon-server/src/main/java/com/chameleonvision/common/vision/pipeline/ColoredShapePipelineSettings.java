package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.vision.opencv.ContourShape;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ColoredShapePipelineSettings")
public class ColoredShapePipelineSettings extends AdvancedPipelineSettings {
    ContourShape desiredShape;

    public ColoredShapePipelineSettings() {
        super();
        pipelineType = PipelineType.ColoredShape;
    }
}
