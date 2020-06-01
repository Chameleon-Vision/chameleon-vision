package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.configuration.CameraConfiguration;
import com.chameleonvision.common.vision.frame.FrameProvider;
import com.chameleonvision.common.vision.frame.FrameStaticProperties;

public interface VisionSource {

    FrameProvider getFrameProvider();

    VisionSourceSettables getSettables();

}
