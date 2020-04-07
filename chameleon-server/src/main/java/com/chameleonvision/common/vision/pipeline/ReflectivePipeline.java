package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameStaticProperties;
import com.chameleonvision.common.vision.opencv.Contour;
import com.chameleonvision.common.vision.pipe.CVPipeResult;
import com.chameleonvision.common.vision.pipe.impl.*;
import com.chameleonvision.common.vision.target.PotentialTarget;
import com.chameleonvision.common.vision.target.TrackedTarget;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Mat;

/** Represents a pipeline for tracking retro-reflective targets. */
public class ReflectivePipeline
        implements CVPipeline<CVPipelineResult, ReflectivePipelineSettings> {

    private final RotateImagePipe rotateImagePipe = new RotateImagePipe();
    private final ErodeDilatePipe erodeDilatePipe = new ErodeDilatePipe();
    private final HSVPipe hsvPipe = new HSVPipe();
    private final OutputMatPipe outputMatPipe = new OutputMatPipe();
    private final FindContoursPipe findContoursPipe = new FindContoursPipe();
    private final SpeckleRejectPipe speckleRejectPipe = new SpeckleRejectPipe();
    private final GroupContoursPipe groupContoursPipe = new GroupContoursPipe();
    private final SortContoursPipe sortContoursPipe = new SortContoursPipe();
    private final Collect2dTargetsPipe collect2dTargetsPipe = new Collect2dTargetsPipe();
    //    private final SolvePNPPipe solvePNPPipe = new SolvePNPPipe();
    private final Draw2dCrosshairPipe draw2dCrosshairPipe = new Draw2dCrosshairPipe();
    private final Draw2dContoursPipe draw2dContoursPipe = new Draw2dContoursPipe();
    //    private final DrawSolvePNPPipe drawSolvePNPPipe = new DrawSolvePNPPipe();

    private long sumPipeNanosElapsed = 0L;
    private Mat rawInputMat = new Mat();

    private void setPipeParams(
            ReflectivePipelineSettings settings, FrameStaticProperties frameStaticProperties) {
        RotateImagePipe.RotateImageParams rotateImageParams =
                new RotateImagePipe.RotateImageParams(settings.inputImageRotationMode);
        rotateImagePipe.setParams(rotateImageParams);

        ErodeDilatePipe.ErodeDilateParams erodeDilateParams =
                new ErodeDilatePipe.ErodeDilateParams(
                        settings.erode, settings.dilate, 5); // TODO: add kernel size to pipeline settings
        erodeDilatePipe.setParams(erodeDilateParams);

        HSVPipe.HSVParams hsvParams =
                new HSVPipe.HSVParams(settings.hsvHue, settings.hsvSaturation, settings.hsvValue);
        hsvPipe.setParams(hsvParams);

        OutputMatPipe.OutputMatParams outputMatParams =
                new OutputMatPipe.OutputMatParams(settings.outputShowThresholded);
        outputMatPipe.setParams(outputMatParams);

        // TODO: necessary? offer different contour methods?
        FindContoursPipe.FindContoursParams findContoursParams =
                new FindContoursPipe.FindContoursParams();
        findContoursPipe.setParams(findContoursParams);

        SpeckleRejectPipe.SpeckleRejectParams speckleRejectParams =
                new SpeckleRejectPipe.SpeckleRejectParams(settings.contourSpecklePercentage);
        speckleRejectPipe.setParams(speckleRejectParams);

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

        Draw2dContoursPipe.Draw2dContoursParams draw2dContoursParams =
                new Draw2dContoursPipe.Draw2dContoursParams(settings.outputShowMultipleTargets);
        draw2dContoursPipe.setParams(draw2dContoursParams);

        Draw2dCrosshairPipe.Draw2dCrosshairParams draw2dCrosshairParams =
                new Draw2dCrosshairPipe.Draw2dCrosshairParams(
                        settings.offsetRobotOffsetMode, settings.offsetCalibrationPoint);
        draw2dCrosshairPipe.setParams(draw2dCrosshairParams);
    }

    @Override
    public CVPipelineResult run(Frame frame, ReflectivePipelineSettings settings) {
        setPipeParams(settings, frame.frameStaticProperties);

        // save for later. used to switch between input frame and thresholded image
        frame.image.copyTo(rawInputMat);

        CVPipeResult<Mat> rotateImageResult = rotateImagePipe.apply(frame.image);
        sumPipeNanosElapsed += rotateImageResult.nanosElapsed;

        CVPipeResult<Mat> erodeDilateResult = erodeDilatePipe.apply(rotateImageResult.result);
        sumPipeNanosElapsed += erodeDilateResult.nanosElapsed;

        CVPipeResult<Mat> hsvPipeResult = hsvPipe.apply(erodeDilateResult.result);
        sumPipeNanosElapsed += hsvPipeResult.nanosElapsed;

        CVPipeResult<Mat> outputMatResult =
                outputMatPipe.apply(Pair.of(rawInputMat, hsvPipeResult.result));
        sumPipeNanosElapsed += outputMatResult.nanosElapsed;

        CVPipeResult<List<Contour>> findContoursResult = findContoursPipe.apply(hsvPipeResult.result);
        sumPipeNanosElapsed += findContoursResult.nanosElapsed;

        CVPipeResult<List<Contour>> speckleRejectResult =
                speckleRejectPipe.apply(findContoursResult.result);
        sumPipeNanosElapsed += speckleRejectResult.nanosElapsed;

        CVPipeResult<List<PotentialTarget>> groupContoursResult =
                groupContoursPipe.apply(speckleRejectResult.result);
        sumPipeNanosElapsed += groupContoursResult.nanosElapsed;

        CVPipeResult<List<PotentialTarget>> sortContoursResult =
                sortContoursPipe.apply(groupContoursResult.result);
        sumPipeNanosElapsed += sortContoursResult.nanosElapsed;

        CVPipeResult<List<TrackedTarget>> collect2dTargetsResult =
                collect2dTargetsPipe.apply(sortContoursResult.result);
        sumPipeNanosElapsed += collect2dTargetsResult.nanosElapsed;

        Pair<Mat, List<TrackedTarget>> drawingInput =
                Pair.of(outputMatResult.result, collect2dTargetsResult.result);

        CVPipeResult<Mat> draw2dCrosshairResult = draw2dCrosshairPipe.apply(drawingInput);
        sumPipeNanosElapsed += draw2dCrosshairResult.nanosElapsed;

        CVPipeResult<Mat> draw2dContoursResult = draw2dContoursPipe.apply(drawingInput);
        sumPipeNanosElapsed += draw2dContoursResult.nanosElapsed;

        // TODO: Implement all the things
        return new CVPipelineResult(
                sumPipeNanosElapsed,
                List.of(),
                new Frame(draw2dContoursResult.result, frame.frameStaticProperties));
    }
}
