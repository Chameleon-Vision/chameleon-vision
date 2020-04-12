package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.vision.pipeline.CVPipeline;
import java.util.LinkedList;

public class PipelineManager {

    public final LinkedList<CVPipeline> pipelines = new LinkedList<>();

    // TODO add calibration and driver mode pipelines

    public CVPipeline getPipeline(int index) {
        return pipelines.get(index);
    }

    public CVPipeline getCurrentPipeline() {
        return null;
    }
}
