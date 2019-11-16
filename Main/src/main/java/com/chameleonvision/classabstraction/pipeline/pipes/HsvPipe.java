package com.chameleonvision.classabstraction.pipeline.pipes;

import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class HsvPipe implements Pipe<Mat, Mat> {

    private final Scalar hsvLower, hsvUpper;

    private Mat outputMat = new Mat();

    public HsvPipe(Scalar hsvLower, Scalar hsvUpper) {
        this.hsvLower = hsvLower;
        this.hsvUpper = hsvUpper;
    }

    @Override
    public Pair<Mat, Long> run(Mat input) {
        long processStartNanos = System.nanoTime();

        Imgproc.cvtColor(input, outputMat, Imgproc.COLOR_RGB2HSV, 3);

        Core.inRange(outputMat, hsvLower, hsvUpper, outputMat);

        long processTime = processStartNanos - System.nanoTime();
        Pair<Mat, Long> output = Pair.of(outputMat, processTime);
        outputMat.release();
        return output;
    }
}
