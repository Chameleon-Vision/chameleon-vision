package com.chameleonvision.vision.pipeline.pipes;

import com.chameleonvision.util.Helpers;
import com.chameleonvision.vision.pipeline.CVPipeline3d;
import com.chameleonvision.vision.pipeline.CVPipeline3dSettings;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.util.List;

public class DrawSolvePNPPipe implements Pipe<Pair<Mat, List<CVPipeline3d.Target3d>>, Mat> {

    private MatOfPoint3f boxCornerMat = new MatOfPoint3f();

    public Scalar color = Helpers.colorToScalar(Color.GREEN);

    public DrawSolvePNPPipe(CVPipeline3dSettings settings) {
        setConfig(settings.cameraMatrix, settings.cameraDistortionCoefficients);
    }

    public void setObjectBox(double targetWidth, double targetHeight, double targetDepth) {
        // implementation from 5190 Green Hope Falcons

        boxCornerMat.release();
        boxCornerMat = new MatOfPoint3f();
        boxCornerMat.put(0, 0, -targetWidth/2);
        boxCornerMat.put(0, 0, -targetHeight / 2);
        boxCornerMat.put(0, 0, 0);

        boxCornerMat.put(0, 0, -targetWidth/2);
        boxCornerMat.put(0, 0, targetHeight / 2);
        boxCornerMat.put(0, 0, 0);

        boxCornerMat.put(0, 0, targetWidth/2);
        boxCornerMat.put(0, 0, targetHeight / 2);
        boxCornerMat.put(0, 0, 0);

        boxCornerMat.put(0, 0, targetWidth/2);
        boxCornerMat.put(0, 0, -targetHeight / 2);
        boxCornerMat.put(0, 0, 0);

        boxCornerMat.put(0, 0, -targetWidth/2);
        boxCornerMat.put(0, 0, -targetHeight / 2);
        boxCornerMat.put(0, 0, -targetDepth/2);

        boxCornerMat.put(0, 0, -targetWidth/2);
        boxCornerMat.put(0, 0, targetHeight / 2);
        boxCornerMat.put(0, 0, -targetDepth/2);

        boxCornerMat.put(0, 0, targetWidth/2);
        boxCornerMat.put(0, 0, targetHeight / 2);
        boxCornerMat.put(0, 0, -targetDepth/2);

        boxCornerMat.put(0, 0, targetWidth/2);
        boxCornerMat.put(0, 0, -targetHeight / 2);
        boxCornerMat.put(0, 0, -targetDepth/2);
    }

    private Mat cameraMatrix = new Mat();
    private MatOfDouble distortionCoefficients = new MatOfDouble();

    public void setConfig(Mat cameraMatrix, Mat distortionMatrix) {
        if(cameraMatrix != this.cameraMatrix) {
            cameraMatrix.release();
            cameraMatrix.copyTo(this.cameraMatrix);
        }
        if(distortionMatrix != this.distortionCoefficients) {
            distortionCoefficients.release();
            distortionMatrix.copyTo(this.distortionCoefficients);
        }
    }

    @Override
    public Pair<Mat, Long> run(Pair<Mat, List<CVPipeline3d.Target3d>> targets) {
        long processStartNanos = System.nanoTime();

        var image = targets.getLeft();
        var rows = boxCornerMat.rows();
        for(var it : targets.getRight()) {
            MatOfPoint2f imagePoints = new MatOfPoint2f();
            Calib3d.projectPoints(boxCornerMat, it.rVector, it.tVector, this.cameraMatrix, this.distortionCoefficients, imagePoints, new Mat() , 0);
            var pts = imagePoints.toList();
            for(int i = 0; i < rows; i++) {
                Imgproc.line(image, pts.get(i), pts.get((i+1)%4), color, 2);
                Imgproc.line(image, pts.get(i+4), pts.get(4+(i+1)%4), color, 2);
                Imgproc.line(image, pts.get(i), pts.get(i+4), color, 2);
            }
        }

        long processTime = System.nanoTime() - processStartNanos;
        return Pair.of(image, processTime);
    }
}
