package com.chameleonvision.common.vision.pipeline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.chameleonvision.common.vision.camera.CaptureStaticProperties;
import com.chameleonvision.common.vision.target.TrackedTarget;
import org.apache.commons.math3.util.FastMath;

public class SortContoursPipe extends CVPipe<List<TrackedTarget>, List<TrackedTarget>,
    SortContoursPipe.SortContoursParams> {

    private List<TrackedTarget> m_sortedContours = new ArrayList<>();

    @Override
    protected List<TrackedTarget> process(List<TrackedTarget> in) {
        m_sortedContours.clear();
        if (in.size() > 0) {
            m_sortedContours.addAll(in);
            if (params.getSortMode() != SortMode.Centermost) {
                m_sortedContours.sort(params.getSortMode().getComparator());
            } else {
                m_sortedContours.sort(Comparator.comparingDouble(this::calcSquareCenterDistance));
            }
        }

        return new ArrayList<>(m_sortedContours.subList(0, Math.min(in.size(),
            params.getMaxTargets() - 1)));
    }

    private double calcSquareCenterDistance(TrackedTarget rect) {
        return FastMath.sqrt(FastMath.pow(params.getCamProperties().centerX - rect.minAreaRect.center.x, 2)
            + FastMath.pow(params.getCamProperties().centerY - rect.minAreaRect.center.y, 2));
    }


    public enum SortMode {
        Largest((rect1, rect2) -> Double.compare(rect2.minAreaRect.size.area(), rect1.minAreaRect.size.area())),
        Smallest(Largest.getComparator().reversed()),
        Highest(Comparator.comparingDouble(rect -> rect.minAreaRect.center.y)),
        Lowest(Highest.getComparator().reversed()),
        Leftmost(Comparator.comparingDouble(target -> target.minAreaRect.center.x)),
        Rightmost(Leftmost.getComparator().reversed()),
        Centermost(null);

        private Comparator<TrackedTarget> m_comparator;

        SortMode(Comparator<TrackedTarget> comparator) {
            m_comparator = comparator;
        }

        public Comparator<TrackedTarget> getComparator() {
            return m_comparator;
        }
    }

    public static class SortContoursParams {
        private SortMode m_sortMode;
        private CaptureStaticProperties m_camProperties;
        private int m_maxTargets;

        public SortContoursParams(SortMode sortMode, CaptureStaticProperties camProperties,
                                  int maxTargets) {
            m_sortMode = sortMode;
            m_camProperties = camProperties;
            m_maxTargets = maxTargets;
        }

        public SortMode getSortMode() {
            return m_sortMode;
        }

        public CaptureStaticProperties getCamProperties() {
            return m_camProperties;
        }

        public int getMaxTargets() {
            return m_maxTargets;
        }
    }
}
