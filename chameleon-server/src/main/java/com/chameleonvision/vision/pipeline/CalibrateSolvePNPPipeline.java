package com.chameleonvision.vision.pipeline;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class CalibrateSolvePNPPipeline extends CVPipeline<DriverVisionPipeline.DriverPipelineResult, CVPipeline3dSettings> {

    private int checkerboardSquaresHigh = 9;
    private int checkerboardSquaresWide = 6;
    private Mat objP = new Mat();//(checkerboardSquaresWide * checkerboardSquaresHigh, 3);
    Size patternSize = new Size(checkerboardSquaresWide, checkerboardSquaresHigh);
    double checkerboardSquareSize = 1; // inches!
    private MatOfPoint2f calibrationOutput = new MatOfPoint2f();
    private List<Mat> objpoints = new ArrayList<>();
    private List<Mat> imgpoints = new ArrayList<>();

    public CalibrateSolvePNPPipeline(CVPipeline3dSettings settings) {
        super(settings);

        // init mat -- set it all to zero
        // start by looping over rows
        for(int i = 0; i < checkerboardSquaresHigh * checkerboardSquaresWide; i++) {
            for(int j = 0; j < 3; j++) {
                objP.put(i, j, 0);
            }
        }

        // the first column iterates through width once, and the second column is zero
        // we repeat this pattern hight times
        for(int i = 0; i < checkerboardSquaresHigh; i++) {
            // within this we incrament by width
            for(int j = 0; j < checkerboardSquaresWide; i++) {
                objP.put(i + j, 0, j);
                objP.put(i + j, 1, i);
            }
        }

    }

    private final Size windowSize = new Size(11, 11);
    private final Size zeroZone = new Size(-1, -1);
    private TermCriteria criteria = new TermCriteria(3, 30, 0.001); //(Imgproc.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 0.001)

    @Override
    public DriverVisionPipeline.DriverPipelineResult runPipeline(Mat inputMat) {

        // look for checkerboard
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_BGR2GRAY);
        var checkerboardFound = Calib3d.findChessboardCorners(inputMat, patternSize, calibrationOutput);
        if(!checkerboardFound) return new DriverVisionPipeline.DriverPipelineResult(null, inputMat, 0);

        // cool we found a checkerboard
        // do corner subpixel
        Imgproc.cornerSubPix(inputMat, calibrationOutput, windowSize, zeroZone, criteria);

        // draw the chessboard
        Calib3d.drawChessboardCorners(inputMat, patternSize, calibrationOutput, true);
        new DriverVisionPipeline.DriverPipelineResult(null, inputMat, 0);

        this.objpoints.add(objP);
        imgpoints.add(calibrationOutput);

        return null;
    }
}
