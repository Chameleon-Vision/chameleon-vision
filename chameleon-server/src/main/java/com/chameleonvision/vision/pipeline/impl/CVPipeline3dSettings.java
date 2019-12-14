package com.chameleonvision.vision.pipeline.impl;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point3;

import java.util.List;

public class CVPipeline3dSettings extends CVPipeline2dSettings {
    // TODO: (2.1) define 3d-specific pipeline settings
    // add 3d-specific property to ensure serializing/deserializing works
    public int maxTargets = 5;
    public List<Point3> targetCorners = List.of();
    public Mat cameraMatrix = new Mat();
    public MatOfDouble cameraDistortionCoefficients = new MatOfDouble();
    public double cameraTiltAngleDeg = 0d;
}
