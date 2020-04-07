package com.chameleonvision.common.vision.frame;

import org.opencv.core.Mat;

public class Frame {
    public final long timestampNanos;
    public final Mat image;
    public final FrameStaticProperties frameStaticProperties;

    public Frame(Mat image, long timestampNanos, FrameStaticProperties frameStaticProperties) {
        this.image = image;
        this.timestampNanos = timestampNanos;
        this.frameStaticProperties = frameStaticProperties;
    }

    public Frame(Mat image, FrameStaticProperties frameStaticProperties) {
        this(image, System.nanoTime(), frameStaticProperties);
    }

    public static Frame copyFrom(Frame frame) {
        Mat newMat = new Mat();
        frame.image.copyTo(newMat);
        frame.image.release();
        return new Frame(newMat, frame.timestampNanos, frame.frameStaticProperties);
    }
}
