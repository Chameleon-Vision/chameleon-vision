package com.chameleonvision.common.vision.opencv;

import com.chameleonvision.common.vision.target.TrackedTarget;
import java.util.Comparator;

public enum ContourSortMode {
    Largest(
            (rect1, rect2) ->
                    Double.compare(rect2.getMinAreaRect().size.area(), rect1.getMinAreaRect().size.area())),
    Smallest(Largest.getComparator().reversed()),
    Highest(Comparator.comparingDouble(rect -> rect.getMinAreaRect().center.y)),
    Lowest(Highest.getComparator().reversed()),
    Leftmost(Comparator.comparingDouble(target -> target.getMinAreaRect().center.x)),
    Rightmost(Leftmost.getComparator().reversed()),
    Centermost(null);

    private Comparator<TrackedTarget> m_comparator;

    ContourSortMode(Comparator<TrackedTarget> comparator) {
        m_comparator = comparator;
    }

    public Comparator<TrackedTarget> getComparator() {
        return m_comparator;
    }
}
