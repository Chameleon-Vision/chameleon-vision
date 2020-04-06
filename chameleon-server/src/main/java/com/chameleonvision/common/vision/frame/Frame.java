package com.chameleonvision.common.vision.frame;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

public class Frame {
    public final long timestampNanos;
    public final Mat image;
    public final Size imageSize;
    public final Point imageCenterPoint;

    public Frame(Mat image, long timestampNanos) {
        this.image = image;
        this.timestampNanos = timestampNanos;
        imageSize = image.size();
        imageCenterPoint = new Point(imageSize.width / 2, imageSize.height / 2);
    }

    public Frame(Mat image) {
        this(image, System.nanoTime());
    }

    public static Frame copyFrom(Frame frame) {
        Mat newMat = new Mat();
        frame.image.copyTo(newMat);
        frame.image.release();
        return new Frame(newMat, frame.timestampNanos);
    }
}
