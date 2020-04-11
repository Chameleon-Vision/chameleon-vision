package com.chameleonvision.common.vision.pipe.impl;

import java.awt.*;
import java.util.List;

import com.chameleonvision.common.util.ColorHelper;
import com.chameleonvision.common.vision.pipe.CVPipe;
import com.chameleonvision.common.vision.target.TrackedTarget;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

public class Draw3dTargetsPipe
        extends CVPipe<Pair<Mat, List<TrackedTarget>>, Mat,
        Draw3dTargetsPipe.Draw2dContoursParams> {

    private static MatOfPoint tempMat = new MatOfPoint();

    @Override
    protected Mat process(Pair<Mat, List<TrackedTarget>> in) {
        for (var target : in.getRight()) {
//            var poly = target.getApproximateBoundingPolygon();
//            poly.convertTo(tempMat, CvType.CV_32S);

//            target.m_mainContour.getMat2f()


//            var contour = target.m_mainContour.mat;
//            var ints = new MatOfInt();
//            Imgproc.convexHull(contour, ints);
//            var hull = Contour.convertIndexesToPoints(contour, ints);
            var pointMat = new MatOfPoint();
            pointMat.fromList(target.m_mainContour.getConvexHull().toList());
////            Imgproc.drawContours(in.getLeft(), List.of(pointMat), -1,
////                    ColorHelper.colorToScalar(Color.green), 5);

            var contour = target.m_mainContour;

            Imgproc.drawContours(in.getLeft(), List.of(pointMat), -1,
                    ColorHelper.colorToScalar(Color.green), 5);

            pointMat.release();
//
//            hull.release();
//            ints.release();

//            tempMat.fromList(target.getApproximateBoundingPolygon().toList());
//            Imgproc.drawContours(in.getLeft(), List.of(tempMat), -1,
//                    ColorHelper.colorToScalar(Color.green), 5);

            var corners = target.getTargetCorners();
            for (var corner : corners) {
                Imgproc.circle(in.getLeft(), corner, params.radius,
                        ColorHelper.colorToScalar(params.color), params.radius);
            }
        }

        return in.getLeft();
    }

    public static class Draw2dContoursParams {
        private final int radius = 3;
        private final Color color = Color.RED;
    }
}
