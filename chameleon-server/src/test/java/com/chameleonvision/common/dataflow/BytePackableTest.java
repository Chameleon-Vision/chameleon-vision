package com.chameleonvision.common.dataflow;

import com.chameleonvision.common.dataflow.structures.SimplePipelineResult;
import com.chameleonvision.common.dataflow.structures.SimpleTrackedTarget;
import com.chameleonvision.common.util.TestUtils;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BytePackableTest {
    @BeforeAll
    public static void init() {
        TestUtils.loadLibraries();
    }

    @Test
    public void SimpleTrackedTargetTest() {
        var pose = new Pose2d(123.45, 678.9, Rotation2d.fromDegrees(69.420));
        var simpleTrackedTarget = new SimpleTrackedTarget(1, 2, 3, pose);

        var data = simpleTrackedTarget.toByteArray();

        var unpackedTarget = new SimpleTrackedTarget();
        unpackedTarget.fromByteArray(data);

        Assertions.assertEquals(simpleTrackedTarget, unpackedTarget);
    }

    @Test
    public void SimplePipelineResultTest() {
        List<SimpleTrackedTarget> targets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            targets.add(
                    new SimpleTrackedTarget(
                            Math.random() * 50,
                            Math.random() * 50,
                            Math.random() * 50,
                            new Pose2d(
                                    Math.random() * 50,
                                    Math.random() * 50,
                                    Rotation2d.fromDegrees(Math.random() * 50))));
        }

        var simplePipelineResult = new SimplePipelineResult(420, true, targets);
        var data = simplePipelineResult.toByteArray();

        var unpackedResult = new SimplePipelineResult();
        unpackedResult.fromByteArray(data);

        Assertions.assertEquals(simplePipelineResult, unpackedResult);
    }
};
