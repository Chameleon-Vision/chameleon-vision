package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.configuration.CameraConfiguration;
import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.vision.frame.FrameProvider;
import com.chameleonvision.common.vision.frame.provider.FileFrameProvider;
import com.chameleonvision.common.vision.pipeline.CVPipelineResult;
import edu.wpi.cscore.VideoMode;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.*;

public class VisionModuleManagerTest {

    @BeforeEach
    public void init() {
        TestUtils.loadLibraries();
    }

    private static class TestSource implements VisionSource {

        private final FrameProvider provider;
        private final CameraConfiguration config;

        public TestSource(FrameProvider provider) {
            this.config = new CameraConfiguration("", "", "", "");
            this.provider = provider;
        }

        @Override
        public FrameProvider getFrameProvider() {
            return provider;
        }

        @Override
        public VisionSourceSettables getSettables() {
            return new TestSettables(config);
        }

        @Override
        public CameraConfiguration getCameraConfiguration() {
            return config;
        }
    }

    private static class TestSettables extends VisionSourceSettables {

        protected TestSettables(CameraConfiguration configuration) {
            super(configuration);
        }

        @Override
        public int getExposure() {
            return 0;
        }

        @Override
        public void setExposure(int exposure) {}

        @Override
        public int getBrightness() {
            return 0;
        }

        @Override
        public void setBrightness(int brightness) {}

        @Override
        public int getGain() {
            return 0;
        }

        @Override
        public void setGain(int gain) {}

        @Override
        public VideoMode getCurrentVideoMode() {
            return new VideoMode(0,320, 240, 30);

        }

        @Override
        public void setCurrentVideoMode(VideoMode videoMode) {}

        @Override
        public HashMap<Integer, VideoMode> getAllVideoModes() {
            return null;
        }
    }

    private static class TestDataConsumer implements Observer<CVPipelineResult> {
        CVPipelineResult result;

        @Override
        public void onSubscribe(@NonNull Disposable d) {}

        @Override
        public void onNext(@NonNull CVPipelineResult o) {
            this.result = o;
        }

        @Override
        public void onError(@NonNull Throwable e) {}

        @Override
        public void onComplete() {}
    }

    @Test
    public void setupManager() {
        var sources = new ArrayList<VisionSource>();
        sources.add(
                new TestSource(
                        new FileFrameProvider(
                                TestUtils.getWPIImagePath(TestUtils.WPI2019Image.kCargoStraightDark72in_HighRes),
                                TestUtils.WPI2019Image.FOV)));

        var moduleManager = new VisionModuleManager(sources);
        var module0DataConsumer = new TestDataConsumer();

        moduleManager.visionModules.get(0).addDataConsumer(module0DataConsumer);

        moduleManager.startModules();

        sleep(500);

        Assertions.assertNotNull(module0DataConsumer.result);
        Assertions.assertNotNull(module0DataConsumer.result);
        printTestResults(module0DataConsumer.result);
    }

    private static void printTestResults(CVPipelineResult pipelineResult) {
        double fps = 1000 / pipelineResult.getLatencyMillis();
        System.out.print(
                "Pipeline ran in " + pipelineResult.getLatencyMillis() + "ms (" + fps + " fps), ");
        System.out.println("Found " + pipelineResult.targets.size() + " valid targets");
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignored
        }
    }
}
