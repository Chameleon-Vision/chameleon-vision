package com.chameleonvision.common.vision.pipe.impl;

import com.chameleonvision.common.vision.opencv.CVShape;
import com.chameleonvision.common.vision.opencv.Contour;
import com.chameleonvision.common.vision.opencv.ContourShape;
import com.chameleonvision.common.vision.pipe.CVPipe;
import org.opencv.core.*;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindPolygonPipe extends CVPipe<List<Contour>, List<CVShape>, FindPolygonPipe.FindPolygonPipeParams> {


    int maxCorners = Math.max(50, 1);
    MatOfPoint corners = new MatOfPoint();
    double qualityLevel = 0.01;
    double minDistance = 10;
    int blockSize = 3, gradientSize = 3;
    boolean useHarrisDetector = true;
    double k = 0.04;
    /**
     * Runs the process for the pipe.
     *
     * @param in Input for pipe processing.
     * @return Result of processing.
     */
    @Override
    protected List<CVShape> process(List<Contour> in) {
        List<CVShape> output = new ArrayList<>();

        for(Contour contour : in) output.add(getShape(contour));

        return output;
    }


    private CVShape getShape(Contour in){
        Mat out = new Mat();
        Imgproc.cornerHarris(in.mat,out ,  3, 3, 0.05);

        int corners = corners(in.mat);

        if(ContourShape.fromSides(corners) == null){
            return new CVShape(in, ContourShape.Custom);
        }
        switch(ContourShape.fromSides(corners)) {
            case Circle:
                return new CVShape(in, ContourShape.Circle);
            case Triangle:
                return new CVShape(in, ContourShape.Triangle);
            case Quadrilateral:
                return new CVShape(in, ContourShape.Quadrilateral);
        }

        return new CVShape(in, ContourShape.Custom);
    }


    private int corners(Mat frame){
        corners.release();
        Imgproc.goodFeaturesToTrack(frame, corners, maxCorners, qualityLevel, minDistance, new Mat(),
                blockSize, gradientSize, useHarrisDetector, k);

        return corners.rows();
    }

    public static class FindPolygonPipeParams {

        public FindPolygonPipeParams(){}

    }
}



