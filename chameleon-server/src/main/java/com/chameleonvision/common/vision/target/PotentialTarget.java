package com.chameleonvision.common.vision.target;

import com.chameleonvision.common.vision.opencv.Contour;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.RotatedRect;

public class PotentialTarget {

    final Contour m_mainContour;
    final List<Contour> m_subContours;

    public PotentialTarget(Contour inputContour) {
        m_mainContour = inputContour;
        m_subContours = new ArrayList<>(); // empty
    }

    public PotentialTarget(Contour inputContour, List<Contour> subContours) {
        m_mainContour = inputContour;
        m_subContours = subContours;
    }

    public RotatedRect getMinAreaRect() {
        return m_mainContour.getMinAreaRect();
    }

    public double getArea() {
        return m_mainContour.getArea();
    }
}
