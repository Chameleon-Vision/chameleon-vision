package com.chameleonvision.common.vision.pipe.impl;

import com.chameleonvision.common.util.math.MathUtils;
import com.chameleonvision.common.vision.frame.FrameStaticProperties;
import com.chameleonvision.common.vision.opencv.Contour;
import com.chameleonvision.common.vision.pipe.CVPipe;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;

public class FilterContoursPipe
        extends CVPipe<List<Contour>, List<Contour>, FilterContoursPipe.FilterContoursParams> {

    List<Contour> m_filteredContours = new ArrayList<>();

    @Override
    protected List<Contour> process(List<Contour> in) {
        m_filteredContours.clear();
        for (Contour contour : in) {
            try {
                filterContour(contour);
            } catch (Exception e) {
                System.err.println("An error occurred while filtering contours.");
                e.printStackTrace();
            }
        }
        return m_filteredContours;
    }

    private void filterContour(Contour contour) {
        // Area Filtering.
        double contourArea = contour.getArea();
        double areaRatio = (contourArea / params.getCamProperties().imageArea) * 100;
        double minArea = MathUtils.sigmoid(params.getArea().get(0));
        double maxArea = MathUtils.sigmoid(params.getArea().get(1));
        if (areaRatio < minArea || areaRatio > maxArea) return;

        // Extent Filtering.
        RotatedRect minAreaRect = contour.getMinAreaRect();
        double minExtent = params.getExtent().get(0).doubleValue() * minAreaRect.size.area() / 100;
        double maxExtent = params.getExtent().get(1).doubleValue() * minAreaRect.size.area() / 100;
        if (contourArea <= minExtent || contourArea >= maxExtent) return;

        // Aspect Ratio Filtering.
        Rect boundingRect = contour.getBoundingRect();
        double aspectRatio = ((double) boundingRect.width / boundingRect.height);
        if (aspectRatio < params.getRatio().get(0).doubleValue()
                || aspectRatio > params.getRatio().get(1).doubleValue()) return;

        m_filteredContours.add(contour);
    }

    public static class FilterContoursParams {
        private List<Number> m_area;
        private List<Number> m_ratio;
        private List<Number> m_extent;
        private FrameStaticProperties m_camProperties;

        public FilterContoursParams(
                List<Number> area,
                List<Number> ratio,
                List<Number> extent,
                FrameStaticProperties camProperties) {
            this.m_area = area;
            this.m_ratio = ratio;
            this.m_extent = extent;
            this.m_camProperties = camProperties;
        }

        public List<Number> getArea() {
            return m_area;
        }

        public List<Number> getRatio() {
            return m_ratio;
        }

        public List<Number> getExtent() {
            return m_extent;
        }

        public FrameStaticProperties getCamProperties() {
            return m_camProperties;
        }
    }
}
