package com.chameleonvision.common.vision.pipeline.pipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.chameleonvision.common.util.math.MathUtils;
import com.chameleonvision.common.vision.pipeline.CVPipe;
import com.chameleonvision.common.vision.target.TrackedTarget;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class GroupContoursPipe extends CVPipe<List<MatOfPoint>, List<TrackedTarget>,
    GroupContoursPipe.GroupContoursParams> {

    private static final Comparator<MatOfPoint> sortByMomentsX =
        Comparator.comparingDouble(GroupContoursPipe::calcMomentsX);

    private MatOfPoint2f m_contourBuffer = new MatOfPoint2f();
    private List<TrackedTarget> m_groupedContours = new ArrayList<>();
    private MatOfPoint2f m_intersectMatA = new MatOfPoint2f();
    private MatOfPoint2f m_intersectMatB = new MatOfPoint2f();

    private static double calcMomentsX(MatOfPoint c) {
        Moments m = Imgproc.moments(c);
        return (m.get_m10() / m.get_m00());
    }

    @Override
    protected List<TrackedTarget> process(List<MatOfPoint> in) {
        m_groupedContours.clear();
        m_contourBuffer.release();

        if (in.size() > (params.getGroup().equals(TrackedTarget.TargetContourGrouping.Single) ? 0 : 1)) {
            List<MatOfPoint> sorted = new ArrayList<>(in);
            sorted.sort(sortByMomentsX);
            Collections.reverse(sorted);

            switch (params.getGroup()) {
                case Single:
                    in.forEach(c -> {
                        m_contourBuffer.fromArray(c.toArray());
                        if (m_contourBuffer.cols() != 0 && m_contourBuffer.rows() != 0) {
                            RotatedRect rect = Imgproc.minAreaRect(m_contourBuffer);
                            Rect boundingRect = Imgproc.boundingRect(m_contourBuffer);

                            // TODO: Create Target
                            // m_groupedContours.add(target);
                        }
                    });
                    break;
                case Dual:
                    for (int i = 0; i < in.size() - 1; i++) {
                        List<Point> finalContourList = new ArrayList<>(in.get(i).toList());

                        try {
                            MatOfPoint firstContour = in.get(i);
                            MatOfPoint secondContour = in.get(i + 1);

                            if (isIntersecting(firstContour, secondContour)) {
                                finalContourList.addAll(secondContour.toList());
                            } else {
                                finalContourList.clear();
                                continue;
                            }

                            m_intersectMatA.release();
                            m_intersectMatB.release();

                            m_contourBuffer.fromList(finalContourList);

                            if (m_contourBuffer.cols() != 0 && m_contourBuffer.rows() != 0) {
                                RotatedRect rect = Imgproc.minAreaRect(m_contourBuffer);
                                Rect boundingRect = Imgproc.boundingRect(m_contourBuffer);

                                // TODO: Create Target
                                // m_groupedContours.add(target);

                                firstContour.release();
                                secondContour.release();

                                i += 1;
                            }
                        } catch (IndexOutOfBoundsException e) {
                            finalContourList.clear();
                        }
                    }
                    break;
            }
        }
        return m_groupedContours;
    }

    private boolean isIntersecting(MatOfPoint contourOne, MatOfPoint contourTwo) {
        if (params.getIntersection().equals(TrackedTarget.TargetContourIntersection.None)) {
            return true;
        }

        try {
            m_intersectMatA.fromArray(contourOne.toArray());
            m_intersectMatB.fromArray(contourTwo.toArray());
            RotatedRect a = Imgproc.fitEllipse(m_intersectMatA);
            RotatedRect b = Imgproc.fitEllipse(m_intersectMatB);
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
            switch (params.getIntersection()) {
                case Up: {
                    if (intersectionY < massY) {
                        return true;
                    }
                    break;
                }
                case Down: {
                    if (intersectionY > massY) {
                        return true;
                    }

                    break;
                }
                case Left: {
                    if (intersectionX < massX) {

                        return true;
                    }
                    break;
                }
                case Right: {
                    if (intersectionX > massX) {
                        return true;
                    }
                    break;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static class GroupContoursParams {
        private TrackedTarget.TargetContourGrouping m_group;
        private TrackedTarget.TargetContourIntersection m_intersection;

        public GroupContoursParams(TrackedTarget.TargetContourGrouping group,
                                   TrackedTarget.TargetContourIntersection intersection) {
            m_group = group;
            m_intersection = intersection;
        }

        public TrackedTarget.TargetContourGrouping getGroup() {
            return m_group;
        }

        public TrackedTarget.TargetContourIntersection getIntersection() {
            return m_intersection;
        }
    }
}
