package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.util.Debug;
import com.chameleonvision.common.util.math.MathUtils;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameStaticProperties;
import com.chameleonvision.common.vision.opencv.CVMat;
import com.chameleonvision.common.vision.opencv.Contour;
import com.chameleonvision.common.vision.opencv.DualMat;
import com.chameleonvision.common.vision.pipe.CVPipeResult;
import com.chameleonvision.common.vision.pipe.impl.Collect2dTargetsPipe;
import com.chameleonvision.common.vision.pipe.impl.CornerDetectionPipe;
import com.chameleonvision.common.vision.pipe.impl.Draw2dCrosshairPipe;
import com.chameleonvision.common.vision.pipe.impl.Draw2dTargetsPipe;
import com.chameleonvision.common.vision.pipe.impl.Draw3dTargetsPipe;
import com.chameleonvision.common.vision.pipe.impl.ErodeDilatePipe;
import com.chameleonvision.common.vision.pipe.impl.FilterContoursPipe;
import com.chameleonvision.common.vision.pipe.impl.FindContoursPipe;
import com.chameleonvision.common.vision.pipe.impl.GroupContoursPipe;
import com.chameleonvision.common.vision.pipe.impl.HSVPipe;
import com.chameleonvision.common.vision.pipe.impl.OutputMatPipe;
import com.chameleonvision.common.vision.pipe.impl.RotateImagePipe;
import com.chameleonvision.common.vision.pipe.impl.SolvePNPPipe;
import com.chameleonvision.common.vision.pipe.impl.SortContoursPipe;
import com.chameleonvision.common.vision.pipe.impl.SpeckleRejectPipe;
import com.chameleonvision.common.vision.target.PotentialTarget;
import com.chameleonvision.common.vision.target.TrackedTarget;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Mat;

/** Represents a pipeline for tracking retro-reflective targets. */
public class ReflectivePipeline extends CVPipeline<CVPipelineResult, ReflectivePipelineSettings> {

    private final RotateImagePipe rotateImagePipe = new RotateImagePipe();
    private final ErodeDilatePipe erodeDilatePipe = new ErodeDilatePipe();
    private final HSVPipe hsvPipe = new HSVPipe();
    private final OutputMatPipe outputMatPipe = new OutputMatPipe();
    private final FindContoursPipe findContoursPipe = new FindContoursPipe();
    private final FilterContoursPipe filterContoursPipe = new FilterContoursPipe();
    private final SpeckleRejectPipe speckleRejectPipe = new SpeckleRejectPipe();
    private final GroupContoursPipe groupContoursPipe = new GroupContoursPipe();
    private final SortContoursPipe sortContoursPipe = new SortContoursPipe();
    private final Collect2dTargetsPipe collect2dTargetsPipe = new Collect2dTargetsPipe();
    private final CornerDetectionPipe cornerDetectionPipe = new CornerDetectionPipe();
    private final SolvePNPPipe solvePNPPipe = new SolvePNPPipe();
    private final Draw2dCrosshairPipe draw2dCrosshairPipe = new Draw2dCrosshairPipe();
    private final Draw2dTargetsPipe draw2dTargetsPipe = new Draw2dTargetsPipe();
    private final Draw3dTargetsPipe draw3dTargetsPipe = new Draw3dTargetsPipe();

    private final Mat rawInputMat = new Mat();
    private final DualMat outputMats = new DualMat();

    private final long[] pipeNanos = new long[14];

    public ReflectivePipeline() {
        settings = new ReflectivePipelineSettings();
    }

