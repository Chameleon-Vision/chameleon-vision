package com.chameleonvision.common.vision.frame.consumer;

import com.chameleonvision.common.configuration.CameraConfiguration;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameConsumer;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.cameraserver.CameraServer;
import org.apache.commons.lang3.NotImplementedException;

public class MJPGFrameConsumer implements FrameConsumer {
    private final CvSource cvSource;

    MJPGFrameConsumer(String name, int width, int height) {
        this.cvSource = CameraServer.getInstance().putVideo(name, width, height);
    }

    @Override
    public void accept(Frame frame) {
        cvSource.putFrame(frame.image.getMat());
    }
}
