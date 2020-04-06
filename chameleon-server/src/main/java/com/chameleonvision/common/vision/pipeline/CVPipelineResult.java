package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.target.TrackedTarget;
import java.util.List;

public class CVPipelineResult {
    public final double latencyMillis;
    public final List<TrackedTarget> targets;
    public final Frame outputFrame;

    public CVPipelineResult(double latencyMillis, List<TrackedTarget> targets, Frame outputFrame) {
        this.latencyMillis = latencyMillis;
        this.targets = targets;

        // TODO: is this the best way to go about this?
        this.outputFrame = Frame.copyFrom(outputFrame);
    }

    public boolean hasTargets() {
        return !targets.isEmpty();
    }
}
