package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameStaticProperties;
import com.chameleonvision.common.vision.frame.provider.FileFrameProvider;
import com.chameleonvision.common.vision.opencv.ContourGroupingMode;
import com.chameleonvision.common.vision.opencv.ContourIntersectionDirection;
import com.chameleonvision.common.vision.opencv.ContourShape;
import java.io.File;
import java.util.Objects;
import org.junit.jupiter.api.Test;

public class ColouredShapePipelineTest {

    @Test
    public static void testTriangleDetection(
            ColouredShapePipeline pipeline,
            ColouredShapePipelineSettings settings,
            FrameStaticProperties frameStaticProperties,
            Frame frame) {
        pipeline.setPipeParams(frameStaticProperties, settings);
        CVPipelineResult colouredShapePipelineResult = pipeline.process(frame, settings);
        TestUtils.showImage(
                colouredShapePipelineResult.outputFrame.image.getMat(), "Pipeline output: Triangle.");
        printTestResults(colouredShapePipelineResult);
    }

    @Test
    public static void testQuadrilateralDetection(
            ColouredShapePipeline pipeline,
            ColouredShapePipelineSettings settings,
            FrameStaticProperties frameStaticProperties,
            Frame frame) {
        settings.desiredShape = ContourShape.Quadrilateral;
        pipeline.setPipeParams(frameStaticProperties, settings);
        CVPipelineResult colouredShapePipelineResult = pipeline.process(frame, settings);
        TestUtils.showImage(
                colouredShapePipelineResult.outputFrame.image.getMat(), "Pipeline output: Quadrilateral.");
        printTestResults(colouredShapePipelineResult);
    }

    @Test
    public static void testCustomShapeDetection(
            ColouredShapePipeline pipeline,
            ColouredShapePipelineSettings settings,
            FrameStaticProperties frameStaticProperties,
            Frame frame) {
        settings.desiredShape = ContourShape.Custom;
        pipeline.setPipeParams(frameStaticProperties, settings);
        CVPipelineResult colouredShapePipelineResult = pipeline.process(frame, settings);
        TestUtils.showImage(
                colouredShapePipelineResult.outputFrame.image.getMat(), "Pipeline output: Custom.");
        printTestResults(colouredShapePipelineResult);
    }

    @Test
    public static void testCircleShapeDetection(
            ColouredShapePipeline pipeline,
            ColouredShapePipelineSettings settings,
            FrameStaticProperties frameStaticProperties,
            Frame frame) {
        settings.desiredShape = ContourShape.Circle;
        pipeline.setPipeParams(frameStaticProperties, settings);
        CVPipelineResult colouredShapePipelineResult = pipeline.process(frame, settings);
        TestUtils.showImage(
                colouredShapePipelineResult.outputFrame.image.getMat(), "Pipeline output: Circle.");
        printTestResults(colouredShapePipelineResult);
    }

    @Test
    public static void testPowercellDetection(
            ColouredShapePipelineSettings settings, ColouredShapePipeline pipeline) {
        File[] powerCells =
                new File(
                                Objects.requireNonNull(
                                                ColouredShapePipelineTest.class
                                                        .getClassLoader()
                                                        .getResource("polygons/powercells"))
                                        .getPath()
                                        .substring(1))
                        .listFiles();
        settings.hsvHue.set(10, 40);
        settings.hsvSaturation.set(100, 255);
        settings.hsvValue.set(100, 255);
        settings.maxCannyThresh = 50;
        settings.accuracy = 15;
        settings.allowableThreshold = 5;
        for (File powerCellImage : powerCells) {
            var frameProvider =
                    new FileFrameProvider(powerCellImage.getAbsolutePath(), TestUtils.WPI2019Image.FOV);
            testCircleShapeDetection(
                    pipeline, settings, frameProvider.get().frameStaticProperties, frameProvider.get());
        }
    }

    public static void main(String[] args) {
        TestUtils.loadLibraries();
        var frameProvider =
                new FileFrameProvider(
                        Objects.requireNonNull(
                                        ColouredShapePipelineTest.class
                                                .getClassLoader()
                                                .getResource("polygons/polygons.png"))
                                .getPath()
                                .substring(1),
                        TestUtils.WPI2019Image.FOV);
        var settings = new ColouredShapePipelineSettings();
        settings.hsvHue.set(0, 100);
        settings.hsvSaturation.set(100, 255);
        settings.hsvValue.set(100, 255);
        settings.outputShowThresholded = true;
        settings.outputShowMultipleTargets = true;
        settings.contourGroupingMode = ContourGroupingMode.Single;
        settings.contourIntersection = ContourIntersectionDirection.Up;
        settings.desiredShape = ContourShape.Triangle;
        settings.allowableThreshold = 10;
        settings.accuracyPercentage = 30.0;

        ColouredShapePipeline pipeline = new ColouredShapePipeline();
        testTriangleDetection(
                pipeline, settings, frameProvider.get().frameStaticProperties, frameProvider.get());
        testQuadrilateralDetection(
                pipeline, settings, frameProvider.get().frameStaticProperties, frameProvider.get());
        testCustomShapeDetection(
                pipeline, settings, frameProvider.get().frameStaticProperties, frameProvider.get());
        testCircleShapeDetection(
                pipeline, settings, frameProvider.get().frameStaticProperties, frameProvider.get());
        testPowercellDetection(settings, pipeline);
    }

    private static void printTestResults(CVPipelineResult pipelineResult) {
        double fps = 1000 / pipelineResult.getLatencyMillis();
        System.out.print(
                "Pipeline ran in " + pipelineResult.getLatencyMillis() + "ms (" + fps + " fps), ");
        System.out.println("Found " + pipelineResult.targets.size() + " valid targets");
    }
}