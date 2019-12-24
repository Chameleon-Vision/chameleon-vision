package com.chameleonvision.vision.pipeline.pipes;

import com.chameleonvision.config.CameraCalibrationConfig;
import com.chameleonvision.util.Helpers;
import com.chameleonvision.vision.pipeline.Pipe;
import com.chameleonvision.vision.pipeline.impl.CVPipeline2d;
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
        setObjectBox(15.5, 6, 2);
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

//        var temp = new Vector<Point3>();
//        var size = 4;
//        temp.add(new Point3(-size, -size, 0));
//        temp.add(new Point3(-size, size, 0));
//        temp.add(new Point3(size, size, 0));
//        temp.add(new Point3(size, -size, 0));
//        temp.add(new Point3(-size, -size, size));
//        temp.add(new Point3(-size, size, size));
//        temp.add(new Point3(size, size, size));
//        temp.add(new Point3(size, -size, size));
//        boxCornerMat.fromList(temp);

        var yes = 4;

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
        var rows = boxCornerMat.rows();
        for(var it : targets.getRight()) {
            MatOfPoint2f imagePoints = new MatOfPoint2f();
            try {
                Calib3d.projectPoints(boxCornerMat, it.rVector, it.tVector, this.cameraMatrix, this.distortionCoefficients, imagePoints, new Mat() , 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            var pts = imagePoints.toList();

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
