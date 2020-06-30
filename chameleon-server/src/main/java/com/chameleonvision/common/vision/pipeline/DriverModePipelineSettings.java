package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.vision.target.RobotOffsetPointMode;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;

@JsonTypeName("DriverModePipelineSettings")
public class DriverModePipelineSettings extends CVPipelineSettings {
    public RobotOffsetPointMode offsetPointMode = RobotOffsetPointMode.None;
    public List<Number> offsetPoint = new ArrayList<>();

    public DriverModePipelineSettings() {
        super();
        pipelineType = PipelineType.DriverMode;
    }
}
