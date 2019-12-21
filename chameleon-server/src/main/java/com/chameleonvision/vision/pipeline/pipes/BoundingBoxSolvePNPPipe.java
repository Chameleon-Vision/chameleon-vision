package com.chameleonvision.vision.pipeline.pipes;

import com.chameleonvision.config.CameraCalibrationConfig;
import com.chameleonvision.config.CameraJsonConfig;
import com.chameleonvision.vision.pipeline.Pipe;
import com.chameleonvision.vision.pipeline.impl.CVPipeline2d;
import com.chameleonvision.vision.pipeline.impl.StandardCVPipelineSettings;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.FastMath;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BoundingBoxSolvePNPPipe implements Pipe<List<CVPipeline2d.TrackedTarget>, List<CVPipeline2d.TrackedTarget>> {

    private Double tilt_angle;
    private MatOfPoint3f objPointsMat = new MatOfPoint3f();
    private Mat rVec = new Mat(), tVec = new Mat(), scaledTvec = new Mat();
    private Mat rodriguez = new Mat();
    private Mat pzero_world = new Mat();
    private MatOfPoint2f imagePoints = new MatOfPoint2f();
    private Mat cameraMatrix = new Mat();
    private MatOfDouble distortionCoefficients = new MatOfDouble();
    private List<CVPipeline2d.TrackedTarget> poseList = new ArrayList<>();

    public BoundingBoxSolvePNPPipe(StandardCVPipelineSettings settings, CameraCalibrationConfig calibration) {
        super();
        setCameraCoeffs(calibration);
        setObjectCorners(settings.targetCorners);
        this.tilt_angle = Math.toRadians(settings.cameraTiltAngleDeg);
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

    public void setConfig(StandardCVPipelineSettings settings, CameraCalibrationConfig camConfig) {
        setCameraCoeffs(camConfig);
        setObjectCorners(settings.targetCorners);
        tilt_angle = Math.toRadians(settings.cameraTiltAngleDeg);
    }

    private void setCameraCoeffs(CameraCalibrationConfig settings) {
        if(cameraMatrix != settings.getCameraMatrix()) {
            cameraMatrix.release();
            settings.getCameraMatrix().copyTo(cameraMatrix);
        }
        if(distortionCoefficients != settings.getDistortionCoefffs()) {
            distortionCoefficients.release();
            settings.getDistortionCoefffs().copyTo(distortionCoefficients);
        }
    }

    @Override
    public Pair<List<CVPipeline2d.TrackedTarget>, Long> run(List<CVPipeline2d.TrackedTarget> targets) {
        long processStartNanos = System.nanoTime();
        poseList.clear();
        for(var target: targets) {
            var corners = findBoundingBoxCorners(target);
            poseList.add(calculatePose(corners, target));
        }
        long processTime = System.nanoTime() - processStartNanos;
        return Pair.of(poseList, processTime);
    }

    private MatOfPoint2f findBoundingBoxCorners(CVPipeline2d.TrackedTarget target) {

//        List<Pair<MatOfPoint2f, CVPipeline2d.Target2d>> list = new ArrayList<>();
//        // find the corners based on the bounding box
//        // order is left top, left bottom, right bottom, right top
        var mat2f = new MatOfPoint2f();

        // extract the corners
        var points = new Point[4];
        target.rawPoint.points(points);

        // find the tl/tr/bl/br corners
        // first, min by left/right
        Comparator<Point> leftRightComparator = Comparator.comparingDouble(point -> point.x);
        Comparator<Point> verticalComparator = Comparator.comparingDouble(point -> point.y);

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
        var tr = right.get(1);
        var bl = left.get(0);
        var br = right.get(1);

        mat2f.fromList(List.of(tl, bl, br, tr));
        return mat2f;
    }

    @SuppressWarnings("FieldCanBeLocal")
    private Scalar scalar = new Scalar(new double[] { -1, -1, -1 });

    private CVPipeline2d.TrackedTarget calculatePose(MatOfPoint2f imageCornerPoints, CVPipeline2d.TrackedTarget target) {
        Calib3d.solvePnP(objPointsMat, imageCornerPoints, cameraMatrix, distortionCoefficients, rVec, tVec);

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
        var rot_inv = new Mat();
        Core.transpose(rodriguez, rot_inv);

        // This should be pzero_world = numpy.matmul(rot_inv, -tvec)
        Core.multiply(tVec, scalar, scaledTvec);
        pzero_world  = rot_inv.mul(scaledTvec);

        var angle2 = FastMath.atan2(pzero_world.get(0, 0)[0], pzero_world.get(2, 0)[0]);

        var targetAngle = -angle1; // radians
        var targetRotation = -angle2; // radians
        //noinspection UnnecessaryLocalVariable
        var targetDistance = distance; // meters or whatever the calibration was in

        var targetLocation = new Translation2d(targetDistance * FastMath.cos(targetAngle), targetDistance * FastMath.sin(targetAngle));
        target.cameraRelativePose = new Pose2d(targetLocation, new Rotation2d(targetRotation));
        target.rVector = rVec;
        target.tVector = tVec;
        return target;
    }

}
