package com.chameleonvision.vision.pipeline.pipes;

import com.chameleonvision.config.CameraCalibrationConfig;
import com.chameleonvision.util.Helpers;
import com.chameleonvision.vision.pipeline.Pipe;
import com.chameleonvision.vision.pipeline.impl.CVPipeline2d;
import com.chameleonvision.vision.pipeline.impl.StandardCVPipelineSettings;
import edu.wpi.first.wpilibj.util.Units;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import javax.management.remote.TargetedNotification;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class DrawSolvePNPPipe implements Pipe<Pair<Mat, List<CVPipeline2d.TrackedTarget>>, Mat> {

    private MatOfPoint3f boxCornerMat = new MatOfPoint3f();

    public Scalar color = Helpers.colorToScalar(Color.GREEN);

    public DrawSolvePNPPipe(CameraCalibrationConfig settings) {
        setConfig(settings);
        setObjectBox(14.5, 6, 2);
    }

    public void setObjectBox(double targetWidth, double targetHeight, double targetDepth) {
        // implementation from 5190 Green Hope Falcons

        boxCornerMat.release();
        boxCornerMat = new MatOfPoint3f(
                new Point3(-targetWidth/2d, -targetHeight/2d, 0),
                new Point3(-targetWidth/2d, targetHeight/2d, 0),
                new Point3(targetWidth/2d, targetHeight/2d, 0),
                new Point3(targetWidth/2d, -targetHeight/2d, 0),
                new Point3(-targetWidth/2d, -targetHeight/2d, -targetDepth),
                new Point3(-targetWidth/2d, targetHeight/2d, -targetDepth),
                new Point3(targetWidth/2d, targetHeight/2d, -targetDepth),
                new Point3(targetWidth/2d, -targetHeight/2d, -targetDepth)
        );
    }

    private Mat cameraMatrix = new Mat();
    private MatOfDouble distortionCoefficients = new MatOfDouble();

    public void setConfig(CameraCalibrationConfig config) {
        if(config == null) {
            System.err.println("got passed a null config! Returning...");
            return;
        }
        setConfig(config.getCameraMatrixAsMat(), config.getDistortionCoeffsAsMat());
    }

    public void setConfig(Mat cameraMatrix_, MatOfDouble distortionMatrix_) {
        this.cameraMatrix = cameraMatrix_;
        this.distortionCoefficients = distortionMatrix_;
    }

    @Override
    public Pair<Mat, Long> run(Pair<Mat, List<CVPipeline2d.TrackedTarget>> targets) {
        long processStartNanos = System.nanoTime();

        var image = targets.getLeft();
        for(var it : targets.getRight()) {
            MatOfPoint2f imagePoints = new MatOfPoint2f();
            try {
                Calib3d.projectPoints(boxCornerMat, it.rVector, it.tVector, this.cameraMatrix, this.distortionCoefficients, imagePoints, new Mat() , 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            var pts = imagePoints.toList();

            // draw left and right targets if possible
            if(it.leftRightTarget2019 != null) {
                var left = it.leftRightTarget2019.getLeft();
                var right = it.leftRightTarget2019.getRight();
                Imgproc.rectangle(image, left.tl(), left.br(), new Scalar(200, 200, 0), 4);
                Imgproc.rectangle(image, right.tl(), right.br(), new Scalar(200, 200, 0), 2);
            }

            // draw corners
            for(int i = 0; i < it.imageCornerPoints.rows(); i++) {
                var point = new Point(it.imageCornerPoints.get(i, 0));
                Imgproc.circle(image, point, 4, new Scalar(0, 255, 0), 5);
            }

            // sketch out floor
            Imgproc.line(image, pts.get(0), pts.get(1), new Scalar(0, 255, 0), 3);
            Imgproc.line(image, pts.get(1), pts.get(2), new Scalar(0, 255, 0), 3);
            Imgproc.line(image, pts.get(2), pts.get(3), new Scalar(0, 255, 0), 3);
            Imgproc.line(image, pts.get(3), pts.get(0), new Scalar(0, 255, 0), 3);

            // draw pillars
            Imgproc.line(image, pts.get(0), pts.get(4), new Scalar(255, 0, 0), 3);
            Imgproc.line(image, pts.get(1), pts.get(5), new Scalar(255, 0, 0), 3);
            Imgproc.line(image, pts.get(2), pts.get(6), new Scalar(255, 0, 0), 3);
            Imgproc.line(image, pts.get(3), pts.get(7), new Scalar(255, 0, 0), 3);

            // draw top
            Imgproc.line(image, pts.get(4), pts.get(5), new Scalar(0, 0, 255), 3);
            Imgproc.line(image, pts.get(5), pts.get(6), new Scalar(0, 0, 255), 3);
            Imgproc.line(image, pts.get(6), pts.get(7), new Scalar(0, 0, 255), 3);
            Imgproc.line(image, pts.get(7), pts.get(4), new Scalar(0, 0, 255), 3);
        }

        long processTime = System.nanoTime() - processStartNanos;
        return Pair.of(image, processTime);
    }
}
