package com.chameleonvision.vision.pipeline.pipes;

import com.chameleonvision.config.CameraCalibrationConfig;
import com.chameleonvision.vision.pipeline.Pipe;
import com.chameleonvision.vision.pipeline.impl.StandardCVPipeline;
import com.chameleonvision.vision.pipeline.impl.StandardCVPipelineSettings;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.FastMath;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SolvePNPPipe implements Pipe<List<StandardCVPipeline.TrackedTarget>, List<StandardCVPipeline.TrackedTarget>> {

    private Double tilt_angle;
    private MatOfPoint3f objPointsMat = new MatOfPoint3f();
    private Mat rVec = new Mat();
    private Mat tVec = new Mat();
    private Mat rodriguez = new Mat();
    private Mat pzero_world = new Mat();
    private Mat cameraMatrix = new Mat();
    Mat rot_inv = new Mat();
    Mat kMat = new Mat();
    private MatOfDouble distortionCoefficients = new MatOfDouble();
    private List<StandardCVPipeline.TrackedTarget> poseList = new ArrayList<>();
    Comparator<Point> leftRightComparator = Comparator.comparingDouble(point -> point.x);
    Comparator<Point> verticalComparator = Comparator.comparingDouble(point -> point.y);
    private double distanceDivisor = 1.0;
    Mat scaledTvec = new Mat();

    public SolvePNPPipe(StandardCVPipelineSettings settings, CameraCalibrationConfig calibration, Rotation2d tilt) {
        super();
        setCameraCoeffs(calibration);
        setTarget(settings.targetWidth, settings.targetHeight);
        this.tilt_angle = tilt.getRadians();
    }

    public void setTarget(double targetWidth, double targetHeight) {
        // order is left top, left bottom, right bottom, right top

        List<Point3> corners = List.of(
                new Point3(-targetWidth / 2.0, targetHeight / 2.0, 0.0),
                new Point3(-targetWidth / 2.0, -targetHeight / 2.0, 0.0),
                new Point3(targetWidth / 2.0, -targetHeight / 2.0, 0.0),
                new Point3(targetWidth / 2.0, targetHeight / 2.0, 0.0)
        );
        setObjectCorners(corners);
    }

    public void setObjectCorners(List<Point3> objectCorners) {
        objPointsMat.release();
        objPointsMat = new MatOfPoint3f();
        objPointsMat.fromList(objectCorners);
    }

    public void setConfig(StandardCVPipelineSettings settings, CameraCalibrationConfig camConfig, Rotation2d tilt) {
        setCameraCoeffs(camConfig);
        setTarget(settings.targetWidth, settings.targetHeight);
        tilt_angle = tilt.getRadians();
    }

    private void setCameraCoeffs(CameraCalibrationConfig settings) {
        if(settings == null) {
            System.err.println("SolvePNP can only run on a calibrated resolution, and this one is not! Please calibrate to use solvePNP.");
            return;
        }
        if(cameraMatrix != settings.getCameraMatrixAsMat()) {
            cameraMatrix.release();
            settings.getCameraMatrixAsMat().copyTo(cameraMatrix);
        }
        if(distortionCoefficients != settings.getDistortionCoeffsAsMat()) {
            distortionCoefficients.release();
            settings.getDistortionCoeffsAsMat().copyTo(distortionCoefficients);
        }
        this.distanceDivisor = settings.squareSize;
    }

    @Override
    public Pair<List<StandardCVPipeline.TrackedTarget>, Long> run(List<StandardCVPipeline.TrackedTarget> targets) {
        long processStartNanos = System.nanoTime();
        poseList.clear();
        for(var target: targets) {
            var corners = (target.leftRightDualTargetPair != null) ? findCorner2019(target) : findBoundingBoxCorners(target);
            var pose = calculatePose(corners, target);
            if(pose != null) poseList.add(pose);
        }
        long processTime = System.nanoTime() - processStartNanos;
        return Pair.of(poseList, processTime);
    }

    private MatOfPoint2f findCorner2019(StandardCVPipeline.TrackedTarget target) {
        if(target.leftRightDualTargetPair == null) return null;

        var left = target.leftRightDualTargetPair.getLeft();
        var right = target.leftRightDualTargetPair.getRight();

        // flip if the "left" target is to the right
        if(left.x > right.x) {
            var temp = left;
            left = right;
            right = temp;
        }

        var points = new MatOfPoint2f();
        points.fromArray(
                new Point(left.x, left.y + left.height),
                new Point(left.x, left.y),
                new Point(right.x + right.width, right.y),
                new Point(right.x + right.width, right.y + right.height)
        );
        return points;
    }

    private MatOfPoint2f findCornerMinAreaRect(StandardCVPipeline.TrackedTarget target) {
        if(target.leftRightRotatedRect == null) return null;

        var centroid = target.minAreaRect.center;

        var left = target.leftRightRotatedRect.getLeft();
        var right = target.leftRightRotatedRect.getRight();

        // flip if the "left" target is to the right
        if(left.center.x > right.center.x) {
            var temp = left;
            left = right;
            right = temp;
        }

        var leftPoints = new Point[4];
        left.points(leftPoints);
        var rightPoints = new Point[4];
        right.points(rightPoints);
        ArrayList<Point> combinedList = new ArrayList<>(List.of(leftPoints));
        combinedList.addAll(List.of(rightPoints));

        // start looking in the top left quadrant
        Comparator<Point> distanceProvider = Comparator.comparingDouble((Point point) -> FastMath.sqrt(FastMath.pow(centroid.x - point.x, 2) + FastMath.pow(centroid.y - point.y, 2)));

        var tl = combinedList.stream().filter(point -> point.x < centroid.x && point.y < centroid.y).max(distanceProvider).get();
        var tr = combinedList.stream().filter(point -> point.x > centroid.x && point.y < centroid.y).max(distanceProvider).get();
        var bl = combinedList.stream().filter(point -> point.x < centroid.x && point.y > centroid.y).max(distanceProvider).get();
        var br = combinedList.stream().filter(point -> point.x > centroid.x && point.y > centroid.y).max(distanceProvider).get();

        boundingBoxResultMat.release();
        boundingBoxResultMat.fromList(List.of(tl, bl, br, tr));

        return boundingBoxResultMat;
    }

    private MatOfPoint2f findBoundingBoxCorners(StandardCVPipeline.TrackedTarget target) {

//        List<Pair<MatOfPoint2f, CVPipeline2d.Target2d>> list = new ArrayList<>();
//        // find the corners based on the bounding box
//        // order is left top, left bottom, right bottom, right top

        // extract the corners
        var points = new Point[4];
        target.minAreaRect.points(points);

        // find the tl/tr/bl/br corners
        // first, min by left/right
        var list_ = Arrays.asList(points);
        list_.sort(leftRightComparator);
        // of this, we now have left and right
        // sort to get top and bottom
        var left = new ArrayList<>(List.of(list_.get(0), list_.get(1)));
        left.sort(verticalComparator);
        var right = new ArrayList<>(List.of(list_.get(2), list_.get(3)));
        right.sort(verticalComparator);

        // tl tr bl br
        var tl = left.get(0);
        var bl = left.get(1);
        var tr = right.get(0);
        var br = right.get(1);

        boundingBoxResultMat.release();
        boundingBoxResultMat.fromList(List.of(tl, bl, br, tr));

        return boundingBoxResultMat;
    }

    MatOfPoint2f boundingBoxResultMat = new MatOfPoint2f();
    MatOfPoint2f goodFeaturesResultMat = new MatOfPoint2f();

    private Mat dstNorm = new Mat();
    private Mat dstNormScaled = new Mat();
    List<Point> tempCornerList = new ArrayList<>();

    /**
     * Find the corners in an image.
     * @param targetImage the image to find corners in.
     * @return the corners found in the image.
     */
    @Deprecated
    private List<Point> findCornerHarris(Mat targetImage) {

        // convert the image to greyscale
        var gray = new Mat();
        Imgproc.cvtColor(targetImage, gray, Imgproc.COLOR_BGR2GRAY);
        Mat dst = Mat.zeros(targetImage.size(), CvType.CV_8U);

        // constants
        final int blockSize = 2;
        final int apertureSize = 3;
        final double k = 0.04;
        final int threshold = 200;

        /// Detecting corners
        Imgproc.cornerHarris(gray, dst, blockSize, apertureSize, k);

        /// Normalizing
        Core.normalize(dst, dstNorm, 0, 255, Core.NORM_MINMAX);
        Core.convertScaleAbs(dstNorm, dstNormScaled);

        /// Drawing a circle around corners
        float[] dstNormData = new float[(int) (dstNorm.total() * dstNorm.channels())];
        dstNorm.get(0, 0, dstNormData);

        tempCornerList.clear();
        for (int i = 0; i < dstNorm.rows(); i++) {
            for (int j = 0; j < dstNorm.cols(); j++) {
                if ((int) dstNormData[i * dstNorm.cols() + j] > threshold) {
                    tempCornerList.add(new Point(j, i));
                }
            }
        }

        return tempCornerList;
    }

    @Deprecated
    private MatOfPoint2f findGoodFeaturesToTrack2019(StandardCVPipeline.TrackedTarget target, Mat srcImage) {

//        Imgproc.approxPolyDP(new MatOfPoint2f(max_contour.toArray()),approx,epsilon,true);

        // start by looking at the targets
        var leftRight = target.leftRightDualTargetPair;
        var reverse = (leftRight.getLeft().x < leftRight.getRight().x);
        var left =  reverse ? leftRight.getLeft() : leftRight.getRight();
        var right =  !reverse ? leftRight.getLeft() : leftRight.getRight();
        var boundingTl = left.tl();
        var boundingBr = right.br();

        var slightlyBiggerTl = new Point(
            Math.max(0, boundingTl.x - 5),
            Math.max(0, boundingTl.y - 5)
        );

        var slightlyBiggerBr = new Point(
            Math.min(srcImage.rows(), boundingBr.x + 5),
            Math.min(srcImage.cols(), boundingBr.y + 5)
        );
        var rect = new Rect(slightlyBiggerTl, slightlyBiggerBr);

        var croppedImage = srcImage.submat(rect);
        var corners = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(croppedImage, corners, 8,0.5,5);

        List<Point> cornerList = new ArrayList<>(corners.toList());
        if(cornerList.size() != 8 && cornerList.size() != 4) return null;
        cornerList.sort(leftRightComparator);

        cornerList = cornerList.stream().map(point ->
            new Point(point.x + slightlyBiggerTl.x, point.y + slightlyBiggerTl.y))
            .collect(Collectors.toList());

        // of these, we want the two leftmost and two rightmost points
        var left1 = cornerList.get(0);
        var left2 = cornerList.get(1);
        var right1 = cornerList.get(0);
        var right2 = cornerList.get(1);

        // TODO maximize distance from the center rather than naively assume the leftmost and rightmost
        // will have to do per quadrant

        var leftOrder = left1.y < left2.y;
        var rightOrder = right1.y < right2.y;

        var tl = leftOrder ? left1 : left2;
        var bl = !leftOrder ? left1 : left2;
        var tr = rightOrder ? right1 : right2;
        var br = !rightOrder ? right1 : right2;

        goodFeaturesResultMat.release();
        goodFeaturesResultMat.fromList(List.of(tl, bl, br, tr));

        return goodFeaturesResultMat;
    }

    private StandardCVPipeline.TrackedTarget calculatePose(MatOfPoint2f imageCornerPoints, StandardCVPipeline.TrackedTarget target) {
        if(objPointsMat.rows() != imageCornerPoints.rows() || cameraMatrix.rows() < 2 || distortionCoefficients.cols() < 4) {
            System.err.println("can't do solvePNP with invalid params!");
            return null;
        }

        imageCornerPoints.copyTo(target.imageCornerPoints);

        try {
            Calib3d.solvePnP(objPointsMat, imageCornerPoints, cameraMatrix, distortionCoefficients, rVec, tVec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // Algorithm from team 5190 Green Hope Falcons

//        var tilt_angle = 0.0; // TODO add to settings

        var x = tVec.get(0, 0)[0];
        var z = FastMath.sin(tilt_angle) * tVec.get(1, 0)[0] + tVec.get(2, 0)[0] *  FastMath.cos(tilt_angle);

        // distance in the horizontal plane between camera and target
        var distance = FastMath.sqrt(x * x + z  * z);

        // horizontal angle between center camera and target
        @SuppressWarnings("SuspiciousNameCombination")
        var angle1 = FastMath.atan2(x, z);

        Calib3d.Rodrigues(rVec, rodriguez);
        Core.transpose(rodriguez, rot_inv);

        // This should be pzero_world = numpy.matmul(rot_inv, -tvec)
//        pzero_world  = rot_inv.mul(matScale(tVec, -1));
        scaledTvec = matScale(tVec, -1);
        Core.gemm(rot_inv, scaledTvec, 1, kMat, 0, pzero_world);

        var angle2 = FastMath.atan2(pzero_world.get(0, 0)[0], pzero_world.get(2, 0)[0]);

        var targetAngle = -angle1; // radians
        var targetRotation = -angle2; // radians
        var targetDistance = distance * 25.4 / 1000d / distanceDivisor; // This should be meters

        var targetLocation = new Translation2d(targetDistance * FastMath.cos(targetAngle), targetDistance * FastMath.sin(targetAngle));
        target.cameraRelativePose = new Pose2d(targetLocation, new Rotation2d(targetRotation));
        target.rVector = rVec;
        target.tVector = tVec;

        return target;
    }

    /**
     * Element-wise scale a matrix by a given factor
     * @param src the source matrix
     * @param factor by how much to scale each element
     * @return the scaled matrix
     */
    public Mat matScale(Mat src, double factor) {
        Mat dst = new Mat(src.rows(),src.cols(),src.type());
        Scalar s = new Scalar(factor); // TODO check if we need to add more elements to this
        Core.multiply(src, s, dst);
        return dst;
    }

}
