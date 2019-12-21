package com.chameleonvision.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opencv.core.Mat;
import org.opencv.core.Size;

/**
 * A class that holds a camera matrix and distortion coefficients for a given resolution
 */
public class CameraCalibrationConfig {
    public final Size resolution;
    private final JsonMat cameraMatrix;
    private final JsonMat distortionCoeffs;

    @JsonCreator
    public CameraCalibrationConfig(
            @JsonProperty("resolution") Size resolution,
            @JsonProperty("cameraMatrix") JsonMat cameraMatrix,
            @JsonProperty("distortionCoeffs") JsonMat distortionCoeffs) {
        this.resolution = resolution;
        this.cameraMatrix = cameraMatrix;
        this.distortionCoeffs = distortionCoeffs;
    }

    public CameraCalibrationConfig(Size resolution, Mat cameraMatrix, Mat distortionCoeffs) {
        this.resolution = resolution;
        this.cameraMatrix = JsonMat.fromMat(cameraMatrix);
        this.distortionCoeffs = JsonMat.fromMat(distortionCoeffs);
    }

    public Mat getCameraMatrix() {
        return cameraMatrix.toMat();
    }

    public Mat getDistortionCoefffs() {
        return distortionCoeffs.toMat();
    }
}
