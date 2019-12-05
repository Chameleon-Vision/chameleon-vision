package com.chameleonvision.vision.pipeline;

import com.chameleonvision.Main;
import com.chameleonvision.util.MemoryManager;
import com.chameleonvision.vision.camera.CameraCapture;
import com.chameleonvision.vision.camera.CaptureStaticProperties;
import com.chameleonvision.vision.pipeline.pipes.*;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.*;

import java.util.*;

import static com.chameleonvision.vision.pipeline.CVPipeline3d.*;

public class CVPipeline3d extends CVPipeline<CVPipeline3dResult, CVPipeline3dSettings> {

    private Mat rawCameraMat = new Mat();

    private RotateFlipPipe rotateFlipPipe;
    private BlurPipe blurPipe;
    private ErodeDilatePipe erodeDilatePipe;
    private HsvPipe hsvPipe;
    private FindContoursPipe findContoursPipe;
    private FilterContoursPipe filterContoursPipe;
    private SpeckleRejectPipe speckleRejectPipe;
    private GroupContoursPipe groupContoursPipe;
    private SortContoursPipe sortContoursPipe;
    private SolvePNPPipe solvePNPPipe;
    private DrawSolvePNPPipe drawSolvePNPPipe;
    private Collect2dTargetsPipe collect2dTargetsPipe;
    private Draw2dContoursPipe.Draw2dContoursSettings draw2dContoursSettings;
    private Draw2dContoursPipe draw2dContoursPipe;
    private OutputMatPipe outputMatPipe;

    private String pipelineTimeString = "";
    private CaptureStaticProperties camProps;
    private Scalar hsvLower, hsvUpper;

    public CVPipeline3d() {
        super(new CVPipeline3dSettings());
    }

    public CVPipeline3d(String name) {
        super(name, new CVPipeline3dSettings());
    }

    public CVPipeline3d(CVPipeline3dSettings settings) {
        super(settings);
    }

    @Override
    public void initPipeline(CameraCapture process) {
        super.initPipeline(process);

        camProps = cameraCapture.getProperties().getStaticProperties();
        hsvLower = new Scalar(settings.hue.get(0).intValue(), settings.saturation.get(0).intValue(), settings.value.get(0).intValue());
        hsvUpper = new Scalar(settings.hue.get(1).intValue(), settings.saturation.get(1).intValue(), settings.value.get(1).intValue());

        rotateFlipPipe = new RotateFlipPipe(settings.rotationMode, settings.flipMode);
        blurPipe = new BlurPipe(5);
        erodeDilatePipe = new ErodeDilatePipe(settings.erode, settings.dilate, 7);
        hsvPipe = new HsvPipe(hsvLower, hsvUpper);
        findContoursPipe = new FindContoursPipe();
        filterContoursPipe = new FilterContoursPipe(settings.area, settings.ratio, settings.extent, camProps);
        speckleRejectPipe = new SpeckleRejectPipe(settings.speckle.doubleValue());
        groupContoursPipe = new GroupContoursPipe(settings.targetGroup, settings.targetIntersection);
        sortContoursPipe = new SortContoursPipe(settings.sortMode, camProps, 5);
        solvePNPPipe = new SolvePNPPipe(settings);
        drawSolvePNPPipe = new DrawSolvePNPPipe(settings);
        collect2dTargetsPipe = new Collect2dTargetsPipe(settings.calibrationMode, settings.point,
                settings.dualTargetCalibrationM, settings.dualTargetCalibrationB, camProps);
        draw2dContoursSettings = new Draw2dContoursPipe.Draw2dContoursSettings();
        // TODO: make settable from UI? config?
        draw2dContoursSettings.showCentroid = false;
        draw2dContoursSettings.showCrosshair = true;
        draw2dContoursSettings.boxOutlineSize = 2;
        draw2dContoursSettings.showRotatedBox = true;
        draw2dContoursSettings.showMaximumBox = true;
        draw2dContoursSettings.showMultiple = settings.multiple;

        draw2dContoursPipe = new Draw2dContoursPipe(draw2dContoursSettings, camProps);
        outputMatPipe = new OutputMatPipe(settings.isBinary);
    }

    private final MemoryManager memManager = new MemoryManager(120, 20000);

