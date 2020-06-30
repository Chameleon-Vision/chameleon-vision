package com.chameleonvision.common.vision.pipe.impl;

import com.chameleonvision.common.vision.pipe.CVPipe;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class HSVPipe extends CVPipe<Mat, Mat, HSVPipe.HSVParams> {

    private Mat m_outputMat = new Mat();

    @Override
    protected Mat process(Mat in) {
        in.copyTo(m_outputMat);
        try {
            Imgproc.cvtColor(m_outputMat, m_outputMat, Imgproc.COLOR_BGR2HSV, 3);
            Core.inRange(m_outputMat, params.getHsvLower(), params.getHsvUpper(), m_outputMat);
        } catch (CvException e) {
            System.err.println("(HSVPipe) Exception thrown by OpenCV: \n" + e.getMessage());
        }

        return m_outputMat;
    }

    public static class HSVParams {
        private Scalar m_hsvLower;
        private Scalar m_hsvUpper;

        public HSVParams(List<Number> hue, List<Number> saturation, List<Number> value) {
            m_hsvLower =
                    new Scalar(
                            hue.get(0).doubleValue(),
                            saturation.get(0).doubleValue(),
                            value.get(0).doubleValue());
            m_hsvUpper =
                    new Scalar(
                            hue.get(1).doubleValue(),
                            saturation.get(1).doubleValue(),
                            value.get(1).doubleValue());
        }

        public HSVParams(Scalar hsvLower, Scalar hsvUpper) {
            m_hsvLower = hsvLower;
            m_hsvUpper = hsvUpper;
        }

        public Scalar getHsvLower() {
            return m_hsvLower;
        }

        public Scalar getHsvUpper() {
            return m_hsvUpper;
        }
    }
}
