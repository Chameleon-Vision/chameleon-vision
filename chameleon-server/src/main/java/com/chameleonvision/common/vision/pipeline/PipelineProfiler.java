package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.common.util.math.MathUtils;

public class PipelineProfiler {

    private static final Logger reflectiveLogger = new Logger(ReflectivePipeline.class, LogGroup.VisionProcess);

    public static void printReflectiveProfile(long[] nanos) {
        if (nanos.length != 14) {
            return;
        }

        var sb = new StringBuilder("ReflectivePipelineProfile - ");
        sb.append("RotateImage: ").append(MathUtils.roundTo(nanos[0] / 1e+6, 3)).append("ms, ");
        sb.append("ErodeDilate: ").append(MathUtils.roundTo(nanos[1] / 1e+6, 3)).append("ms, ");
        sb.append("HSV: ").append(MathUtils.roundTo(nanos[2] / 1e+6, 3)).append("ms, ");
        sb.append("OutputMat: ").append(MathUtils.roundTo(nanos[3] / 1e+6, 3)).append("ms, ");
        sb.append("FindContours: ").append(MathUtils.roundTo(nanos[4] / 1e+6, 3)).append("ms, ");
        sb.append("FilterContours: ").append(MathUtils.roundTo(nanos[5] / 1e+6, 3)).append("ms, ");
        sb.append("SpeckleReject: ").append(MathUtils.roundTo(nanos[6] / 1e+6, 3)).append("ms, ");
        sb.append("GroupContours: ").append(MathUtils.roundTo(nanos[7] / 1e+6, 3)).append("ms, ");
        sb.append("SortContours: ").append(MathUtils.roundTo(nanos[8] / 1e+6, 3)).append("ms, ");

        sb.append("Collect2dTargets: ").append(MathUtils.roundTo(nanos[9] / 1e+6, 3)).append("ms, ");

        // check which target method is used
        if (nanos[10] != 0 && nanos[11] != 0) {
            sb.append("CornerDetection: ").append(MathUtils.roundTo(nanos[10] / 1e+6, 3)).append("ms, "); //
            sb.append("SolvePNP: ").append(MathUtils.roundTo(nanos[11] / 1e+6, 3)).append("ms, "); //
        } else {
            sb.append("CornerDetection: Skipped, "); //
            sb.append("SolvePNP: Skipped, "); //
        }

        sb.append("Draw2dCrosshair: ").append(MathUtils.roundTo(nanos[12] / 1e+6, 3)).append("ms, ");

        // check if 2d or 3d being drawn (2d is positive, 3d is negative
        if (nanos[13] > 0) {
            sb.append("Draw2dTarget: ").append(MathUtils.roundTo(nanos[13] / 1e+6, 3)).append("ms");
        } else {
            sb.append("Draw3dTarget: ").append(MathUtils.roundTo((nanos[13] * -1) / 1e+6, 3)).append("ms");
        }

        reflectiveLogger.trace(sb.toString());
    }
}
