package com.chameleonvision.common.vision.pipeline.pipe;

import java.util.ArrayList;
import java.util.List;

import com.chameleonvision.common.vision.pipeline.CVPipe;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

public class FindContoursPipe extends CVPipe<Mat, List<MatOfPoint>, FindContoursPipe.FindContoursParams> {

    private List<MatOfPoint> m_foundContours = new ArrayList<>();

    @Override
    protected List<MatOfPoint> process(Mat in) {
        m_foundContours.clear();
        Imgproc.findContours(in, m_foundContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);
        return m_foundContours;
    }

    public static class FindContoursParams {
    }
}

