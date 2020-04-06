package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.util.math.MathUtils;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.pipe.impl.ResizeImagePipe;
import com.chameleonvision.common.vision.pipe.impl.RotateImagePipe;

public class DriverModePipeline implements CVPipeline {

    private final RotateImagePipe rotateImagePipe = new RotateImagePipe();
    private final ResizeImagePipe resizeImagePipe = new ResizeImagePipe();

    @Override
    public CVPipelineResult run(Frame frame, CVPipelineSettings settings) {
        // update pipe parameters
        rotateImagePipe.setParams(
                new RotateImagePipe.RotateImageParams(settings.inputImageRotationMode));
        resizeImagePipe.setParams(new ResizeImagePipe.ResizeImageParams(settings.inputFrameDivisor));

        // apply pipes
        var rotateImageResult = rotateImagePipe.apply(frame.image);
        var resizeImageResult = resizeImagePipe.apply(rotateImageResult.result);

        // calculate elapsed nanoseconds
        long totalNanos = rotateImageResult.nanosElapsed + resizeImageResult.nanosElapsed;

        return new DriverModePipelineResult(
                MathUtils.nanosToMillis(totalNanos), new Frame(resizeImageResult.result));
    }
}
