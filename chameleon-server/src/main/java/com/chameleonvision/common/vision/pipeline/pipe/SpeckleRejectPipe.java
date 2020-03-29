package com.chameleonvision.common.vision.pipeline.pipe;

import java.util.ArrayList;
import java.util.List;

import com.chameleonvision.common.vision.pipeline.CVPipe;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

public class SpeckleRejectPipe extends CVPipe<List<MatOfPoint>, List<MatOfPoint>,
    SpeckleRejectPipe.SpeckleRejectParams> {

    private List<MatOfPoint> m_despeckledContours = new ArrayList<>();

    @Override
    protected List<MatOfPoint> process(List<MatOfPoint> in) {
        m_despeckledContours.forEach(MatOfPoint::release);
        m_despeckledContours.clear();

        if (in.size() > 0) {
            double averageArea = 0.0;
            for (MatOfPoint c : in) {
                averageArea += Imgproc.contourArea(c);
            }
            averageArea /= in.size();

            double minAllowedArea = params.getMinPercentOfAvg() / 100.0 * averageArea;
            for (MatOfPoint c : in) {
                if (Imgproc.contourArea(c) >= minAllowedArea) {
                    m_despeckledContours.add(c);
                }
            }
        }

        return m_despeckledContours;
    }

    public static class SpeckleRejectParams {
        private double m_minPercentOfAvg;

        public SpeckleRejectParams(double minPercentOfAvg) {
            m_minPercentOfAvg = minPercentOfAvg;
        }

        public double getMinPercentOfAvg() {
            return m_minPercentOfAvg;
        }
    }
}
