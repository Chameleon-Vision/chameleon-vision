package com.chameleonvision.common.vision.pipe.impl;

import com.chameleonvision.common.vision.opencv.CVShape;
import com.chameleonvision.common.vision.opencv.Contour;
import com.chameleonvision.common.vision.opencv.ContourShape;
import com.chameleonvision.common.vision.pipe.CVPipe;
import java.util.List;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

public class FindShapesPipe
        extends CVPipe<List<Contour>, List<CVShape>, FindShapesPipe.FindShapesParams> {

    MatOfPoint2f approxCurve = new MatOfPoint2f();

    @Override
    protected List<CVShape> process(List<Contour> in) {
        approxCurve.release();
        approxCurve = new MatOfPoint2f();

        for (var contour : in) {

            if (params.desiredShape == ContourShape.Circle) {

            } else {
                int desiredSides = params.desiredShape.sides;
                Imgproc.approxPolyDP(contour.getMat2f(), approxCurve, params.approxEpsilon, true);

                //                int actualSides = approxCurve.
                //                switch ()
                System.out.println("fugg");
            }
        }
        return List.of();
    }

    public static class FindShapesParams {
        double approxEpsilon = 0.05;
        ContourShape desiredShape;
    }
}
