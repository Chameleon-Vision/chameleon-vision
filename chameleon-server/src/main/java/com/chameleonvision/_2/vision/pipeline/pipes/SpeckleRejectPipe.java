package com.chameleonvision._2.vision.pipeline.pipes;

import com.chameleonvision._2.vision.pipeline.Pipe;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

public class SpeckleRejectPipe implements Pipe<List<MatOfPoint>, List<MatOfPoint>> {

    private double minPercentOfAvg;

    private List<MatOfPoint> despeckledContours = new ArrayList<>();

    public SpeckleRejectPipe(double minPercentOfAvg) {
        this.minPercentOfAvg = minPercentOfAvg;
    }

    public void setConfig(double minPercentOfAvg) {
        this.minPercentOfAvg = minPercentOfAvg;
    }

    @Override
    public Pair<List<MatOfPoint>, Long> run(List<MatOfPoint> input) {
        long processStartNanos = System.nanoTime();

        despeckledContours.forEach(MatOfPoint::release);
        despeckledContours.clear();
        despeckledContours = new ArrayList<>();

        if (input.size() > 0) {
            double averageArea = 0.0;

            for (MatOfPoint c : input) {
                averageArea += Imgproc.contourArea(c);
            }

            averageArea /= input.size();

            double minAllowedArea = minPercentOfAvg / 100.0 * averageArea;

            for (MatOfPoint c : input) {
                if (Imgproc.contourArea(c) >= minAllowedArea) {
                    despeckledContours.add(c);
                }
            }
        }

        long processTime = System.nanoTime() - processStartNanos;
        return Pair.of(despeckledContours, processTime);
    }
}
