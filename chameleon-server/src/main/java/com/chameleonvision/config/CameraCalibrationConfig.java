package com.chameleonvision.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.opencv.core.Mat;
import org.opencv.core.Size;

public class CameraCalibrationConfig {
    public final Size resolution;
    public final Mat cameraCoeffs;
    public final Mat distCoeffs;


}
