package com.chameleonvision.vision.pipeline.pipes;

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
                if (this.targetRegion == TargetRegion.Center) {
                    t.point.x = t.minAreaRect.center.x;
                    t.point.y = t.minAreaRect.center.y;
                } else {
                    Point[] points = new Point[4];
                    t.minAreaRect.points(points);
                    double angle = t.minAreaRect.angle;
                    Point tl, tr, bl, br, result;
//                    if (t.minAreaRect.size.width < t.minAreaRect.size.height) {
//                        angle -= 90;
//                    }


                    bl = getMiddle(points[0], points[1]);
                    tl = getMiddle(points[1], points[2]);
                    tr = getMiddle(points[2], points[3]);
                    br = getMiddle(points[3], points[0]);

                    switch (this.targetRegion) {
                        case Top: {
                            if (t.minAreaRect.size.width > t.minAreaRect.size.height) {
                                result = tl;
                            } else {
                                result = tr;
                            }
                            break;
                        }
                        case Bottom:{
                            if (t.minAreaRect.size.width > t.minAreaRect.size.height) {
                                result = br;
                            } else {
                                result = bl;
                            }
                            break;
                        }
                        case Left:{
                            if (t.minAreaRect.size.width > t.minAreaRect.size.height) {
                                result = bl;
                            } else {
                                result = tl;
                            }
                            break;
                        }
                        case Right:{
                            if (t.minAreaRect.size.width > t.minAreaRect.size.height) {
                                result = tr;
                            } else {
                                result = br;
                            }
                            break;
                        }
                        default:{
                            result = t.minAreaRect.center;
                        }

                    }
                    t.point = result;


                }
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
