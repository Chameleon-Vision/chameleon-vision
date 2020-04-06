package com.chameleonvision.common.vision.pipe.impl;

import com.chameleonvision.common.vision.frame.FrameStaticProperties;
import com.chameleonvision.common.vision.opencv.ContourSortMode;
import com.chameleonvision.common.vision.pipe.CVPipe;
import com.chameleonvision.common.vision.target.TrackedTarget;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.math3.util.FastMath;

public class SortContoursPipe
        extends CVPipe<List<TrackedTarget>, List<TrackedTarget>, SortContoursPipe.SortContoursParams> {

    private List<TrackedTarget> m_sortedContours = new ArrayList<>();

    @Override
    protected List<TrackedTarget> process(List<TrackedTarget> in) {
        m_sortedContours.clear();
        if (in.size() > 0) {
            m_sortedContours.addAll(in);
            if (params.getSortMode() != ContourSortMode.Centermost) {
                m_sortedContours.sort(params.getSortMode().getComparator());
            } else {
                m_sortedContours.sort(Comparator.comparingDouble(this::calcSquareCenterDistance));
            }
        }

        return new ArrayList<>(
                m_sortedContours.subList(0, Math.min(in.size(), params.getMaxTargets() - 1)));
    }

    private double calcSquareCenterDistance(TrackedTarget rect) {
        return FastMath.sqrt(
                FastMath.pow(params.getCamProperties().centerX - rect.getMinAreaRect().center.x, 2)
                        + FastMath.pow(params.getCamProperties().centerY - rect.getMinAreaRect().center.y, 2));
    }

    public static class SortContoursParams {
        private ContourSortMode m_sortMode;
        private FrameStaticProperties m_camProperties;
        private int m_maxTargets;

        public SortContoursParams(
                ContourSortMode sortMode, FrameStaticProperties camProperties, int maxTargets) {
            m_sortMode = sortMode;
            m_camProperties = camProperties;
            m_maxTargets = maxTargets;
        }

        public ContourSortMode getSortMode() {
            return m_sortMode;
        }

        public FrameStaticProperties getCamProperties() {
            return m_camProperties;
        }

        public int getMaxTargets() {
            return m_maxTargets;
        }
    }
}
