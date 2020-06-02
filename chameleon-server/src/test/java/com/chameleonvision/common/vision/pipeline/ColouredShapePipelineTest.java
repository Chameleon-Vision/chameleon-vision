package com.chameleonvision.common.vision.pipeline;


import com.chameleonvision.common.configuration.ConfigManager;
import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.provider.FileFrameProvider;
import com.chameleonvision.common.vision.opencv.CVMat;
import com.chameleonvision.common.vision.opencv.ContourGroupingMode;
import com.chameleonvision.common.vision.opencv.ContourIntersectionDirection;
import com.chameleonvision.common.vision.opencv.ContourShape;

public class ColouredShapePipelineTest {

    private static void continuouslyRunPipeline(Frame frame, ColouredShapePipelineSettings settings) {
        var pipeline = new ColouredShapePipeline();

        while (true) {
            CVPipelineResult pipelineResult = pipeline.run(frame);
            printTestResults(pipelineResult);
            int preRelease = CVMat.getMatCount();
            pipelineResult.release();
            int postRelease = CVMat.getMatCount();

            System.out.printf("Pre: %d, Post: %d\n", preRelease, postRelease);
        }
    }

    public static void main(String[] args){
        TestUtils.loadLibraries();
        var frameProvider =
                new FileFrameProvider(
                        "D:\\chameleon-vision\\chameleon-server\\src\\test\\resources\\polygons\\polygons2.png",
                        TestUtils.WPI2019Image.FOV);
        var settings = new ColouredShapePipelineSettings();
        settings.hsvHue.set(60, 100);
        settings.hsvSaturation.set(100, 255);
        settings.hsvValue.set(190, 255);
        settings.outputShowThresholded = true;
        settings.outputShowMultipleTargets = true;
        settings.contourGroupingMode = ContourGroupingMode.Dual;
        settings.contourIntersection = ContourIntersectionDirection.Up;
        settings.desiredShape = ContourShape.Quadrilateral;
        settings.accuracyPercentage = 20.0;

        continuouslyRunPipeline(frameProvider.get(), settings);

    }

    private static void printTestResults(CVPipelineResult pipelineResult) {
        double fps = 1000 / pipelineResult.getLatencyMillis();
        System.out.print(
                "Pipeline ran in " + pipelineResult.getLatencyMillis() + "ms (" + fps + " fps), ");
        System.out.println("Found " + pipelineResult.targets.size() + " valid targets");
    }


}
