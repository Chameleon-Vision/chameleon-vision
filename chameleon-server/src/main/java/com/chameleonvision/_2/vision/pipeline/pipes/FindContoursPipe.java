package com.chameleonvision._2.vision.pipeline.pipes;

import com.chameleonvision._2.vision.pipeline.Pipe;
import com.chameleonvision.common.vision.opencv.Contour;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FindContoursPipe implements Pipe<Mat, List<Contour>> {

    private List<MatOfPoint> foundContours = new ArrayList<>();

    public FindContoursPipe() {}

    @Override
    public Pair<List<Contour>, Long> run(Mat input) {
        long processStartNanos = System.nanoTime();

        foundContours.clear();

        Imgproc.findContours(input, foundContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);

        long processTime = System.nanoTime() - processStartNanos;
        return Pair.of(foundContours.stream().map(Contour::new).collect(Collectors.toList()), processTime);
    }
}
