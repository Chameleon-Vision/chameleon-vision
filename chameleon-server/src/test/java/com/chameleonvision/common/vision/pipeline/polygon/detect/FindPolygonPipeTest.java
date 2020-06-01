package com.chameleonvision.common.vision.pipeline.polygon.detect;

import com.chameleonvision.common.vision.opencv.CVShape;
import com.chameleonvision.common.vision.opencv.Contour;
import com.chameleonvision.common.vision.pipe.CVPipeResult;
import com.chameleonvision.common.vision.pipe.impl.FindPolygonPipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class FindPolygonPipeTest {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        FindPolygonPipe findPolygonPipe = new FindPolygonPipe();
        findPolygonPipe.setParams(new FindPolygonPipe.FindPolygonPipeParams(25));
        Mat srcGray = new Mat();
        String filename =
                args.length > 0
                        ? args[0]
                        : Objects.requireNonNull(
                                FindPolygonPipeTest.class
                                        .getClassLoader()
                                        .getResource("polygons/polygons.png")
                                        .getPath()
                                        .substring(1));
        Mat src = Imgcodecs.imread(filename);
        Imgproc.resize(src, src, new Size(480, 324));
        if (src.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(
                srcGray, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        List<Contour> input = new ArrayList<>();

        contours.forEach(contour -> input.add(new Contour(contour)));

        CVPipeResult<List<CVShape>> output = findPolygonPipe.apply(input);
        System.out.println("Pipe ran in : " + output.nanosElapsed / 1000000 + " ms");
        output.result.forEach(
                corner -> {
                    Rect rect = Imgproc.boundingRect(corner.contour.mat);
                    Imgproc.putText(
                            src,
                            String.valueOf(corner.shape.sides),
                            new Point(rect.x, rect.y - 10),
                            Core.FONT_HERSHEY_SIMPLEX,
                            0.9,
                            new Scalar(36, 255, 12),
                            2);
                    Imgproc.drawContours(
                            src,
                            List.of(new MatOfPoint(corner.getApproxPolyDp(0.01, true).toArray())),
                            -1,
                            new Scalar(255, 0, 255),
                            3);
                });
        HighGui.imshow("Test", src);
        HighGui.waitKey();
    }
}
