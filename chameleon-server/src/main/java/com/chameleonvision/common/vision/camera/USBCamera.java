package com.chameleonvision.common.vision.camera;

import com.chameleonvision.common.configuration.CameraConfiguration;
import com.chameleonvision.common.vision.frame.provider.USBFrameProvider;
import com.chameleonvision.common.vision.processes.VisionSourceSettables;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.UsbCameraInfo;
import edu.wpi.cscore.VideoMode;
import edu.wpi.first.cameraserver.CameraServer;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class USBCamera extends USBFrameProvider {
    CvSink cvSink;
    UsbCamera camera;
    boolean isPS3Eye;

    public USBCamera(UsbCameraInfo info, CameraConfiguration config) {
        this.camera = new UsbCamera(config.nickname, info.dev);
        this.cvSink = CameraServer.getInstance().getVideo(this.camera);
    }

    public class USBCameraSettables implements VisionSourceSettables {

        @Override
        public int getExposure() {
            return camera.getProperty("exposure").get();
        }

        @Override
        public void setExposure(int exposure) {
            camera.setExposureManual(exposure);
        }

        @Override
        public int getBrightness() {
            return camera.getBrightness();
        }

        @Override
        public void setBrightness(int brightness) {
            camera.setBrightness(brightness);
        }

        @Override
        public int getGain() {
            return camera.getProperty("gain").get();
        }

        @Override
        public void setGain(int gain) {
            if (isPS3Eye) {
                camera.getProperty("gain_automatic").set(0);
                camera.getProperty("gain").set(gain);
            }
        }

        @Override
        public VideoMode getCurrentVideoMode() {
            return camera.getVideoMode();
        }

        @Override
        public void setCurrentVideoMode(VideoMode videoMode) {
            camera.setVideoMode(videoMode);
        }

        @Override
        public Dictionary<Integer, VideoMode> getAllVideoModes() {
            Dictionary<Integer, VideoMode> dictionary = new Hashtable<>();
            List<VideoMode> videoModes = Arrays.asList(camera.enumerateVideoModes());
            for (VideoMode videoMode : videoModes) {
                dictionary.put(videoModes.indexOf(videoMode), videoMode);
            }
            return dictionary;
        }
    }
}
