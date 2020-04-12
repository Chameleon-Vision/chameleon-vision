package com.chameleonvision.common.vision.pipe.impl;

import com.chameleonvision.common.calibration.CameraCalibrationCoefficients;
import com.chameleonvision.common.vision.pipe.CVPipe;
import com.chameleonvision.common.vision.target.TargetModel;
import com.chameleonvision.common.vision.target.TrackedTarget;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import java.util.List;
import org.apache.commons.math3.util.FastMath;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;

public class SolvePNPPipe
        extends CVPipe<List<TrackedTarget>, List<TrackedTarget>, SolvePNPPipe.SolvePNPPipeParams> {

    private MatOfPoint2f imagePoints = new MatOfPoint2f();

    @Override
    protected List<TrackedTarget> process(List<TrackedTarget> targetList) {
        for (var target : targetList) {
            calculateTargetPose(target);
        }
        return targetList;
    }

    private void calculateTargetPose(TrackedTarget target) {
        Pose2d targetPose;

        var corners = target.getTargetCorners();
        if (corners == null
                || corners.isEmpty()
                || params.cameraCoefficients.getCameraIntrinsicsMat() == null
                || params.cameraCoefficients.getCameraExtrinsicsMat() == null) {
            targetPose = new Pose2d();
            return;
        }
        this.imagePoints.fromList(corners);

        var rVec = new Mat();
        var tVec = new Mat();
        try {
            Calib3d.solvePnP(
                    params.targetModel.getRealWorldTargetCoordinates(),
                    imagePoints,
                    params.cameraCoefficients.getCameraIntrinsicsMat(),
                    params.cameraCoefficients.getCameraExtrinsicsMat(),
                    rVec,
                    tVec);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        target.setCameraRelativeTvec(tVec);
        target.setCameraRelativeRvec(rVec);

        targetPose = correctLocationForCameraPitch(tVec, rVec, params.cameraPitchAngle);

        target.setRobotRelativePose(targetPose);
    }

    Mat rotationMatrix = new Mat();
    Mat inverseRotationMatrix = new Mat();
    Mat pzeroWorld = new Mat();
    Mat kMat = new Mat();
    Mat scaledTvec;

    @SuppressWarnings("DuplicatedCode") // yes I know we have another solvePNP pipe
    private Pose2d correctLocationForCameraPitch(Mat tVec, Mat rVec, Rotation2d cameraPitchAngle) {
        // Algorithm from team 5190 Green Hope Falcons. Can also be found in Ligerbot's vision
        // whitepaper
        var tiltAngle = cameraPitchAngle.getRadians();

        // the left/right distance to the target, unchanged by tilt.
        var x = tVec.get(0, 0)[0];

        // Z distance in the flat plane is given by
        // Z_field = z cos theta + y sin theta.
        // Z is the distance "out" of the camera (straight forward).
        var zField =
                tVec.get(2, 0)[0] * FastMath.cos(tiltAngle) + tVec.get(1, 0)[0] * FastMath.sin(tiltAngle);

        Calib3d.Rodrigues(rVec, rotationMatrix);
        Core.transpose(rotationMatrix, inverseRotationMatrix);

        scaledTvec = matScale(tVec, -1);

        Core.gemm(inverseRotationMatrix, scaledTvec, 1, kMat, 0, pzeroWorld);
        scaledTvec.release();

        var angle2 = FastMath.atan2(pzeroWorld.get(0, 0)[0], pzeroWorld.get(2, 0)[0]);

        // target rotation is the rotation of the target relative to straight ahead. this number
        // should be unchanged if the robot purely translated left/right.
        var targetRotation = -angle2; // radians

        // We want a vector that is X forward and Y left.
        // We have a Z_field (out of the camera projected onto the field), and an X left/right.
        // so Z_field becomes X, and X becomes Y

        //noinspection SuspiciousNameCombination
        var targetLocation = new Translation2d(zField, -x);
        return new Pose2d(targetLocation, new Rotation2d(targetRotation));
    }

    /**
    * Element-wise scale a matrix by a given factor
    *
    * @param src the source matrix
    * @param factor by how much to scale each element
    * @return the scaled matrix
    */
    private static Mat matScale(Mat src, double factor) {
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        Scalar s = new Scalar(factor);
        Core.multiply(src, s, dst);
        return dst;
    }

    public static class SolvePNPPipeParams {
        private final CameraCalibrationCoefficients cameraCoefficients;
        private final Rotation2d cameraPitchAngle;
        private final TargetModel targetModel;

        public SolvePNPPipeParams(
                CameraCalibrationCoefficients cameraCoefficients,
                Rotation2d cameraPitchAngle,
                TargetModel targetModel) {
            this.cameraCoefficients = cameraCoefficients;
            this.cameraPitchAngle = cameraPitchAngle;
            this.targetModel = targetModel;
        }
    }
}
