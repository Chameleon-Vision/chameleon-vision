package com.chameleonvision.vision.image;

import com.chameleonvision.config.CameraCalibrationConfig;
import com.chameleonvision.vision.camera.CameraCapture;
import com.chameleonvision.vision.camera.USBCaptureProperties;
import edu.wpi.cscore.VideoMode;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StaticImageCapture implements CameraCapture {

    private Mat image = new Mat();
    private final VideoMode fakeVideoMode;
    private final com.chameleonvision.vision.image.CaptureProperties properties;

    public StaticImageCapture(Path imagePath) {
        this(imagePath, 70);
    }

    public StaticImageCapture(Path imagePath, double FOV) {
        if (!Files.exists(imagePath)) throw new RuntimeException("Invalid path for image!");

        Mat loadedImage = Imgcodecs.imread(imagePath.toString());
        loadedImage.copyTo(image);
        if (image.cols() > 0 && image.rows() > 0) {
            fakeVideoMode = new VideoMode(VideoMode.PixelFormat.kBGR, image.cols(), image.rows(), 60);
        } else {
            throw new RuntimeException("Failed to load image!");
        }

        properties = new com.chameleonvision.vision.image.CaptureProperties(fakeVideoMode, FOV);
    }

    @Override
    public Pair<Mat, Long> getFrame() {
        return Pair.of(image, System.nanoTime());
    }

    @Override
    public CaptureProperties getProperties() {
        return properties;
    }

    @Override
    public VideoMode getCurrentVideoMode() {
        return fakeVideoMode;
    }

    @Override
    public void setExposure(int exposure) {
        // do nothing
    }

    @Override
    public void setBrightness(int brightness) {
        // do nothing
    }

    @Override
    public void setVideoMode(VideoMode mode) {
        // do nothing
    }

    @Override
    public void setVideoMode(int index) {
        // do nothing
    }

    @Override
    public void setGain(int gain) {
        // do nothing
    }

    @Override
    public CameraCalibrationConfig getCurrentCalibrationData() {
        return null;
    }

    @Override
    public List<CameraCalibrationConfig> getAllCalibrationData() {
        return null;
    }
}
