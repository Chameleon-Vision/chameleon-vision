package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.logging.Level;
import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.vision.frame.provider.FileFrameProvider;
import com.chameleonvision.common.vision.opencv.ContourGroupingMode;
import com.chameleonvision.common.vision.opencv.ContourIntersectionDirection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReflectivePipelineTest {

   private static final Logger logger = new Logger(ReflectivePipeline.class, LogGroup.VisionProcess);

    @Test
    public void test2019() {
        TestUtils.loadLibraries();
        var pipeline = new ReflectivePipeline();
        pipeline.getSettings().hsvHue.set(60, 100);
        pipeline.getSettings().hsvSaturation.set(100, 255);
        pipeline.getSettings().hsvValue.set(190, 255);
        pipeline.getSettings().outputShowThresholded = true;
        pipeline.getSettings().outputShowMultipleTargets = true;
        pipeline.getSettings().contourGroupingMode = ContourGroupingMode.Dual;
        pipeline.getSettings().contourIntersection = ContourIntersectionDirection.Up;

        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2019Image.kCargoStraightDark72in_HighRes),
                        TestUtils.WPI2019Image.FOV);

        TestUtils.showImage(frameProvider.get().image.getMat(), "Pipeline input", 1);

        CVPipelineResult pipelineResult;

        pipelineResult = pipeline.run(frameProvider.get());
        printTestResults(pipelineResult);

        Assertions.assertTrue(pipelineResult.hasTargets());
        Assertions.assertEquals(2, pipelineResult.targets.size(), "Target count wrong!");

        TestUtils.showImage(pipelineResult.outputFrame.image.getMat(), "Pipeline output");
    }

    @Test
    public void test2020() {
        TestUtils.loadLibraries();
        var pipeline = new ReflectivePipeline();

        pipeline.getSettings().hsvHue.set(60, 100);
        pipeline.getSettings().hsvSaturation.set(200, 255);
        pipeline.getSettings().hsvValue.set(200, 255);
        pipeline.getSettings().outputShowThresholded = true;

        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2020Image.kBlueGoal_108in_Center),
                        TestUtils.WPI2020Image.FOV);

        CVPipelineResult pipelineResult = pipeline.run(frameProvider.get());
        printTestResults(pipelineResult);

        TestUtils.showImage(pipelineResult.outputFrame.image.getMat(), "Pipeline output");
    }

    // used to run VisualVM for profiling. It won't run on unit tests.
    public static void main(String[] args) {
        Logger.setLevel(LogGroup.VisionProcess, Level.TRACE);
        TestUtils.loadLibraries();
        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2019Image.kCargoStraightDark72in_HighRes),
                        TestUtils.WPI2019Image.FOV);

        var pipeline = new ReflectivePipeline();
        pipeline.getSettings().hsvHue.set(60, 100);
        pipeline.getSettings().hsvSaturation.set(100, 255);
        pipeline.getSettings().hsvValue.set(190, 255);
        pipeline.getSettings().outputShowThresholded = true;
        pipeline.getSettings().outputShowMultipleTargets = true;
        pipeline.getSettings().contourGroupingMode = ContourGroupingMode.Dual;
        pipeline.getSettings().contourIntersection = ContourIntersectionDirection.Up;

        //noinspection InfiniteLoopStatement
        while (true) {
            CVPipelineResult pipelineResult = pipeline.run(frameProvider.get());
            printTestResults(pipelineResult);
            pipelineResult.release();
        }
    }

    private static void printTestResults(CVPipelineResult pipelineResult) {
        double fps = 1000 / pipelineResult.getLatencyMillis();
        logger.debug("Pipeline ran in " + pipelineResult.getLatencyMillis() + "ms (" + fps + " fps)");
//        System.out.println("Found " + pipelineResult.targets.size() + " valid targets");
    }
}
