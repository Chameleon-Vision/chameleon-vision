package com.chameleonvision.common;

import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.util.math.MathUtils;
import com.chameleonvision.common.util.numbers.NumberListUtils;
import com.chameleonvision.common.vision.frame.FrameProvider;
import com.chameleonvision.common.vision.frame.provider.FileFrameProvider;
import com.chameleonvision.common.vision.opencv.CVMat;
import com.chameleonvision.common.vision.opencv.ContourGroupingMode;
import com.chameleonvision.common.vision.opencv.ContourIntersectionDirection;
import com.chameleonvision.common.vision.pipeline.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Various tests that check performance on long-running tasks (i.e. a pipeline) */
public class BenchmarkTest {
    @BeforeAll
    public static void init() {
        TestUtils.loadLibraries();
    }

    @Test
    public void Reflective240pBenchmark() {
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
                        TestUtils.getWPIImagePath(TestUtils.WPI2019Image.kCargoSideStraightDark72in),
                        TestUtils.WPI2019Image.FOV);

        frameProvider.setImageReloading(true);

        benchmarkPipeline(frameProvider, pipeline, 5);
    }

    @Test
    public void Reflective480pBenchmark() {
        var pipeline = new ReflectivePipeline();
        pipeline.getSettings().hsvHue.set(60, 100);
        pipeline.getSettings().hsvSaturation.set(200, 255);
        pipeline.getSettings().hsvValue.set(200, 255);
        pipeline.getSettings().outputShowThresholded = true;

        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2020Image.kBlueGoal_084in_Center),
                        TestUtils.WPI2020Image.FOV);

        frameProvider.setImageReloading(true);

        benchmarkPipeline(frameProvider, pipeline, 5);
    }

    @Test
    public void Reflective720pBenchmark() {
        var pipeline = new ReflectivePipeline();
        pipeline.getSettings().hsvHue.set(60, 100);
        pipeline.getSettings().hsvSaturation.set(200, 255);
        pipeline.getSettings().hsvValue.set(200, 255);
        pipeline.getSettings().outputShowThresholded = true;

        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2020Image.kBlueGoal_084in_Center_720p),
                        TestUtils.WPI2020Image.FOV);

        frameProvider.setImageReloading(true);

        benchmarkPipeline(frameProvider, pipeline, 5);
    }

    @Test
    public void Reflective1920x1440Benchmark() {
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

        frameProvider.setImageReloading(true);

        benchmarkPipeline(frameProvider, pipeline, 5);
    }

    private static <P extends CVPipeline> void benchmarkPipeline(
            FrameProvider frameProvider, P pipeline, int secondsToRun) {
        CVMat.enablePrint(false);
        // warmup for 5 loops.
        System.out.println("Warming up for 5 loops...");
        for (int i = 0; i < 5; i++) {
            pipeline.run(frameProvider.get());
        }

        final List<Double> processingTimes = new ArrayList<>();
        final List<Double> latencyTimes = new ArrayList<>();

        var frameProps = frameProvider.get().frameStaticProperties;

        // begin benchmark
        System.out.println(
                "Beginning "
                        + secondsToRun
                        + " second benchmark at resolution "
                        + frameProps.imageWidth
                        + "x"
                        + frameProps.imageHeight);
        var benchmarkStartMillis = System.currentTimeMillis();
        do {
            CVPipelineResult pipelineResult = pipeline.run(frameProvider.get());
            pipelineResult.release();
            processingTimes.add(pipelineResult.processingMillis);
            latencyTimes.add(pipelineResult.getLatencyMillis());
        } while (System.currentTimeMillis() - benchmarkStartMillis < secondsToRun * 1000);
        System.out.println("Benchmark complete.");

        var processingMin = Collections.min(processingTimes);
        var processingMean = NumberListUtils.mean(processingTimes);
        var processingMax = Collections.max(processingTimes);

        var latencyMin = Collections.min(latencyTimes);
        var latencyMean = NumberListUtils.mean(latencyTimes);
        var latencyMax = Collections.max(latencyTimes);

        String processingResult =
                "Processing times - "
                        + "Min: "
                        + MathUtils.roundTo(processingMin, 3)
                        + "ms ("
                        + MathUtils.roundTo(1000 / processingMin, 3)
                        + " FPS), "
                        + "Mean: "
                        + MathUtils.roundTo(processingMean, 3)
                        + "ms ("
                        + MathUtils.roundTo(1000 / processingMean, 3)
                        + " FPS), "
                        + "Max: "
                        + MathUtils.roundTo(processingMax, 3)
                        + "ms ("
                        + MathUtils.roundTo(1000 / processingMax, 3)
                        + " FPS)";
        System.out.println(processingResult);
        String latencyResult =
                "Latency times - "
                        + "Min: "
                        + MathUtils.roundTo(latencyMin, 3)
                        + "ms ("
                        + MathUtils.roundTo(1000 / latencyMin, 3)
                        + " FPS), "
                        + "Mean: "
                        + MathUtils.roundTo(latencyMean, 3)
                        + "ms ("
                        + MathUtils.roundTo(1000 / latencyMean, 3)
                        + " FPS), "
                        + "Max: "
                        + MathUtils.roundTo(latencyMax, 3)
                        + "ms ("
                        + MathUtils.roundTo(1000 / latencyMax, 3)
                        + " FPS)";
        System.out.println(latencyResult);
    }
}
