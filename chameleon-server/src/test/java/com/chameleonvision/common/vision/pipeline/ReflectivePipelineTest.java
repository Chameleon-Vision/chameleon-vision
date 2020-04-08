package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.provider.FileFrameProvider;
import com.chameleonvision.common.vision.opencv.ContourGroupingMode;
import com.chameleonvision.common.vision.opencv.ContourIntersectionDirection;
import org.junit.jupiter.api.Test;

public class ReflectivePipelineTest {

    @Test
    public void test2019() {
        TestUtils.loadLibraries();
        var pipeline = new ReflectivePipeline();

        var settings = new ReflectivePipelineSettings();
        settings.hsvHue.set(60, 100);
        settings.hsvSaturation.set(100, 255);
        settings.hsvValue.set(190, 255);
        settings.outputShowThresholded = true;
        settings.outputShowMultipleTargets = true;
        settings.contourGroupingMode = ContourGroupingMode.Dual;
        settings.contourIntersection = ContourIntersectionDirection.Up;

        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2019Image.kCargoStraightDark72in_HighRes),
                        TestUtils.WPI2019Image.FOV);

        TestUtils.showImage(frameProvider.getFrame().image, "Pipeline input", 1);

        CVPipelineResult pipelineResult;

        pipelineResult = pipeline.run(frameProvider.getFrame(), settings);
        printTestResults(pipelineResult);

        TestUtils.showImage(pipelineResult.outputFrame.image, "Pipeline output");
    }

    @Test
    public void test2020() {
        TestUtils.loadLibraries();
        var pipeline = new ReflectivePipeline();
        var settings = new ReflectivePipelineSettings();

        settings.hsvHue.set(60, 100);
        settings.hsvSaturation.set(200, 255);
        settings.hsvValue.set(200, 255);
        settings.outputShowThresholded = true;

        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2020Image.kBlueGoal_108in_Center),
                        TestUtils.WPI2020Image.FOV);

        CVPipelineResult pipelineResult = pipeline.run(frameProvider.getFrame(), settings);
        printTestResults(pipelineResult);

        TestUtils.showImage(pipelineResult.outputFrame.image, "Pipeline output");
    }

    private static void continuouslyRunPipeline(Frame frame, ReflectivePipelineSettings settings) {
        var pipeline = new ReflectivePipeline();

        while (true) {
            CVPipelineResult pipelineResult = pipeline.run(frame, settings);
            printTestResults(pipelineResult);
            pipelineResult.release();
        }
    }

    // used to run VisualVM for profiling. It won't run on unit tests.
    public static void main(String[] args) {
        TestUtils.loadLibraries();
        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2019Image.kCargoStraightDark72in_HighRes),
                        TestUtils.WPI2019Image.FOV);

        var settings = new ReflectivePipelineSettings();
        settings.hsvHue.set(60, 100);
        settings.hsvSaturation.set(100, 255);
        settings.hsvValue.set(190, 255);
        settings.outputShowThresholded = true;
        settings.outputShowMultipleTargets = true;
        settings.contourGroupingMode = ContourGroupingMode.Dual;
        settings.contourIntersection = ContourIntersectionDirection.Up;

        continuouslyRunPipeline(frameProvider.getFrame(), settings);
    }

    private static void printTestResults(CVPipelineResult pipelineResult) {
        double fps = 1000 / pipelineResult.getLatencyMillis();
        System.out.print(
                "Pipeline ran in " + pipelineResult.getLatencyMillis() + "ms (" + fps + " fps), ");
        System.out.println("Found " + pipelineResult.targets.size() + " valid targets");
    }
}
