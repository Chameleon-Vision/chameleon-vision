package com.chameleonvision.common.vision;

import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;

public class TestUtils {

    private TestUtils() {}

    private static int DefaultTimeoutMillis = 10000;

    public static void showImage(Mat frame, String title, int timeoutMs) {
        HighGui.imshow(title, frame);
        HighGui.waitKey(timeoutMs);
        HighGui.destroyAllWindows();
    }

    public static void showImage(Mat frame, int timeoutMs) {
        showImage(frame, "", timeoutMs);
    }

    public static void showImage(Mat frame, String title) {
        showImage(frame, title, DefaultTimeoutMillis);
    }

    public static void showImage(Mat frame) {
        showImage(frame, DefaultTimeoutMillis);
    }
}
