package com.chameleonvision.common.vision.frame;

import org.opencv.core.Mat;

public class Frame {
    public final long timestampNanos;
    public final Mat image;

    public Frame(Mat image) {
        this.image = image;
        timestampNanos = System.nanoTime();
    }

    public Frame(Mat image, long timestampNanos) {
        this.image = image;
        this.timestampNanos = timestampNanos;
    }

    public static Frame copyFrom(Frame frame) {
        Mat newMat = new Mat();
        frame.image.copyTo(newMat);
        frame.image.release();
        return new Frame(newMat, frame.timestampNanos);
    }
}
