package com.chameleonvision.common.vision.pipe.impl;

import java.awt.*;
import java.util.List;

import com.chameleonvision.common.util.ColorHelper;
import com.chameleonvision.common.vision.pipe.CVPipe;
import com.chameleonvision.common.vision.target.TrackedTarget;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

public class Draw3dTargetsPipe
        extends CVPipe<Pair<Mat, List<TrackedTarget>>, Mat,
        Draw3dTargetsPipe.Draw2dContoursParams> {

    private static MatOfPoint tempMat = new MatOfPoint();

    @Override
    protected Mat process(Pair<Mat, List<TrackedTarget>> in) {
        for (var target : in.getRight()) {

            // draw convex hull
            var pointMat = new MatOfPoint();
            target.m_mainContour.getConvexHull().convertTo(pointMat, CvType.CV_32S);
            Imgproc.drawContours(in.getLeft(), List.of(pointMat), -1,
                    ColorHelper.colorToScalar(Color.green), 1);

            // draw approximate polygon
            var poly = target.getApproximateBoundingPolygon();
            poly.convertTo(pointMat, CvType.CV_32S);
            System.out.println("approx poly:\n" + poly.toList() + "\npoint mat:\n" + pointMat.toList() + "\n");
            Imgproc.drawContours(in.getLeft(), List.of(pointMat), -1,
                    ColorHelper.colorToScalar(Color.blue), 2);

            pointMat.release();

            // draw corners
            var corners = target.getTargetCorners();
            for (var corner : corners) {
                Imgproc.circle(in.getLeft(), corner, params.radius,
                        ColorHelper.colorToScalar(params.color), params.radius);
            }
        }

        return in.getLeft();
    }

    public static class Draw2dContoursParams {
        private final int radius = 2;
        private final Color color = Color.RED;
    }
}
