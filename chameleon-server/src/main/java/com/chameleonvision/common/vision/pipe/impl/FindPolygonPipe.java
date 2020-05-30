package com.chameleonvision.common.vision.pipe.impl;

import com.chameleonvision.common.vision.opencv.CVShape;
import com.chameleonvision.common.vision.opencv.Contour;
import com.chameleonvision.common.vision.opencv.ContourShape;
import com.chameleonvision.common.vision.pipe.CVPipe;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;
import com.chameleonvision.common.vision.opencv.ContourShape;

public class FindPolygonPipe extends CVPipe<Contour, CVShape, FindPolygonPipe.FindPolygonPipeParams> {


    /**
     * Runs the process for the pipe.
     *
     * @param in Input for pipe processing.
     * @return Result of processing.
     */
    @Override
    protected CVShape process(Contour in) {
        MatOfPoint2f approx = new MatOfPoint2f();
        Imgproc.approxPolyDP(in.getMat2f(), approx,0.01 * Imgproc.arcLength(in.getMat2f(), true), true);

        int corners = (int)approx.size().width;

        switch(corners){
            case 0:
                return new CVShape(in, ContourShape.Circle);
            case 3:
                return new CVShape(in, ContourShape.Triangle);
            case 4:
                return new CVShape(in, ContourShape.Quadrilateral);
        }


        return new CVShape(in, ContourShape.Custom);
    }


    public static class FindPolygonPipeParams {

        public FindPolygonPipeParams(){}

    }
}



