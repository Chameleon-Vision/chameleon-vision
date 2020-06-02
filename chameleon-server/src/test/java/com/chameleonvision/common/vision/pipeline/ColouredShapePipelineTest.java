package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameStaticProperties;
import com.chameleonvision.common.vision.frame.provider.FileFrameProvider;
import com.chameleonvision.common.vision.opencv.ContourGroupingMode;
import com.chameleonvision.common.vision.opencv.ContourIntersectionDirection;
import com.chameleonvision.common.vision.opencv.ContourShape;
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
                colouredShapePipelineResult.outputFrame.image.getMat(), "Pipeline output: Custom.");
        printTestResults(colouredShapePipelineResult);
    }

    public static void main(String[] args) {
        TestUtils.loadLibraries();
        var frameProvider =
                new FileFrameProvider(
                        "D:\\chameleon-vision\\chameleon-server\\src\\test\\resources\\polygons\\polygons2.png",
                        TestUtils.WPI2019Image.FOV);
        var settings = new ColouredShapePipelineSettings();
        settings.hsvHue.set(0, 100);
        settings.hsvSaturation.set(80, 255);
        settings.hsvValue.set(100, 255);
        settings.outputShowThresholded = true;
        settings.outputShowMultipleTargets = true;
        settings.contourGroupingMode = ContourGroupingMode.Single;
        settings.contourIntersection = ContourIntersectionDirection.Up;
        settings.desiredShape = ContourShape.Triangle;
        settings.accuracyPercentage = 20.0;

        ColouredShapePipeline pipeline = new ColouredShapePipeline();
        testTriangleDetection(
                pipeline, settings, frameProvider.get().frameStaticProperties, frameProvider.get());
        testQuadrilateralDetection(
                pipeline, settings, frameProvider.get().frameStaticProperties, frameProvider.get());
        testCustomShapeDetection(
                pipeline, settings, frameProvider.get().frameStaticProperties, frameProvider.get());
        testCircleShapeDetection(
                pipeline, settings, frameProvider.get().frameStaticProperties, frameProvider.get());
    }

    private static void printTestResults(CVPipelineResult pipelineResult) {
        double fps = 1000 / pipelineResult.getLatencyMillis();
        System.out.print(
                "Pipeline ran in " + pipelineResult.getLatencyMillis() + "ms (" + fps + " fps), ");
        System.out.println("Found " + pipelineResult.targets.size() + " valid targets");
    }
}
