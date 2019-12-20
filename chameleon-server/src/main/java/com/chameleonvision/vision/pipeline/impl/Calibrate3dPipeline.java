package com.chameleonvision.vision.pipeline.impl;

import com.chameleonvision.config.CameraCalibrationConfig;
import com.chameleonvision.vision.camera.CameraCapture;
import com.chameleonvision.vision.pipeline.CVPipeline;
import com.chameleonvision.vision.pipeline.impl.CVPipeline3dSettings;
import com.chameleonvision.vision.pipeline.impl.DriverVisionPipeline;
import edu.wpi.cscore.VideoMode;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Calibrate3dPipeline extends CVPipeline<DriverVisionPipeline.DriverPipelineResult, CVPipeline3dSettings> {

    private int checkerboardSquaresHigh = 6;
    private int checkerboardSquaresWide = 8;
    private MatOfPoint3f objP_ORIG;
    private MatOfPoint3f objP;// new MatOfPoint3f(checkerboardSquaresHigh + checkerboardSquaresWide, 3);//(checkerboardSquaresWide * checkerboardSquaresHigh, 3);
    private Size patternSize = new Size(checkerboardSquaresWide, checkerboardSquaresHigh);
    private Size imageSize;
    double checkerboardSquareSize = 1; // inches!
    private MatOfPoint2f calibrationOutput = new MatOfPoint2f();
    private List<Mat> objpoints = new ArrayList<>();
    private List<Mat> imgpoints = new ArrayList<>();

    public static double checkerboardSquareSizeUnits = 1.0;

    public static final int MIN_COUNT = 15;

    public Calibrate3dPipeline(CVPipeline3dSettings settings) {
        super(settings);

        objP_ORIG = new MatOfPoint3f();
        objP = new MatOfPoint3f();

        for(int i = 0; i < checkerboardSquaresHigh * checkerboardSquaresWide; i++) {
            objP_ORIG.push_back(new MatOfPoint3f(new Point3(i / checkerboardSquaresWide, i % checkerboardSquaresHigh, 0.0f)));
        }

        setObjectSize(checkerboardSquareSizeUnits);

        objpoints.forEach(Mat::release);
        imgpoints.forEach(Mat::release);
        objpoints.clear();
        imgpoints.clear();
    }

    private void setObjectSize(double size) {
        Core.multiply(objP_ORIG, new Scalar(new double[] { -1, -1, -1 }), objP);
    }

    private final Size windowSize = new Size(11, 11);
    private final Size zeroZone = new Size(-1, -1);
    private TermCriteria criteria = new TermCriteria(3, 30, 0.001); //(Imgproc.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 0.001)

    private int captureCount = 0;
    private boolean wantsSnapshot = false;

    public void takeSnapshot() {
        wantsSnapshot = true;
    }

    public int getCount() {
        return captureCount;
    }

    @Override
    public DriverVisionPipeline.DriverPipelineResult runPipeline(Mat inputMat) {

        // look for checkerboard
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_BGR2GRAY);
        var checkerboardFound = Calib3d.findChessboardCorners(inputMat, patternSize, calibrationOutput);



        if(!checkerboardFound) {
            Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_GRAY2BGR);
            putText(inputMat, captureCount);

            return new DriverVisionPipeline.DriverPipelineResult(null, inputMat, 0);
        }

//        System.out.println("[SolvePNP] checkerboard found!!");

        // cool we found a checkerboard
        // do corner subpixel
        Imgproc.cornerSubPix(inputMat, calibrationOutput, windowSize, zeroZone, criteria);

        // convert back to BGR
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_GRAY2BGR);
        // draw the chessboard
        Calib3d.drawChessboardCorners(inputMat, patternSize, calibrationOutput, true);
        putText(inputMat, captureCount);

        if(wantsSnapshot) {
            var mat = new MatOfPoint3f();
            calibrationOutput.copyTo(mat);
            this.objpoints.add(objP);
            imgpoints.add(mat);
            captureCount++;
            wantsSnapshot = false;
        }

        imageSize = new Size(inputMat.width(), inputMat.height());

        return new DriverVisionPipeline.DriverPipelineResult(null, inputMat, 0);
    }

    @Override
    public void initPipeline(CameraCapture camera) {
        super.initPipeline(camera);
        objpoints.clear();
        captureCount = 0;
    }

    private void putText(Mat inputImage, int count) {
        Imgproc.putText(inputImage, "Captured " + count + " images",
                new Point(35, 60),
                Core.FONT_HERSHEY_PLAIN, 3.0, new Scalar(0, 0, 255));
    }

    public boolean calibrate() {
        if (captureCount < MIN_COUNT - 1) return false;

        Mat cameraMatrix = new Mat();
        Mat distortionCoeffs = new Mat();
        List<Mat> rvecs = new ArrayList<>();
        List<Mat> tvecs = new ArrayList<>();

        try {
            Calib3d.calibrateCamera(objpoints, imgpoints, imageSize, cameraMatrix, distortionCoeffs, rvecs, tvecs);
        } catch(Exception e) {
            System.err.println("Camera calibration failed!");
            return false;
        }

        VideoMode currentVidMode = cameraCapture.getCurrentVideoMode();
        Size resolution = new Size(currentVidMode.width, currentVidMode.height);
        CameraCalibrationConfig cal = new CameraCalibrationConfig(resolution, cameraMatrix, distortionCoeffs);

        // TODO: (HIGH) save calibration data!

        System.out.printf("CALIBRATION SUCCESS! camMatrix: \n%s\ndistortionCoeffs:\n%s\n", cameraMatrix, distortionCoeffs);

        return true;
    }
}
