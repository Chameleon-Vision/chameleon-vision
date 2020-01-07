package com.chameleonvision.vision.pipeline.pipes;

import com.chameleonvision.util.MathHandler;
import com.chameleonvision.vision.camera.CaptureStaticProperties;
import com.chameleonvision.vision.enums.TargetRegion;
import com.chameleonvision.vision.pipeline.Pipe;
import com.chameleonvision.vision.pipeline.impl.StandardCVPipeline;
import com.chameleonvision.vision.enums.CalibrationMode;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.FastMath;
import org.opencv.core.Point;

import java.util.*;

public class Collect2dTargetsPipe implements Pipe<Pair<List<StandardCVPipeline.TrackedTarget>, CaptureStaticProperties>, List<StandardCVPipeline.TrackedTarget>> {


    private CaptureStaticProperties camProps;
    private CalibrationMode calibrationMode;
    private List<Number> calibrationPoint;
    private double calibrationM, calibrationB;
    private TargetRegion targetRegion;
    private List<StandardCVPipeline.TrackedTarget> targets = new ArrayList<>();
    private Point[] vertices = new Point[4];

    public Collect2dTargetsPipe(CalibrationMode calibrationMode, TargetRegion targetRegion, List<Number> calibrationPoint, double calibrationM, double calibrationB, CaptureStaticProperties camProps) {
        setConfig(calibrationMode, targetRegion, calibrationPoint, calibrationM, calibrationB, camProps);
    }

    public void setConfig(CalibrationMode calibrationMode, TargetRegion targetRegion, List<Number> calibrationPoint, double calibrationM, double calibrationB, CaptureStaticProperties camProps) {
        this.calibrationMode = calibrationMode;
        this.calibrationPoint = calibrationPoint;
        this.calibrationM = calibrationM;
        this.calibrationB = calibrationB;
        this.camProps = camProps;
        this.targetRegion = targetRegion;
    }

    @Override
    public Pair<List<StandardCVPipeline.TrackedTarget>, Long> run(Pair<List<StandardCVPipeline.TrackedTarget>, CaptureStaticProperties> inputPair) {
        long processStartNanos = System.nanoTime();

        targets.clear();
        var input = inputPair.getLeft();
        var imageArea = inputPair.getRight().imageArea;

        if (input.size() > 0) {
            for (var t : input) {
                t.minAreaRect.points(vertices);
                Point bl = getMiddle(vertices[0], vertices[1]);
                Point tl = getMiddle(vertices[1], vertices[2]);
                Point tr = getMiddle(vertices[2], vertices[3]);
                Point br = getMiddle(vertices[3], vertices[0]);

                double m = MathHandler.toSlope(-t.minAreaRect.angle);
                double b1 = vertices[3].x * m - vertices[3].y;
                double b2 = vertices[0].x * m - vertices[0].y;

                boolean orientation = b1 < b2 || t.minAreaRect.size.width > t.minAreaRect.size.height;
                Point result = t.minAreaRect.center;
                switch (this.targetRegion) {
                    case Top: {
                        result = orientation ? tl : tr;
                        break;
                    }
                    case Bottom: {
                        result = orientation ? br : bl;
                        break;
                    }
                    case Left: {
                        result = orientation ? bl : tl;
                        break;
                    }
                    case Right: {
                        result = orientation ? tr : br;
                        break;
                    }
                }
                t.point = result;

                switch (this.calibrationMode) {
                    case Single:
                        if (this.calibrationPoint.isEmpty()) {
                            this.calibrationPoint.add(camProps.centerX);
                            this.calibrationPoint.add(camProps.centerY);
                        }
                        t.calibratedX = this.calibrationPoint.get(0).doubleValue();
                        t.calibratedY = this.calibrationPoint.get(1).doubleValue();
                        break;
                    case None:
                        t.calibratedX = camProps.centerX;
                        t.calibratedY = camProps.centerY;
                        break;
                    case Dual:
                        t.calibratedX = (t.point.x - this.calibrationB) / this.calibrationM;
                        t.calibratedY = (t.point.y * this.calibrationM) + this.calibrationB;
                        break;
                }

                t.pitch = calculatePitch(t.point.y, t.calibratedY);
                t.yaw = calculateYaw(t.point.x, t.calibratedX);
                t.area = t.minAreaRect.size.area() / imageArea;

                targets.add(t);
            }
        }

        long processTime = System.nanoTime() - processStartNanos;
        return Pair.of(targets, processTime);
    }

    private double calculatePitch(double pixelY, double centerY) {
        double pitch = FastMath.toDegrees(FastMath.atan((pixelY - centerY) / camProps.verticalFocalLength));
        return (pitch * -1);
    }

    private double calculateYaw(double pixelX, double centerX) {
        return FastMath.toDegrees(FastMath.atan((pixelX - centerX) / camProps.horizontalFocalLength));
    }

    private Point getMiddle(Point p1, Point p2) {
        return new Point(((p1.x + p2.x) / 2), ((p1.y + p2.y) / 2));
    }
}
