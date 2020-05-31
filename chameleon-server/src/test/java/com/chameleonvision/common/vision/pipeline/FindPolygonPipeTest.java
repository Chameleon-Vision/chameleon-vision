package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.vision.pipe.impl.FindPolygonPipe;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class FindPolygonPipeTest {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static FindPolygonPipe findPolygonPipe = new FindPolygonPipe();

    public static void main(String[] args){

        loadContours();
    }

    public static List<MatOfPoint> loadContours(){
        Mat frame = Imgcodecs.imread("D:\\chameleon-vision\\chameleon-server\\src\\test\\resources\\polygons\\polygons.png");
        Imgproc.resize(frame, frame, new Size(1080, 720));
        HighGui.imshow("Hi", frame);
        HighGui.waitKey();

        Mat frameHSV = new Mat();
        Imgproc.cvtColor(frame, frameHSV, Imgproc.COLOR_BGR2GRAY);
        List<MatOfPoint> contours = new ArrayList<>();
        final Mat hierarchy = new Mat();
        Imgproc.findContours(frameHSV, contours, hierarchy,  Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

}
