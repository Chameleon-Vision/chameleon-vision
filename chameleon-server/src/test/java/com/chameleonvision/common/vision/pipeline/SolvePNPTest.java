package com.chameleonvision.common.vision.pipeline;

import java.io.IOException;
import java.nio.file.Path;

import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.util.numbers.DoubleCouple;
import com.chameleonvision.common.vision.camera.CameraCalibrationCoefficients;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.provider.FileFrameProvider;
import com.chameleonvision.common.vision.opencv.CVMat;
import com.chameleonvision.common.vision.opencv.ContourGroupingMode;
import com.chameleonvision.common.vision.opencv.ContourIntersectionDirection;
import com.chameleonvision.common.vision.target.TargetModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import org.junit.jupiter.api.Test;

public class SolvePNPTest {

    @Test
    public void meme() throws IOException {
        TestUtils.loadLibraries();

        var lowres = (Path.of(TestUtils.getCalibrationPath().toString(), "lifecamcal.json")
                .toFile());
        var cal1 = new ObjectMapper().readValue(lowres, CameraCalibrationCoefficients.class);

        var highres = (Path.of(TestUtils.getCalibrationPath().toString(), "lifecamcal2.json")
                .toFile());
        var cal2 = new ObjectMapper().readValue(highres, CameraCalibrationCoefficients.class);

    }

    private CameraCalibrationCoefficients get640p() {
        try {
            return new ObjectMapper().readValue((Path.of(TestUtils.getCalibrationPath().toString(), "lifecam640p.json")
                    .toFile()), CameraCalibrationCoefficients.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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
        settings.solvePNPEnabled = true;
        settings.contourGroupingMode = ContourGroupingMode.Dual;
        settings.contourIntersection = ContourIntersectionDirection.Up;
        settings.cornerDetectionUseConvexHulls = true;

        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2019Image.kCargoStraightDark48in),
                        TestUtils.WPI2019Image.FOV);

        CVPipelineResult pipelineResult;

        pipelineResult = pipeline.run(frameProvider.getFrame(), settings);

        TestUtils.showImage(pipelineResult.outputFrame.image.getMat(), "Pipeline output",
                1000 * 90);
    }

    @Test
    public void test2020() {
        TestUtils.loadLibraries();
        var pipeline = new ReflectivePipeline();

        var settings = new ReflectivePipelineSettings();
        settings.hsvHue.set(60, 100);
        settings.hsvSaturation.set(100, 255);
        settings.hsvValue.set(60, 255);
        settings.outputShowThresholded = true;
        settings.solvePNPEnabled = true;
        settings.cornerDetectionUseConvexHulls = true;
        settings.cameraCalibration = get640p();
        settings.targetModel = TargetModel.get2020Target();
        settings.cameraPitch = Rotation2d.fromDegrees(0.0);
        settings.contourRatio = new DoubleCouple(2.0, 3.0);

        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2020Image.kBlueGoal_Far_ProtectedZone),
                        TestUtils.WPI2020Image.FOV);

        CVPipelineResult pipelineResult = pipeline.run(frameProvider.getFrame(), settings);
        printTestResults(pipelineResult);

        TestUtils.showImage(pipelineResult.outputFrame.image.getMat(), "Pipeline output", 999999);
    }

    private static void continuouslyRunPipeline(Frame frame, ReflectivePipelineSettings
            settings) {
        var pipeline = new ReflectivePipeline();

        while (true) {
            CVPipelineResult pipelineResult = pipeline.run(frame, settings);
            printTestResults(pipelineResult);
            int preRelease = CVMat.getMatCount();
            pipelineResult.release();
            int postRelease = CVMat.getMatCount();

            System.out.printf("Pre: %d, Post: %d\n", preRelease, postRelease);
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
                "Pipeline ran in " + pipelineResult.getLatencyMillis() + "ms (" + fps + " " +
                        "fps), ");
        System.out.println("Found " + pipelineResult.targets.size() + " valid targets");
    }
}
