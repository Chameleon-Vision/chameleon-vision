package com.chameleonvision.common.vision.frame.consumer;

import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameConsumer;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.first.cameraserver.CameraServer;

public class MJPGFrameConsumer implements FrameConsumer {
    private final CvSource cvSource;
    private final MjpegServer cvServer;

    public MJPGFrameConsumer(String name, int width, int height) {
        this.cvSource = CameraServer.getInstance().putVideo(name, width, height);
        cvServer = (MjpegServer) CameraServer.getInstance().getServer("serve_" + name);
    }

    @Override
    public void accept(Frame frame) {
        cvSource.putFrame(frame.image.getMat());
    }

    @Override
    public int getPort() {
        return cvServer.getPort();
    }
}
