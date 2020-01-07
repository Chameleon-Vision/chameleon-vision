package com.chameleonvision.vision.pipeline.pipes;

import com.chameleonvision.vision.camera.CaptureStaticProperties;
import com.chameleonvision.vision.enums.TargetRegion;
import com.chameleonvision.vision.pipeline.Pipe;
import com.chameleonvision.vision.pipeline.impl.StandardCVPipeline;
import com.chameleonvision.vision.enums.CalibrationMode;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.List;

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
                    double angle = FastMath.toRadians(t.minAreaRect.angle);
                    double cosA = FastMath.cos(angle);
                    double sinA = FastMath.sin(angle);
                    switch (this.targetRegion) {
                        case Right:
                            t.point.x = t.minAreaRect.center.x + t.minAreaRect.size.width / 2 * cosA;
                            t.point.y = t.minAreaRect.center.y + t.minAreaRect.size.width / 2 * sinA;
                            break;
                        case Left:
                            t.point.x = t.minAreaRect.center.x - t.minAreaRect.size.width / 2 * cosA;
                            t.point.y = t.minAreaRect.center.y + t.minAreaRect.size.width / 2 * sinA;
                            break;
                        case Top:
                            t.point.x = t.minAreaRect.center.x + t.minAreaRect.size.height / 2 * sinA;
                            t.point.y = t.minAreaRect.center.y - t.minAreaRect.size.height / 2 * cosA;
                            break;
                        case Bottom:
                            t.point.x = t.minAreaRect.center.x + t.minAreaRect.size.height / 2 * sinA;
                            t.point.y = t.minAreaRect.center.y + t.minAreaRect.size.height / 2 * cosA;
                            break;
                    }

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
}
