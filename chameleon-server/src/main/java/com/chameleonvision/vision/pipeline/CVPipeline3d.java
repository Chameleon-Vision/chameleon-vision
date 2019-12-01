package com.chameleonvision.vision.pipeline;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;

import java.util.List;

import static com.chameleonvision.vision.pipeline.CVPipeline3d.*;

public class CVPipeline3d extends CVPipeline<CVPipeline3dResult, CVPipeline3dSettings> {


    protected CVPipeline3d(CVPipeline3dSettings settings) {
        super(settings);
    }

    CVPipeline3d() {
        super(new CVPipeline3dSettings());
    }


    private Mat rVec = new Mat();
    private Mat tVec = new Mat();
    private Mat rot = new Mat();
    private Mat b = new Mat();
    MatOfDouble distCoeffs;
    Mat camMatrix;
    Point[] points = new Point[4];

    @Override
    public CVPipeline3dResult runPipeline(Mat inputMat) {
        //        private StandardCVProcess.Pose2d solvePNP(RotatedRect rectangleImageLoc, List< Point3 > realWorldCoordinates) {

        // find the coordinates of the corners

        rectangleImageLoc.points(points);
        var imagePoints = new MatOfPoint2f(points);

        var objPointsMat = new MatOfPoint3f();
        objPointsMat.fromList(realWorldCoordinates);

        Calib3d.solvePnP(objPointsMat, imagePoints, camMatrix, distCoeffs, rVec, tVec);

        // Algorithm from team 5190 Green Hope Falcons

        var tilt_angle = 0.0; // TODO add to settings

        var x = tVec.get(0, 0)[0];
        var z = FastMath.sin(tilt_angle) * tVec.get(1, 0)[0] + tVec.get(2, 0)[0] *  FastMath.cos(tilt_angle);

        // distance in the horizontal plane between camera and target
        var distance = FastMath.sqrt(x * x + z  * z);

        // horizontal angle between center camera and target
        @SuppressWarnings("SuspiciousNameCombination")
        var angle1 = FastMath.atan2(x, z);

        Calib3d.Rodrigues(rVec, rot);
        var rot_inv = new Mat();
        Core.transpose(rot, rot_inv);

        // This should be pzero_world = numpy.matmul(rot_inv, -tvec)
        Core.multiply(tVec, new Scalar(-1.0, -1.0, -1.0, -1.0), b);

        var pzero_world  = rot_inv.mul(b);
        var angle2 = FastMath.atan2(pzero_world.get(0, 0)[0], pzero_world.get(2, 0)[0]);

        var targetAngle = -angle1; // radians
        var targetRotation = -angle2; // radians
        var targetDistance = distance; // meters or whatever the calibration was in

        var targetLocation = new StandardCVProcess.Translation2d(targetDistance * FastMath.cos(targetAngle), targetDistance * FastMath.sin(targetAngle));
        return new StandardCVProcess.Pose2d(targetLocation, targetRotation);
    }
    }


    public static class CVPipeline3dResult extends CVPipelineResult<Target3d> {
        public CVPipeline3dResult(List<Target3d> targets, Mat outputMat, long processTime) {
            super(targets, outputMat, processTime);
        }
    }

    public static class Target3d extends CVPipeline2d.Target2d {
        // TODO: (2.1) Define 3d-specific target data
    }
}
