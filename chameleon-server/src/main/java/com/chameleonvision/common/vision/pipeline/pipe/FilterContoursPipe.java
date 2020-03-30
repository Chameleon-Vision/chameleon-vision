package com.chameleonvision.common.vision.pipeline.pipe;

import com.chameleonvision.common.util.math.MathUtils;
import com.chameleonvision.common.util.numbers.DoubleCouple;
import com.chameleonvision.common.vision.camera.CaptureStaticProperties;
import com.chameleonvision.common.vision.opencv.Contour;
import com.chameleonvision.common.vision.pipeline.CVPipe;
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
        double areaRatio = (contourArea / params.getCamProperties().imageArea);
        double minArea = MathUtils.sigmoid(params.getArea().getFirst());
        double maxArea = MathUtils.sigmoid(params.getArea().getSecond());
        if (areaRatio < minArea || areaRatio > maxArea) return;

        // Extent Filtering.
        RotatedRect minAreaRect = contour.getMinAreaRect();
        double minExtent = params.getExtent().getFirst() * minAreaRect.size.area() / 100;
        double maxExtent = params.getExtent().getSecond() * minAreaRect.size.area() / 100;
        if (contourArea <= minExtent || contourArea >= maxExtent) return;

        // Aspect Ratio Filtering.
        Rect boundingRect = contour.getBoundingRect();
        double aspectRatio = (double) boundingRect.width / boundingRect.height;
        if (aspectRatio < params.getRatio().getFirst() || aspectRatio > params.getRatio().getSecond())
            return;

        m_filteredContours.add(contour);
    }

    public static class FilterContoursParams {
        private DoubleCouple m_area;
        private DoubleCouple m_ratio;
        private DoubleCouple m_extent;
        private CaptureStaticProperties m_camProperties;

        public FilterContoursParams(
                DoubleCouple area,
                DoubleCouple ratio,
                DoubleCouple extent,
                CaptureStaticProperties camProperties) {
            this.m_area = area;
            this.m_ratio = ratio;
            this.m_extent = extent;
            this.m_camProperties = camProperties;
        }

        public DoubleCouple getArea() {
            return m_area;
        }

        public DoubleCouple getRatio() {
            return m_ratio;
        }

        public DoubleCouple getExtent() {
            return m_extent;
        }

        public CaptureStaticProperties getCamProperties() {
            return m_camProperties;
        }
    }
}
