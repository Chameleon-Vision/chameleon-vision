package com.chameleonvision.common.vision.target;

import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.util.numbers.DoubleCouple;
import com.chameleonvision.common.vision.opencv.Contour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TrackedTargetTest {
    @BeforeEach
    public void Init() {
        TestUtils.loadLibraries();
    }

    @Test
    void axisTest() {
        MatOfPoint mat = new MatOfPoint();
        mat.fromList(List.of(
                new Point(400, 298),
                new Point(426.22, 298),
                new Point(426.22, 302),
                new Point(400, 302)
        )); // gives contour with center of 426, 300
        Contour contour = new Contour(mat);
        var pTarget = new PotentialTarget(contour);
        var setting =
                new TrackedTarget.TargetCalculationParameters(
                        false,
                        TargetOffsetPointEdge.Center,
                        new Point(0, 0),
                        new Point(400, 300),
                        new DoubleCouple(0.0, 0.0),
                        RobotOffsetPointMode.None,
                        61,
                        34.3,
                        480000);

        var trackedTarget = new TrackedTarget(pTarget, setting);
        assertEquals(trackedTarget.getYaw(), 1.4);
        assertEquals(trackedTarget.getPitch(), 0);
    }
}
