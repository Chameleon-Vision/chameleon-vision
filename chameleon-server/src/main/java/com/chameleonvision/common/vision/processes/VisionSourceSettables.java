package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.configuration.CameraConfiguration;
import com.chameleonvision.common.vision.frame.FrameStaticProperties;
import edu.wpi.cscore.VideoMode;
import java.util.HashMap;

public abstract class VisionSourceSettables {
    private CameraConfiguration configuration;

    protected VisionSourceSettables(CameraConfiguration configuration) {
        this.configuration = configuration;
    }

    FrameStaticProperties frameStaticProperties;
    protected HashMap<Integer, VideoMode> videoModes;

    public abstract int getExposure();

    public abstract void setExposure(int exposure);

    public abstract int getBrightness();

    public abstract void setBrightness(int brightness);

    public abstract int getGain();

    public abstract void setGain(int gain);

    public abstract VideoMode getCurrentVideoMode();

    public abstract void setCurrentVideoMode(VideoMode videoMode);

    public abstract HashMap<Integer, VideoMode> getAllVideoModes();

    public double getFOV() {
        return configuration.FOV;
    }

    public void setFOV(double fov) {
        configuration.FOV = fov;
    }

    public FrameStaticProperties getFrameStaticProperties() {
        return frameStaticProperties;
    }
}
