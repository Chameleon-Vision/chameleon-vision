package com.chameleonvision.classabstraction.pipeline.pipes;

import com.chameleonvision.classabstraction.camera.CameraStaticProperties;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Draw2dContoursPipe implements Pipe<Pair<Mat, List<RotatedRect>>, Mat> {

    private final Draw2dContoursSettings settings;
    private final CameraStaticProperties camProps;

    private Mat outputMat = new Mat();

    public Draw2dContoursPipe(Draw2dContoursSettings settings, CameraStaticProperties camProps) {
        this.settings = settings;
        this.camProps = camProps;
    }

    @Override
    public Pair<Mat, Long> run(Pair<Mat, List<RotatedRect>> input) {
        long processStartNanos = System.nanoTime();

        input.getLeft().copyTo(outputMat);

        for (RotatedRect r : input.getRight()) {
            if (r == null) continue;

            List<MatOfPoint> drawnContour = new ArrayList<>();
            Point[] vertices = new Point[4];
            r.points(vertices);
            MatOfPoint contour = new MatOfPoint(vertices);
            drawnContour.add(contour);

            if (settings.showCentroid) {
                Imgproc.circle(outputMat, r.center, 3, colorToScalar(settings.centroidColor));
            }

            if (settings.showCrosshair) {
                Point xMax = new Point(camProps.centerX + 10, camProps.centerY);
                Point xMin = new Point(camProps.centerX - 10, camProps.centerY);
                Point yMax = new Point(camProps.centerX, camProps.centerY + 10);
                Point yMin = new Point(camProps.centerX, camProps.centerY - 10);
                Imgproc.line(outputMat, xMax, xMin, colorToScalar(settings.crosshairColor), 2);
                Imgproc.line(outputMat, yMax, yMin, colorToScalar(settings.crosshairColor), 2);
            }

            if (settings.showRotatedBox) {
                Imgproc.drawContours(outputMat, drawnContour, 0, colorToScalar(settings.rotatedBoxColor), settings.boxOutlineSize);
            }

            if (settings.showMaximumBox) {
                Rect box = Imgproc.boundingRect(contour);
                Imgproc.rectangle(outputMat, new Point(box.x, box.y), new Point((box.x + box.width), (box.y + box.height)), colorToScalar(settings.maximumBoxColor), settings.boxOutlineSize);
            }
        }

        long processTime = processStartNanos - System.nanoTime();
        Pair<Mat, Long> output = Pair.of(outputMat, processTime);
        outputMat.release();
        return output;
    }

    private Scalar colorToScalar(Color color) {
        return new Scalar(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static class Draw2dContoursSettings {
        public boolean showCentroid = false;
        public boolean showCrosshair = false;
        public int boxOutlineSize = 0;
        public boolean showRotatedBox = false;
        public boolean showMaximumBox = false;
        public Color centroidColor = Color.GREEN;
        public Color crosshairColor = Color.GREEN;
        public Color rotatedBoxColor = Color.BLUE;
        public Color maximumBoxColor = Color.RED;

    }
}