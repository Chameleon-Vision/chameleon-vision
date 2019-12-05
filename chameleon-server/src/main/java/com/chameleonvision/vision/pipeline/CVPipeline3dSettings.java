package com.chameleonvision.vision.pipeline;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point3;

import java.util.List;

public class CVPipeline3dSettings extends CVPipeline2dSettings {
    // TODO: (2.1) define 3d-specific pipeline settings
    // add 3d-specific property to ensure serializing/deserializing works
    public int maxTargets = 5;
    public List<Point3> targetCorners;
    public Mat cameraMatrix;
    public MatOfDouble cameraDistortionCoefficients;
}
