package com.chameleonvision.vision.pipeline;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import org.apache.commons.math3.util.FastMath;
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




    public static class CVPipeline3dResult extends CVPipelineResult<Target3d> {
        public CVPipeline3dResult(List<Target3d> targets, Mat outputMat, long processTime) {
            super(targets, outputMat, processTime);
        }
    }

    public static class Target3d extends CVPipeline2d.Target2d {
        public Pose2d cameraRelativePose;
    }
}
