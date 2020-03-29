package com.chameleonvision.common.vision.pipeline.pipe;

import java.util.ArrayList;
import java.util.List;

import com.chameleonvision.common.util.numbers.DoubleCouple;
import com.chameleonvision.common.vision.camera.CaptureStaticProperties;
import com.chameleonvision.common.vision.pipeline.CVPipe;
import com.chameleonvision.common.vision.target.TrackedTarget;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Point;

public class Collect2dTargetsPipe extends CVPipe<
    Pair<List<TrackedTarget>, CaptureStaticProperties>, List<TrackedTarget>,
    Collect2dTargetsPipe.Collect2dTargetsParams> {

    @Override
    protected List<TrackedTarget> process(Pair<List<TrackedTarget>, CaptureStaticProperties> in) {
        List<TrackedTarget> targets = new ArrayList<>();

        List<TrackedTarget> inputs = in.getLeft();
        double imageArea = in.getRight().imageArea;

        for (TrackedTarget input : inputs) {
            Point targetOffsetPoint = input.getTargetOffsetPoint(
                params.getOrientation() == TrackedTarget.TargetOrientation.Landscape,
                params.getOffsetPointRegion());

            Point robotOffsetPoint = input.getRobotOffsetPoint(
                targetOffsetPoint, params.getCalibrationPoint(),
                new DoubleCouple(params.getCalibrationB(), params.getCalibrationM()),
                params.getOffsetMode());

            // TODO: Calculate pitch, yaw, and area.
            targets.add(input);
        }

        return targets;
    }

    public static class Collect2dTargetsParams {
        private CaptureStaticProperties m_captureStaticProperties;
        private TrackedTarget.RobotOffsetPointMode m_offsetMode;
        private double m_calibrationM, m_calibrationB;
        private Point m_calibrationPoint;
        private TrackedTarget.TargetOffsetPointRegion m_region;
        private TrackedTarget.TargetOrientation m_orientation;

        public Collect2dTargetsParams(CaptureStaticProperties captureStaticProperties,
                                      TrackedTarget.RobotOffsetPointMode offsetMode,
                                      double calibrationM, double calibrationB,
                                      Point calibrationPoint,
                                      TrackedTarget.TargetOffsetPointRegion region,
                                      TrackedTarget.TargetOrientation orientation) {
            m_captureStaticProperties = captureStaticProperties;
            m_offsetMode = offsetMode;
            m_calibrationM = calibrationM;
            m_calibrationB = calibrationB;
            m_calibrationPoint = calibrationPoint;
            m_region = region;
            m_orientation = orientation;
        }

        public CaptureStaticProperties getCaptureStaticProperties() {
            return m_captureStaticProperties;
        }

        public TrackedTarget.RobotOffsetPointMode getOffsetMode() {
            return m_offsetMode;
        }

        public double getCalibrationM() {
            return m_calibrationM;
        }

        public double getCalibrationB() {
            return m_calibrationB;
        }

        public Point getCalibrationPoint() {
            return m_calibrationPoint;
        }

        public TrackedTarget.TargetOffsetPointRegion getOffsetPointRegion() {
            return m_region;
        }

        public TrackedTarget.TargetOrientation getOrientation() {
            return m_orientation;
        }


    }


}