    @Override
    protected void setPipeParams(
            FrameStaticProperties frameStaticProperties, ReflectivePipelineSettings settings) {
        RotateImagePipe.RotateImageParams rotateImageParams =
                new RotateImagePipe.RotateImageParams(settings.inputImageRotationMode);
        rotateImagePipe.setParams(rotateImageParams);

        ErodeDilatePipe.ErodeDilateParams erodeDilateParams =
                new ErodeDilatePipe.ErodeDilateParams(settings.erode, settings.dilate, 5);
        // TODO: add kernel size to pipeline settings
        erodeDilatePipe.setParams(erodeDilateParams);

        HSVPipe.HSVParams hsvParams =
                new HSVPipe.HSVParams(settings.hsvHue, settings.hsvSaturation, settings.hsvValue);
        hsvPipe.setParams(hsvParams);

        OutputMatPipe.OutputMatParams outputMatParams =
                new OutputMatPipe.OutputMatParams(settings.outputShowThresholded);
        outputMatPipe.setParams(outputMatParams);

        FindContoursPipe.FindContoursParams findContoursParams =
                new FindContoursPipe.FindContoursParams();
        findContoursPipe.setParams(findContoursParams);

        SpeckleRejectPipe.SpeckleRejectParams speckleRejectParams =
                new SpeckleRejectPipe.SpeckleRejectParams(settings.contourSpecklePercentage);
        speckleRejectPipe.setParams(speckleRejectParams);

        FilterContoursPipe.FilterContoursParams filterContoursParams =
                new FilterContoursPipe.FilterContoursParams(
                        settings.contourArea,
                        settings.contourRatio,
                        settings.contourExtent,
                        frameStaticProperties);
        filterContoursPipe.setParams(filterContoursParams);

        GroupContoursPipe.GroupContoursParams groupContoursParams =
                new GroupContoursPipe.GroupContoursParams(
                        settings.contourGroupingMode, settings.contourIntersection);
        groupContoursPipe.setParams(groupContoursParams);

        SortContoursPipe.SortContoursParams sortContoursParams =
                new SortContoursPipe.SortContoursParams(settings.contourSortMode, frameStaticProperties, 5);
        sortContoursPipe.setParams(sortContoursParams);

        Collect2dTargetsPipe.Collect2dTargetsParams collect2dTargetsParams =
                new Collect2dTargetsPipe.Collect2dTargetsParams(
                        frameStaticProperties,
                        settings.offsetRobotOffsetMode,
                        settings.offsetDualLineM,
                        settings.offsetDualLineB,
                        settings.offsetCalibrationPoint.toPoint(),
                        settings.contourTargetOffsetPointEdge,
                        settings.contourTargetOrientation);
        collect2dTargetsPipe.setParams(collect2dTargetsParams);

        var params =
                new CornerDetectionPipe.CornerDetectionPipeParameters(
                        settings.cornerDetectionStrategy,
                        settings.cornerDetectionUseConvexHulls,
                        settings.cornerDetectionExactSideCount,
                        settings.cornerDetectionSideCount,
                        settings.cornerDetectionAccuracyPercentage);
        cornerDetectionPipe.setParams(params);

        Draw2dTargetsPipe.Draw2dContoursParams draw2dContoursParams =
                new Draw2dTargetsPipe.Draw2dContoursParams(settings.outputShowMultipleTargets);
        draw2dTargetsPipe.setParams(draw2dContoursParams);

        Draw2dCrosshairPipe.Draw2dCrosshairParams draw2dCrosshairParams =
                new Draw2dCrosshairPipe.Draw2dCrosshairParams(
                        settings.offsetRobotOffsetMode, settings.offsetCalibrationPoint);
        draw2dCrosshairPipe.setParams(draw2dCrosshairParams);

        var draw3dContoursParams =
                new Draw3dTargetsPipe.Draw3dContoursParams(
                        settings.cameraCalibration, settings.targetModel);
        draw3dTargetsPipe.setParams(draw3dContoursParams);

        var solvePNPParams =
                new SolvePNPPipe.SolvePNPPipeParams(
                        settings.cameraCalibration, settings.cameraPitch, settings.targetModel);
        solvePNPPipe.setParams(solvePNPParams);
    }

