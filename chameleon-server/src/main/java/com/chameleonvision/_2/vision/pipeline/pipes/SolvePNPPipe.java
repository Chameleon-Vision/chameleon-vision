package com.chameleonvision._2.vision.pipeline.pipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.chameleonvision._2.config.CameraCalibrationConfig;
import com.chameleonvision._2.vision.pipeline.Pipe;
import com.chameleonvision._2.vision.pipeline.impl.StandardCVPipeline;
import com.chameleonvision._2.vision.pipeline.impl.StandardCVPipelineSettings;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.FastMath;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

/**
 * Handles detecting target corners and calculating robot-relative pose.
 */
public class SolvePNPPipe implements Pipe<Pair<List<StandardCVPipeline.TrackedTarget>, Mat>,
        List<StandardCVPipeline.TrackedTarget>> {

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
  private List<StandardCVPipeline.TrackedTarget> targetList = new ArrayList<>();
  Comparator<Point> leftRightComparator = Comparator.comparingDouble(point -> point.x);
  Comparator<Point> verticalComparator = Comparator.comparingDouble(point -> point.y);
  private double distanceDivisor = 1.0;
  Mat scaledTvec = new Mat();
  MatOfPoint2f boundingBoxResultMat = new MatOfPoint2f();
  MatOfPoint2f polyOutput = new MatOfPoint2f();
  private Mat greyImg = new Mat();
  private double accuracyPercentage = 0.2;

  /**
   * @param settings    unused :bolb:
   * @param calibration the camera intrinsics and extrinsics
   * @param tilt        The pitch of the camera relative to horzontal. used to account for
   *                    distances in calculate pose
   */
  public SolvePNPPipe(StandardCVPipelineSettings settings, CameraCalibrationConfig calibration,
                      Rotation2d tilt) {
    super();
    setCameraCoeffs(calibration);
//        setBoundingBoxTarget(settings.targetWidth, settings.targetHeight);
    // TODO add proper year differentiation
    set2020Target(true);

    this.tilt_angle = tilt.getRadians();
  }

  public void set2020Target(boolean isHighGoal) {
    if (isHighGoal) {
      // tl, bl, br, tr is the order
      List<Point3> corners = List.of(

              new Point3(-19.625, 0, 0),
              new Point3(-9.819867, -17, 0),
              new Point3(9.819867, -17, 0),
              new Point3(19.625, 0, 0));
      setObjectCorners(corners);
    } else {
      setBoundingBoxTarget(7, 11);
    }
  }

  public void setBoundingBoxTarget(double targetWidth, double targetHeight) {
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

  public void setConfig(StandardCVPipelineSettings settings, CameraCalibrationConfig camConfig,
                        Rotation2d tilt) {
    setCameraCoeffs(camConfig);
//        setBoundingBoxTarget(settings.targetWidth, settings.targetHeight);
    // TODO add proper year differentiation
    tilt_angle = tilt.getRadians();
    this.objPointsMat = settings.targetCornerMat;
    this.accuracyPercentage = settings.accuracy.doubleValue();
  }

  private void setCameraCoeffs(CameraCalibrationConfig settings) {
    if (settings == null) {
      System.err.println("SolvePNP can only run on a calibrated resolution, and this one is not!" +
              " Please calibrate to use solvePNP.");
      return;
    }
    if (cameraMatrix != settings.getCameraMatrixAsMat()) {
      cameraMatrix.release();
      settings.getCameraMatrixAsMat().copyTo(cameraMatrix);
    }
    if (distortionCoefficients != settings.getDistortionCoeffsAsMat()) {
      distortionCoefficients.release();
      settings.getDistortionCoeffsAsMat().copyTo(distortionCoefficients);
    }
    this.distanceDivisor = settings.squareSize;
  }

  @Override
  public Pair<List<StandardCVPipeline.TrackedTarget>, Long> run(Pair<List<StandardCVPipeline.TrackedTarget>, Mat> imageTargetPair) {
    long processStartNanos = System.nanoTime();
    var targets = imageTargetPair.getLeft();
    var image = imageTargetPair.getRight();
    Imgproc.cvtColor(image, greyImg, Imgproc.COLOR_BGR2GRAY);
    targetList.clear();
    for (var target : targets) {
      MatOfPoint2f corners;
      // if it's a dual target use 2019, but default to 2020
      if (target.leftRightRotatedRect == null) {
        corners = find2020VisionTarget(target, accuracyPercentage);//, imageTargetPair.getRight
        // ()); //find2020VisionTarget(target);// (target.leftRightDualTargetPair != null) ?
        // findCorner2019(target) : findBoundingBoxCorners(target);
      } else {
        corners = findCorner2019(target);
      }
//            var corners = findCorner2019(target);
      if (corners == null) continue;

      // convert the corners into a Pose2d
      var pose = calculatePose(corners, target);
      targetList.add(pose); // TODO null check null poses. DO NOT ADD A NULL CHECK HERE, otherwise
      // the order will be wrong.
    }
    long processTime = System.nanoTime() - processStartNanos;
    return Pair.of(targetList, processTime);
  }

  /**
   * basically we split the target's two tapes, find the min area rectangle for each, and take
   * the outermost 4 corners out of the 2 rectangles
   *
   * @param target the target to use
   * @return the 4 outermost corners.
   */
  private MatOfPoint2f findCorner2019(StandardCVPipeline.TrackedTarget target) {
    if (target.leftRightDualTargetPair == null) return null;

    var left = target.leftRightDualTargetPair.getLeft();
    var right = target.leftRightDualTargetPair.getRight();

    // flip if the "left" target is to the right
    if (left.x > right.x) {
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

  MatOfPoint2f target2020ResultMat = new MatOfPoint2f();

  private double distanceBetween(Point a, Point b) {
    return FastMath.sqrt(FastMath.pow(a.x - b.x, 2) + FastMath.pow(a.y - b.y, 2));
  }

  /**
   * Find the target using the outermost tape corners and a 2020 target. Uses approxPolyDP to
   * approximate the target outline.
   *
   * @param target the target.
   * @return The four outermost tape corners.
   */
  private MatOfPoint2f find2020VisionTarget(StandardCVPipeline.TrackedTarget target,
                                            double accuracyPercentage) {
    if (target.rawContour.cols() < 1) return null;

    var centroid = target.minAreaRect.center;
    Comparator<Point> distanceProvider =
            Comparator.comparingDouble((Point point) -> FastMath.sqrt(FastMath.pow(centroid.x - point.x, 2) + FastMath.pow(centroid.y - point.y, 2)));

    // algorithm from team 4915

    // Contour perimeter
    var peri = Imgproc.arcLength(target.rawContour, true);
    // approximating a shape around the contours
    // Can be tuned to allow/disallow hulls
    // Approx is the number of vertices
    // Ramer–Douglas–Peucker algorithm
    // we want a number between 0 and 0.16 out of a percentage from 0 to 100
    // so take accuracy and divide by 600
    Imgproc.approxPolyDP(target.rawContour, polyOutput, accuracyPercentage / 600.0 * peri, true);

    var area = Imgproc.moments(polyOutput);

//        if (area.get_m00() < 200) {
//            return null;
//        }

    var polyList = polyOutput.toList();

    polyOutput.copyTo(target.approxPoly);

    // left top, left bottom, right bottom, right top
    var boundingBoxCorners = findBoundingBoxCorners(target).toList();

    try {

      // top left and top right are the poly corners closest to the bouding box tl and tr
      var tl = polyList.stream().min(Comparator.comparingDouble((Point p) -> distanceBetween(p,
              boundingBoxCorners.get(0)))).get();
      var tr = polyList.stream().min(Comparator.comparingDouble((Point p) -> distanceBetween(p,
              boundingBoxCorners.get(3)))).get();

      // bottom left and bottom right have to be in the correct quadrant and are the furthest
      // from the center
      var bl =
              polyList.stream().filter(point -> point.x < centroid.x && point.y > centroid.y).max(distanceProvider).get();
      var br =
              polyList.stream().filter(point -> point.x > centroid.x && point.y > centroid.y).max(distanceProvider).get();

//            polyList = new ArrayList<>(polyList);
//            polyList.removeAll(List.of(tl, tr, bl, br));
//
//            var tl2 = polyList.stream().min(Comparator.comparingDouble((Point p) ->
//            distanceBetween(p, boundingBoxCorners.get(0)))).get();
//            var tr2 = polyList.stream().min(Comparator.comparingDouble((Point p) ->
//            distanceBetween(p, boundingBoxCorners.get(3)))).get();
//
//            var bl2 = polyList.stream().filter(point -> point.x < centroid.x && point.y >
//            centroid.y).max(distanceProvider).get();
//            var br2 = polyList.stream().filter(point -> point.x > centroid.x && point.y >
//            centroid.y).max(distanceProvider).get();

      target2020ResultMat.release();
      target2020ResultMat.fromList(List.of(tl, bl, br, tr));//, tr2, br2, bl2, tl2));

      return target2020ResultMat;
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  /**
   * Find the target using the outermost tape corners and a dual target.
   *
   * @param target the target.
   * @return The four outermost tape corners.
   */
  private MatOfPoint2f findDualTargetCornerMinAreaRect(StandardCVPipeline.TrackedTarget target) {
    if (target.leftRightRotatedRect == null) return null;

    var centroid = target.minAreaRect.center;
    Comparator<Point> distanceProvider =
            Comparator.comparingDouble((Point point) -> FastMath.sqrt(FastMath.pow(centroid.x - point.x, 2) + FastMath.pow(centroid.y - point.y, 2)));

    var left = target.leftRightRotatedRect.getLeft();
    var right = target.leftRightRotatedRect.getRight();

    // flip if the "left" target is to the right
    if (left.center.x > right.center.x) {
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
    var tl =
            combinedList.stream().filter(point -> point.x < centroid.x && point.y < centroid.y).max(distanceProvider).get();
    var tr =
            combinedList.stream().filter(point -> point.x > centroid.x && point.y < centroid.y).max(distanceProvider).get();
    var bl =
            combinedList.stream().filter(point -> point.x < centroid.x && point.y > centroid.y).max(distanceProvider).get();
    var br =
            combinedList.stream().filter(point -> point.x > centroid.x && point.y > centroid.y).max(distanceProvider).get();

    boundingBoxResultMat.release();
    boundingBoxResultMat.fromList(List.of(tl, bl, br, tr));

    return boundingBoxResultMat;
  }

  /**
   * @param target the target to find the corners of.
   * @return the corners. left top, left bottom, right bottom, right top
   */
  private MatOfPoint2f findBoundingBoxCorners(StandardCVPipeline.TrackedTarget target) {
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

  MatOfPoint2f goodFeatureToTrackRetval = new MatOfPoint2f();

  private MatOfPoint2f refineCornersByBestTrack(MatOfPoint2f corners, Mat greyImg,
                                                StandardCVPipeline.TrackedTarget target) {

    MatOfPoint approxf1 = new MatOfPoint();
    var origCornerList = new ArrayList<>(corners.toList());
    approxf1.fromList(origCornerList.stream()
            .map(it -> new Point(it.x - target.boundingRect.x, it.y - target.boundingRect.y))
            .collect(Collectors.toList())
    );
    var croppedImage = greyImg.submat(target.boundingRect);

    Imgproc.goodFeaturesToTrack(croppedImage, approxf1, 0, 0.1, 5);

    // at this point corners is still unmodified so let's map it
    List<Point> tempList = new ArrayList<>();

    // shift all points back into global pose
    var reshiftedList =
            approxf1.toList().stream().map(it -> new Point(it.x + target.boundingRect.x,
                    it.y + target.boundingRect.y))
                    .collect(Collectors.toList());
    for (Point p : origCornerList) {
      // find the goodFeaturesToTrack corner closest to me
      var closestPoint =
              reshiftedList.stream().min(Comparator.comparingDouble(p_ -> distanceBetween(p_, p)));
      if (closestPoint.isEmpty()) {
        tempList.add(p);
        reshiftedList.remove(p);
      } else {
        tempList.add(closestPoint.get());
        reshiftedList.remove(closestPoint.get());
      }
    }

    goodFeatureToTrackRetval.fromList(tempList);
    return goodFeatureToTrackRetval;
  }

  // Set the needed parameters to find the refined corners
  Size winSize = new Size(4, 4);
  Size zeroZone = new Size(-1, -1); // we don't need a zero zone
  TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.COUNT, 90, 0.001);

  private boolean shouldRefineCorners = true;

  /**
   * Refine an estimated corner position using the cornerSubPixel algorithm.
   * <p>
   * TODO should this be here or before the points are chosen?
   *
   * @param corners the corners detected -- this mat is modified!
   * @param greyImg the image taken by the camera as color
   * @return the updated mat, same as the corner mat passed in.
   */
  private MatOfPoint2f refineCornerEstimateSubPix(MatOfPoint2f corners, Mat greyImg) {
    if (!shouldRefineCorners) return corners; // just return
    Imgproc.cornerSubPix(greyImg, corners, winSize, zeroZone, criteria);

    return corners;
  }

//    NetworkTableEntry tvecE = NetworkTableInstance.getDefault().getTable("SmartDashboard")
//    .getEntry("tvec");
//    NetworkTableEntry rvecE = NetworkTableInstance.getDefault().getTable("SmartDashboard")
//    .getEntry("rvec");

  /**
   * Calculate the pose of the vision target
   *
   * @param imageCornerPoints the corners we found.
   * @param target            the target to process, mutated.
   * @return the target, with the pose2d added to it.
   */
  public StandardCVPipeline.TrackedTarget calculatePose(MatOfPoint2f imageCornerPoints,
                                                        StandardCVPipeline.TrackedTarget target) {
    if (objPointsMat.rows() != imageCornerPoints.rows() || cameraMatrix.rows() < 2 || distortionCoefficients.cols() < 4) {
      System.err.println("can't do solvePNP with invalid params!");
      return null;
    }

    imageCornerPoints.copyTo(target.imageCornerPoints);

    try {
      Calib3d.solvePnP(objPointsMat, imageCornerPoints, cameraMatrix, distortionCoefficients,
              rVec, tVec);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

//        tvecE.setString(tVec.dump());
//        rvecE.setString(rVec.dump());

    // Algorithm from team 5190 Green Hope Falcons. Can also be found in Ligerbot's vision
    // whitepaper

    // the left/right distance to the target, unchanged by tilt. Inches
    var x = tVec.get(0, 0)[0];

    // Z distance in the flat plane is given by
    // Z_field = z cos theta + y sin theta.
    // Z is the distance "out" of the camera (straight forward). Inches.
    var z = tVec.get(2, 0)[0] * FastMath.cos(tilt_angle) + tVec.get(1, 0)[0] * FastMath.sin(tilt_angle);

    Calib3d.Rodrigues(rVec, rodriguez);
    Core.transpose(rodriguez, rot_inv); // rodrigurz.t()

    scaledTvec = matScale(tVec, -1);
    Core.gemm(rot_inv, scaledTvec, 1, kMat, 0, pzero_world);

    var angle2 = FastMath.atan2(pzero_world.get(0, 0)[0], pzero_world.get(2, 0)[0]);

    // target rotation is the rotation of the target relative to straight ahead. this number
    // should be unchanged if the robot purely translated left/right.
    var targetRotation = -angle2; // radians

    // We want a vector that is X forward and Y left.
    // We have a Z_field (out of the camera projected onto the field), and an X left/right.
    // so Z_field becomes X, and X becomes Y

    //noinspection SuspiciousNameCombination
    var targetLocation = new Translation2d(z, -x).times(25.4 / 1000d / distanceDivisor);
    target.cameraRelativePose = new Pose2d(targetLocation, new Rotation2d(targetRotation));
    target.rVector = rVec;
    target.tVector = tVec;

    return target;
  }

  /**
   * Element-wise scale a matrix by a given factor
   *
   * @param src    the source matrix
   * @param factor by how much to scale each element
   * @return the scaled matrix
   */
  public Mat matScale(Mat src, double factor) {
    Mat dst = new Mat(src.rows(), src.cols(), src.type());
    Scalar s = new Scalar(factor); // TODO check if we need to add more elements to this
    Core.multiply(src, s, dst);
    return dst;
  }

}
