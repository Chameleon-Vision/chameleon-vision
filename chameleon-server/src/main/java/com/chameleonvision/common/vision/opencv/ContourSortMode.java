package com.chameleonvision.common.vision.opencv;

import com.chameleonvision.common.vision.target.TrackedTarget;
import java.util.Comparator;
import org.apache.commons.math3.util.FastMath;

public enum ContourSortMode {
    Largest(Comparator.comparingDouble(TrackedTarget::getArea)),
    Smallest(Largest.getComparator().reversed()),
    Highest(Comparator.comparingDouble(rect -> rect.getMinAreaRect().center.y)),
    Lowest(Highest.getComparator().reversed()),
    Leftmost(Comparator.comparingDouble(target -> target.getMinAreaRect().center.x)),
    Rightmost(Leftmost.getComparator().reversed()),
    Centermost(
            Comparator.comparingDouble(
                    rect ->
                            (FastMath.pow(rect.getMinAreaRect().center.y, 2)
                                    + FastMath.pow(rect.getMinAreaRect().center.x, 2))));

    private Comparator<TrackedTarget> m_comparator;

    ContourSortMode(Comparator<TrackedTarget> comparator) {
        m_comparator = comparator;
    }

    public Comparator<TrackedTarget> getComparator() {
        return m_comparator;
    }
}