    @Override
    public CVPipelineResult process(Frame frame, ReflectivePipelineSettings settings) {
        setPipeParams(frame.frameStaticProperties, settings);

        long sumPipeNanosElapsed = 0L;

        frame.image.getMat().copyTo(rawInputMat);

        CVPipeResult<Mat> rotateImageResult = rotateImagePipe.apply(frame.image.getMat());
        pipeNanos[0] = rotateImageResult.nanosElapsed;
        sumPipeNanosElapsed += rotateImageResult.nanosElapsed;

        CVPipeResult<Mat> erodeDilateResult = erodeDilatePipe.apply(rotateImageResult.result);
        pipeNanos[1] = erodeDilateResult.nanosElapsed;
        sumPipeNanosElapsed += erodeDilateResult.nanosElapsed;

        CVPipeResult<Mat> hsvPipeResult = hsvPipe.apply(erodeDilateResult.result);
        pipeNanos[2] = hsvPipeResult.nanosElapsed;
        sumPipeNanosElapsed += hsvPipeResult.nanosElapsed;

        // mat leak fix attempt
        outputMats.first = rawInputMat;
        outputMats.second = hsvPipeResult.result;

        CVPipeResult<Mat> outputMatResult = outputMatPipe.apply(outputMats);
        pipeNanos[3] = outputMatResult.nanosElapsed;
        sumPipeNanosElapsed += outputMatResult.nanosElapsed;

        CVPipeResult<List<Contour>> findContoursResult = findContoursPipe.apply(hsvPipeResult.result);
        pipeNanos[4] = findContoursResult.nanosElapsed;
        sumPipeNanosElapsed += findContoursResult.nanosElapsed;

        var filterContoursResult = filterContoursPipe.apply(findContoursResult.result);
        pipeNanos[5] = filterContoursResult.nanosElapsed;
        sumPipeNanosElapsed += filterContoursResult.nanosElapsed;

        CVPipeResult<List<Contour>> speckleRejectResult =
                speckleRejectPipe.apply(filterContoursResult.result);
        pipeNanos[6] = speckleRejectResult.nanosElapsed;
        sumPipeNanosElapsed += speckleRejectResult.nanosElapsed;

        CVPipeResult<List<PotentialTarget>> groupContoursResult =
                groupContoursPipe.apply(speckleRejectResult.result);
        pipeNanos[7] = groupContoursResult.nanosElapsed;
        sumPipeNanosElapsed += groupContoursResult.nanosElapsed;

        CVPipeResult<List<PotentialTarget>> sortContoursResult =
                sortContoursPipe.apply(groupContoursResult.result);
        pipeNanos[8] = sortContoursResult.nanosElapsed;
        sumPipeNanosElapsed += sortContoursResult.nanosElapsed;

        CVPipeResult<List<TrackedTarget>> collect2dTargetsResult =
                collect2dTargetsPipe.apply(sortContoursResult.result);
        pipeNanos[9] = collect2dTargetsResult.nanosElapsed;
        sumPipeNanosElapsed += collect2dTargetsResult.nanosElapsed;

        CVPipeResult<List<TrackedTarget>> collectTargetsResult;

        // 3d stuff
        if (settings.solvePNPEnabled) {
            CVPipeResult<List<TrackedTarget>> cornerDetectionResult =
                    cornerDetectionPipe.apply(collect2dTargetsResult.result);
            pipeNanos[10] = cornerDetectionResult.nanosElapsed;
            sumPipeNanosElapsed += cornerDetectionResult.nanosElapsed;

            CVPipeResult<List<TrackedTarget>> solvePNPResult =
                    solvePNPPipe.apply(cornerDetectionResult.result);
            pipeNanos[11] = solvePNPResult.nanosElapsed;
            sumPipeNanosElapsed += solvePNPResult.nanosElapsed;

            collectTargetsResult = solvePNPResult;
        } else {
            pipeNanos[10] = 0;
            pipeNanos[11] = 0;
            collectTargetsResult = collect2dTargetsResult;
        }

        CVPipeResult<Mat> draw2dCrosshairResult =
                draw2dCrosshairPipe.apply(Pair.of(outputMatResult.result, collectTargetsResult.result));
        pipeNanos[12] = draw2dCrosshairResult.nanosElapsed;
        sumPipeNanosElapsed += draw2dCrosshairResult.nanosElapsed;

        CVPipeResult<Mat> drawTargetResult;

        if (settings.solvePNPEnabled) {
            drawTargetResult =
                    draw3dTargetsPipe.apply(
                            Pair.of(draw2dCrosshairResult.result, collect2dTargetsResult.result));
            pipeNanos[13] = -drawTargetResult.nanosElapsed;
        } else {
            drawTargetResult =
                    draw2dTargetsPipe.apply(
                            Pair.of(draw2dCrosshairResult.result, collect2dTargetsResult.result));
            pipeNanos[13] = drawTargetResult.nanosElapsed;
        }
        sumPipeNanosElapsed += drawTargetResult.nanosElapsed;

        if (settings.outputShowThresholded) {
            rawInputMat.release();
        }

        if (Debug.ShouldProfile) {
            PipelineProfiler.printReflectiveProfile(pipeNanos);
        }

        return new CVPipelineResult(
                MathUtils.nanosToMillis(sumPipeNanosElapsed),
                collectTargetsResult.result,
                new Frame(new CVMat(drawTargetResult.result), frame.frameStaticProperties));
    }
}
