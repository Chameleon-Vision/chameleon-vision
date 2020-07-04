package com.chameleonvision.common.vision.frame.consumer;

import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameConsumer;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.first.cameraserver.CameraServer;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MJPGFrameConsumer implements FrameConsumer {
    private CvSource cvSource;
    private final MjpegServer cvServer;
    private Size res;

    public MJPGFrameConsumer(String name, int width, int height) {
        this.cvSource = CameraServer.getInstance().putVideo(name, width, height);
        res = new Size(width, height);
        cvServer = (MjpegServer) CameraServer.getInstance().getServer("serve_" + name);
    }

    @Override
    public void accept(Frame frame) {
        Mat mat = frame.image.getMat();
        Imgproc.resize(mat, mat, res);
        cvSource.putFrame(mat);
    }

    @Override
    public int getPort() {
        return cvServer.getPort();
    }

    @Override
    public void setResolution(int width, int height) {
        this.res = new Size(width, height);
        this.cvSource.setResolution(width, height);
    }
}
