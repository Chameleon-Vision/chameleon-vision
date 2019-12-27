package com.chameleonvision.vision.pipeline.pipes;

import com.chameleonvision.vision.camera.CaptureStaticProperties;
import com.chameleonvision.vision.enums.SortMode;
import com.chameleonvision.vision.pipeline.Pipe;
import com.chameleonvision.vision.pipeline.impl.CVPipeline2d;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.FastMath;
import org.opencv.core.RotatedRect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortContoursPipe implements Pipe<List<CVPipeline2d.TrackedTarget>, List<CVPipeline2d.TrackedTarget>> {

    private final Comparator<CVPipeline2d.TrackedTarget> SortByCentermostComparator = Comparator.comparingDouble(this::calcSquareCenterDistance);

    private static final Comparator<CVPipeline2d.TrackedTarget> SortByLargestComparator = (rect1, rect2) -> Double.compare(rect2.rawPoint.size.area(), rect1.rawPoint.size.area());
    private static final Comparator<CVPipeline2d.TrackedTarget> SortBySmallestComparator = SortByLargestComparator.reversed();

    private static final Comparator<CVPipeline2d.TrackedTarget> SortByHighestComparator = (rect1, rect2) -> Double.compare(rect2.rawPoint.center.y, rect1.rawPoint.center.y);
    private static final Comparator<CVPipeline2d.TrackedTarget> SortByLowestComparator = SortByHighestComparator.reversed();

    public static final Comparator<CVPipeline2d.TrackedTarget> SortByLeftmostComparator = Comparator.comparingDouble(target -> target.rawPoint.center.x);
    private static final Comparator<CVPipeline2d.TrackedTarget> SortByRightmostComparator = SortByLeftmostComparator.reversed();

    private SortMode sort;
    private CaptureStaticProperties camProps;
    private int maxTargets;

    private List<CVPipeline2d.TrackedTarget> sortedContours = new ArrayList<>();

    public SortContoursPipe(SortMode sort, CaptureStaticProperties camProps, int maxTargets) {
        this.sort = sort;
        this.camProps = camProps;
        this.maxTargets = maxTargets;
    }

    public void setConfig(SortMode sort, CaptureStaticProperties camProps, int maxTargets) {
        this.sort = sort;
        this.camProps = camProps;
        this.maxTargets = maxTargets;
    }

    @Override
    public Pair<List<CVPipeline2d.TrackedTarget>, Long> run(List<CVPipeline2d.TrackedTarget> input) {
        long processStartNanos = System.nanoTime();

        sortedContours.clear();

        if (input.size() > 0) {
            sortedContours.addAll(input.subList(0, Math.min(input.size(), maxTargets - 1)));

            switch (sort) {
                case Largest:
                    sortedContours.sort(SortByLargestComparator);
                    break;
                case Smallest:
                    sortedContours.sort(SortBySmallestComparator);
                    break;
                case Highest:
                    sortedContours.sort(SortByHighestComparator);
                    break;
                case Lowest:
                    sortedContours.sort(SortByLowestComparator);
                    break;
                case Leftmost:
                    sortedContours.sort(SortByLeftmostComparator);
                    break;
                case Rightmost:
                    sortedContours.sort(SortByRightmostComparator);
                    break;
                case Centermost:
                    sortedContours.sort(SortByCentermostComparator);
                    break;
                default:
                    break;
            }
        }

        long processTime = System.nanoTime() - processStartNanos;
        return Pair.of(sortedContours, processTime);
    }

    private double calcSquareCenterDistance(CVPipeline2d.TrackedTarget rect) {
        return FastMath.sqrt(FastMath.pow(camProps.centerX - rect.rawPoint.center.x, 2) + FastMath.pow(camProps.centerY - rect.rawPoint.center.y, 2));
    }
}
