package com.chameleonvision.vision.pipeline.pipes;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.FastMath;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;

import java.util.ArrayList;
import java.util.List;

public class SolvePNPPipe implements Pipe<List<MatOfPoint2f>, List<Pose2d>> {

    private MatOfPoint3f objPointsMat = new MatOfPoint3f();
    private Mat rVec = new Mat(), tVec = new Mat();
    private Mat rodriguez = new Mat();
    Mat pzero_world = new Mat();
    private MatOfPoint2f imagePoints = new MatOfPoint2f();
    private Mat cameraMatrix = new Mat();
    private MatOfDouble distortionCoefficients = new MatOfDouble();
    private List<Pose2d> poseList = new ArrayList<>();

    public void setObjectCorners(List<Point3> objectCorners) {
        objPointsMat.release();
        objPointsMat = new MatOfPoint3f();
        objPointsMat.fromList(objectCorners);
    }

    /**
     * Set the camera calibration. Passed values won't be released
     * @param cameraMat the camera matrix
     * @param distCoeffs the distortion coefficient
     */
    public void setCameraCoeffs(Mat cameraMat, MatOfDouble distCoeffs) {
        cameraMatrix.release();
        cameraMat.copyTo(cameraMatrix);
        distortionCoefficients.release();
        distCoeffs.copyTo(distortionCoefficients);
    }

    @Override
    public Pair<List<Pose2d>, Long> run(List<MatOfPoint2f> objectCornerPoints) {
        long processStartNanos = System.nanoTime();
        poseList.clear();
        for(var corner: objectCornerPoints) {
            poseList.add(calculatePose(corner));
        }
        long processTime = System.nanoTime() - processStartNanos;
        return Pair.of(poseList, processTime);
    }

    private Pose2d calculatePose(MatOfPoint2f imageCornerPoints) {
        Calib3d.solvePnP(objPointsMat, imageCornerPoints, cameraMatrix, distortionCoefficients, rVec, tVec);

        // Algorithm from team 5190 Green Hope Falcons

        var tilt_angle = 0.0; // TODO add to settings

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
        Core.multiply(tVec, new Scalar(tVec.cols()), tVec);
        pzero_world  = rot_inv.mul(tVec);

        var angle2 = FastMath.atan2(pzero_world.get(0, 0)[0], pzero_world.get(2, 0)[0]);

        var targetAngle = -angle1; // radians
        var targetRotation = -angle2; // radians
        //noinspection UnnecessaryLocalVariable
        var targetDistance = distance; // meters or whatever the calibration was in

        var targetLocation = new Translation2d(targetDistance * FastMath.cos(targetAngle), targetDistance * FastMath.sin(targetAngle));
        return new Pose2d(targetLocation, new Rotation2d(targetRotation));
    }

}