    @Override
    public CVPipeline3dResult runPipeline(Mat inputMat) {
        long totalPipelineTimeNanos = 0;
        long pipelineStartTimeNanos = System.nanoTime();

        if (cameraCapture == null) {
            throw new RuntimeException("Pipeline was not initialized before being run!");
        }

        // TODO (HIGH) find the source of the random NPE
        if (settings == null) {
            throw new RuntimeException("settings was not initialized!");
        }
        if (inputMat.cols() <= 1) {
            throw new RuntimeException("Input Mat is empty!");
        }

        pipelineTimeString = "";

        inputMat.copyTo(rawCameraMat);

        // prepare pipes
        camProps = cameraCapture.getProperties().getStaticProperties();
        hsvLower = new Scalar(settings.hue.get(0).intValue(), settings.saturation.get(0).intValue(), settings.value.get(0).intValue());
        hsvUpper = new Scalar(settings.hue.get(1).intValue(), settings.saturation.get(1).intValue(), settings.value.get(1).intValue());
        rotateFlipPipe.setConfig(settings.rotationMode, settings.flipMode);
        blurPipe.setConfig(0);
        erodeDilatePipe.setConfig(settings.erode, settings.dilate, 7);
        hsvPipe.setConfig(hsvLower, hsvUpper);
        filterContoursPipe.setConfig(settings.area, settings.ratio, settings.extent, camProps);
        speckleRejectPipe.setConfig(settings.speckle.doubleValue());
        groupContoursPipe.setConfig(settings.targetGroup, settings.targetIntersection);
        solvePNPPipe.setConfig(settings);
        sortContoursPipe.setConfig(settings.sortMode, camProps, settings.maxTargets);
        collect2dTargetsPipe.setConfig(settings.calibrationMode, settings.point,
                settings.dualTargetCalibrationM, settings.dualTargetCalibrationB, camProps);
        draw2dContoursPipe.setConfig(settings.multiple, camProps);
        outputMatPipe.setConfig(settings.isBinary);

        long pipeInitTimeNanos = System.nanoTime() - pipelineStartTimeNanos;

        // run pipes
        Pair<Mat, Long> rotateFlipResult = rotateFlipPipe.run(inputMat);
        totalPipelineTimeNanos += rotateFlipResult.getRight();

//        Pair<Mat, Long> blurResult = blurPipe.run(rotateFlipResult.getLeft());
//        totalPipelineTimeNanos += blurResult.getRight();

        Pair<Mat, Long> erodeDilateResult = erodeDilatePipe.run(rotateFlipResult.getLeft());
        totalPipelineTimeNanos += erodeDilateResult.getRight();

        Pair<Mat, Long> hsvResult = hsvPipe.run(erodeDilateResult.getLeft());
        totalPipelineTimeNanos += hsvResult.getRight();

        Pair<List<MatOfPoint>, Long> findContoursResult = findContoursPipe.run(hsvResult.getLeft());
        totalPipelineTimeNanos += findContoursResult.getRight();

        Pair<List<MatOfPoint>, Long> filterContoursResult = filterContoursPipe.run(findContoursResult.getLeft());
        totalPipelineTimeNanos += filterContoursResult.getRight();

        Pair<List<MatOfPoint>, Long> speckleRejectResult = speckleRejectPipe.run(filterContoursResult.getLeft());
        totalPipelineTimeNanos += speckleRejectResult.getRight();

        // group targets by single/dual
        Pair<List<RotatedRect>, Long> groupContoursResult = groupContoursPipe.run(speckleRejectResult.getLeft());
        totalPipelineTimeNanos += groupContoursResult.getRight();

        // sort the contours by "best-ness" as defined in the config
        Pair<List<RotatedRect>, Long> sortContoursResult = sortContoursPipe.run(groupContoursResult.getLeft());
        totalPipelineTimeNanos += sortContoursResult.getRight();

        // turn the rectangles into targets
        Pair<List<CVPipeline2d.Target2d>, Long> collect2dTargetsResult = collect2dTargetsPipe.run(Pair.of(sortContoursResult.getLeft(), camProps));
        totalPipelineTimeNanos += collect2dTargetsResult.getRight();

        // find the image corner points somehow. for now just the bounding box ig

        List<Pair<MatOfPoint2f, CVPipeline2d.Target2d>> list = new ArrayList<>();
            // find the corners based on the bounding box
            // order is left top, left bottom, right bottom, right top
        collect2dTargetsResult.getLeft().forEach(it -> {
            var mat = new MatOfPoint2f();

            // extract the corners
            var points = new Point[4];
            it.rawPoint.points(points);

            // find the tl/tr/bl/br corners
            // first, min by left/right
            Comparator<Point> comparator = Comparator.comparingDouble(point -> point.x);
            Comparator<Point> comparator2 = Comparator.comparingDouble(point -> point.y);
            var list_ =Arrays.asList(points);
            list_.sort(comparator);
            // of this, we now have left and right
            // sort to get top and bottom
            var left = List.of(list_.get(0), list_.get(1));
            left.sort(comparator2);
            var right = List.of(list_.get(2), list_.get(3));
            left.sort(comparator2);

            // tl tr bl br
            var tl = left.get(0);
            var tr = right.get(1);
            var bl = left.get(0);
            var br = right.get(1);

            mat.fromList(List.of(tl, bl, br, tr));
            list.add(Pair.of(mat, it));
            
        });

        // once we've sorted our targets, perform solvePNP. The number of "best targets" is limited by the above pipe
        Pair<List<Target3d>, Long> solvePNPResult = solvePNPPipe.run(list);
        totalPipelineTimeNanos += solvePNPResult.getRight();


        // takes pair of (Mat of original camera image (8UC3), Mat of HSV thresholded image(8UC1))
        Pair<Mat, Long> outputMatResult = outputMatPipe.run(Pair.of(rotateFlipResult.getLeft(), hsvResult.getLeft()));
        totalPipelineTimeNanos += outputMatResult.getRight();

        // draw the targets
        var draw3dContoursResult = drawSolvePNPPipe.run(Pair.of(outputMatResult.getLeft(), solvePNPResult.getRight()));


//        // takes pair of (Mat to draw on, List<RotatedRect> of sorted contours)
//        Pair<Mat, Long> draw2dContoursResult = draw2dContoursPipe.run(Pair.of(outputMatResult.getLeft(), sortContoursResult.getLeft()));
//        totalPipelineTimeNanos += draw2dContoursResult.getRight();

        if (Main.testMode) {
            pipelineTimeString += String.format("PipeInit: %.2fms, ", pipeInitTimeNanos / 1000000.0);
            pipelineTimeString += String.format("RotateFlip: %.2fms, ", rotateFlipResult.getRight() / 1000000.0);
//            pipelineTimeString += String.format("Blur: %.2fms, ", blurResult.getRight() / 1000000.0);
            pipelineTimeString += String.format("ErodeDilate: %.2fms, ", erodeDilateResult.getRight() / 1000000.0);
            pipelineTimeString += String.format("HSV: %.2fms, ", hsvResult.getRight() / 1000000.0);
            pipelineTimeString += String.format("FindContours: %.2fms, ", findContoursResult.getRight() / 1000000.0);
            pipelineTimeString += String.format("FilterContours: %.2fms, ", filterContoursResult.getRight() / 1000000.0);
            pipelineTimeString += String.format("SpeckleReject: %.2fms, ", speckleRejectResult.getRight() / 1000000.0);
            pipelineTimeString += String.format("GroupContours: %.2fms, ", groupContoursResult.getRight() / 1000000.0);
            pipelineTimeString += String.format("SortContours: %.2fms, ", sortContoursResult.getRight() / 1000000.0);
            pipelineTimeString += String.format("Collect2dTargets: %.2fms, ", collect2dTargetsResult.getRight() / 1000000.0);
            pipelineTimeString += String.format("OutputMat: %.2fms, ", outputMatResult.getRight() / 1000000.0);
//            pipelineTimeString += String.format("Draw2dContours: %.2fms, ", draw2dContoursResult.getRight() / 1000000.0);

            System.out.println(pipelineTimeString);
            double totalPipelineTimeMillis = totalPipelineTimeNanos / 1000000.0;
            double totalPipelineTimeFPS = 1.0 / (totalPipelineTimeMillis / 1000.0);
            double truePipelineTimeMillis = (System.nanoTime() - pipelineStartTimeNanos) / 1000000.0;
            double truePipelineFPS = 1.0 / (truePipelineTimeMillis / 1000.0);
            System.out.printf("Pipeline processed in %.3fms (%.2fFPS), ", totalPipelineTimeMillis, totalPipelineTimeFPS);
            System.out.printf("full pipeline run time was %.3fms (%.2fFPS)\n", truePipelineTimeMillis, truePipelineFPS);
        }

        memManager.run();

        return new CVPipeline3dResult(

        )

//        return new CVPipeline3dResult(collect2dTargetsResult.getLeft(), draw2dContoursResult.getLeft(), totalPipelineTimeNanos);
    }

    public static class CVPipeline3dResult extends CVPipelineResult<Target3d> {
        public CVPipeline3dResult(List<Target3d> targets, Mat outputMat, long processTime) {
            super(targets, outputMat, processTime);
        }
    }

    public static class Target3d extends CVPipeline2d.Target2d {
        public Pose2d cameraRelativePose;
        public Mat rVector;
        public Mat tVector;

        public Target3d(CVPipeline2d.Target2d target) {
            super.calibratedX = target.calibratedX;
            super.calibratedY = target.calibratedY;
            super.pitch = target.pitch;
            super.area = target.area;
            super.rawPoint = target.rawPoint;
            super.yaw = target.yaw;
        }
    }
}
