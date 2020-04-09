package com.chameleonvision.common.vision.opencv;

import com.chameleonvision.common.util.math.MathUtils;
import java.util.Comparator;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class Contour implements Releasable {

    public static final Comparator<Contour> SortByMomentsX =
            Comparator.comparingDouble(
                    (contour) -> contour.getMoments().get_m10() / contour.getMoments().get_m00());

    public final MatOfPoint mat;

    private Double area = Double.NaN;
    private RotatedRect minAreaRect = null;
    private Rect boundingRect = null;
    private Moments moments = null;

    public Contour(MatOfPoint mat) {
        this.mat = mat;
    }

    public double getArea() {
        if (Double.isNaN(area)) {
            area = Imgproc.contourArea(mat);
        }
        return area;
    }

    public RotatedRect getMinAreaRect() {
        if (minAreaRect == null) {
            MatOfPoint2f temp = new MatOfPoint2f();
            mat.convertTo(temp, CvType.CV_32F);
            minAreaRect = Imgproc.minAreaRect(temp);
            temp.release();
        }
        return minAreaRect;
    }

    public Rect getBoundingRect() {
        if (boundingRect == null) {
            boundingRect = Imgproc.boundingRect(mat);
        }
        return boundingRect;
    }

    public Moments getMoments() {
        if (moments == null) {
            moments = Imgproc.moments(mat);
        }
        return moments;
    }

    public Point getCenterPoint() {
        return getMinAreaRect().center;
    }

    public boolean isEmpty() {
        return mat.empty();
    }

    public boolean isIntersecting(
            Contour secondContour, ContourIntersectionDirection intersectionDirection) {
        boolean isIntersecting = false;

        if (intersectionDirection == ContourIntersectionDirection.None) {
            isIntersecting = true;
        } else {
            try {
                MatOfPoint2f intersectMatA = new MatOfPoint2f();
                MatOfPoint2f intersectMatB = new MatOfPoint2f();

                mat.convertTo(intersectMatA, CvType.CV_32F);
                secondContour.mat.convertTo(intersectMatB, CvType.CV_32F);

                RotatedRect a = Imgproc.fitEllipse(intersectMatA);
                RotatedRect b = Imgproc.fitEllipse(intersectMatB);
                double mA = MathUtils.toSlope(a.angle);
                double mB = MathUtils.toSlope(b.angle);
                double x0A = a.center.x;
                double y0A = a.center.y;
                double x0B = b.center.x;
                double y0B = b.center.y;
                double intersectionX = ((mA * x0A) - y0A - (mB * x0B) + y0B) / (mA - mB);
                double intersectionY = (mA * (intersectionX - x0A)) + y0A;
                double massX = (x0A + x0B) / 2;
                double massY = (y0A + y0B) / 2;
                switch (intersectionDirection) {
                    case Up:
                        if (intersectionY < massY) isIntersecting = true;
                        break;
                    case Down:
                        if (intersectionY > massY) isIntersecting = true;
                        break;
                    case Left:
                        if (intersectionX < massX) isIntersecting = true;
                        break;
                    case Right:
                        if (intersectionX > massX) isIntersecting = true;
                        break;
                }
                intersectMatA.release();
                intersectMatB.release();
            } catch (Exception e) {
                // defaults to false
            }
        }

        return isIntersecting;
    }

    // TODO: refactor to do "infinite" contours
    public static Contour groupContoursByIntersection(
            Contour firstContour, Contour secondContour, ContourIntersectionDirection intersection) {
        if (areIntersecting(firstContour, secondContour, intersection)) {
            return combineContours(firstContour, secondContour);
        } else {
            return null;
        }
    }

    public static boolean areIntersecting(
            Contour firstContour,
            Contour secondContour,
            ContourIntersectionDirection intersectionDirection) {
        return firstContour.isIntersecting(secondContour, intersectionDirection)
                || secondContour.isIntersecting(firstContour, intersectionDirection);
    }

    private static Contour combineContours(Contour... contours) {
        var points = new MatOfPoint();

        for (var contour : contours) {
            points.push_back(contour.mat);
        }

        var finalContour = new Contour(points);

        boolean contourEmpty = finalContour.isEmpty();
        return contourEmpty ? null : finalContour;
    }

    @Override
    public void release() {
        mat.release();
    }
}
