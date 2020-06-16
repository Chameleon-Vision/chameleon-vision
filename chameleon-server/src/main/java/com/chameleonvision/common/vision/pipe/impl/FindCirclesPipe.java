package com.chameleonvision.common.vision.pipe.impl;

import com.chameleonvision.common.vision.opencv.CVShape;
import com.chameleonvision.common.vision.opencv.Contour;
import com.chameleonvision.common.vision.opencv.ContourShape;
import com.chameleonvision.common.vision.pipe.CVPipe;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class FindCirclesPipe
        extends CVPipe<Pair<Mat, List<Contour>>, List<CVShape>, FindCirclesPipe.FindCirclePipeParams> {

    double[] c;
    Mat circles = new Mat();
    Moments mu;
    double x_center;
    double y_center;
    /**
    * Runs the process for the pipe.
    *
    * @param in Input for pipe processing.
    * @return Result of processing.
    */
    @Override
    protected List<CVShape> process(Pair<Mat, List<Contour>> in) {
        circles.release();
        List<CVShape> output = new ArrayList<>();

        Imgproc.HoughCircles(
                in.getLeft(),
                circles,
                Imgproc.HOUGH_GRADIENT,
                1.0,
                (double) in.getLeft().rows() / 10,
                30,
                10,
                10,
                0);
        for (int x = 0; x < circles.cols(); x++) {
            c = circles.get(0, x);
            x_center = c[0];
            y_center = c[1];
            for (Contour contour : in.getRight()) {
                mu = contour.getMoments();
                if (Math.abs(x_center - (mu.m10 / mu.m00)) <= 1
                        && Math.abs(y_center - (mu.m01 / mu.m00)) <= 1) {
                    output.add(new CVShape(contour, ContourShape.Circle));
                }
            }
        }

        return output;
    }

    public static class FindCirclePipeParams {
        public FindCirclePipeParams() {}
    }
}
