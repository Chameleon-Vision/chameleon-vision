package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.util.math.MathUtils;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.pipe.impl.Draw2dCrosshairPipe;
import com.chameleonvision.common.vision.pipe.impl.ResizeImagePipe;
import com.chameleonvision.common.vision.pipe.impl.RotateImagePipe;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class DriverModePipeline
        implements CVPipeline<DriverModePipelineResult, DriverModePipelineSettings> {

    private final RotateImagePipe rotateImagePipe = new RotateImagePipe();

    private final ResizeImagePipe resizeImagePipe = new ResizeImagePipe();

    private final Draw2dCrosshairPipe draw2dCrosshairPipe = new Draw2dCrosshairPipe();

    private void setPipeParams(DriverModePipelineSettings settings) {
        RotateImagePipe.RotateImageParams rotateImageParams =
                new RotateImagePipe.RotateImageParams(settings.inputImageRotationMode);
        rotateImagePipe.setParams(rotateImageParams);

        ResizeImagePipe.ResizeImageParams resizeImageParams =
                new ResizeImagePipe.ResizeImageParams(settings.inputFrameDivisor);
        resizeImagePipe.setParams(resizeImageParams);

        Draw2dCrosshairPipe.Draw2dCrosshairParams draw2dCrosshairParams =
                new Draw2dCrosshairPipe.Draw2dCrosshairParams(
                        settings.offsetPointMode, settings.offsetPoint);
        draw2dCrosshairPipe.setParams(draw2dCrosshairParams);
    }

    @Override
    public DriverModePipelineResult run(Frame frame, DriverModePipelineSettings settings) {

        // update pipe parameters

        // apply pipes
        var rotateImageResult = rotateImagePipe.apply(frame.image);
        var resizeImageResult = resizeImagePipe.apply(rotateImageResult.result);
        var draw2dCrosshairResult =
                draw2dCrosshairPipe.apply(Pair.of(resizeImageResult.result, List.of()));

        // calculate elapsed nanoseconds
        long totalNanos =
                rotateImageResult.nanosElapsed
                        + resizeImageResult.nanosElapsed
                        + draw2dCrosshairResult.nanosElapsed;

        return new DriverModePipelineResult(
                MathUtils.nanosToMillis(totalNanos),
                new Frame(draw2dCrosshairResult.result, frame.frameStaticProperties));
    }
}
