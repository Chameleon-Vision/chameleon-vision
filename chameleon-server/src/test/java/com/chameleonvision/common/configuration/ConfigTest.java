package com.chameleonvision.common.configuration;

import com.chameleonvision.common.logging.Level;
import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.util.file.JacksonUtils;
import com.chameleonvision.common.vision.pipeline.ColoredShapePipelineSettings;
import com.chameleonvision.common.vision.pipeline.ReflectivePipelineSettings;
import com.chameleonvision.common.vision.target.TargetModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.*;

public class ConfigTest {

    private static final ConfigManager configMgr;
    private static final CameraConfiguration cameraConfig =
            new CameraConfiguration("TestCamera", "/dev/video420");
    private static final ReflectivePipelineSettings REFLECTIVE_PIPELINE_SETTINGS =
            new ReflectivePipelineSettings();
    private static final ColoredShapePipelineSettings COLORED_SHAPE_PIPELINE_SETTINGS =
            new ColoredShapePipelineSettings();

    static {
        TestUtils.loadLibraries();
        configMgr = ConfigManager.getInstance();
    }

    @BeforeAll
    public static void init() {
        TestUtils.loadLibraries();
        Logger.setLevel(LogGroup.General, Level.DE_PEST);

        REFLECTIVE_PIPELINE_SETTINGS.pipelineNickname = "2019Tape";
        REFLECTIVE_PIPELINE_SETTINGS.targetModel = TargetModel.get2019Target();

        COLORED_SHAPE_PIPELINE_SETTINGS.pipelineNickname = "2019Cargo";
        COLORED_SHAPE_PIPELINE_SETTINGS.pipelineIndex = 1;

        cameraConfig.addPipelineSetting(REFLECTIVE_PIPELINE_SETTINGS);
        cameraConfig.addPipelineSetting(COLORED_SHAPE_PIPELINE_SETTINGS);
    }

    @Test
    @Order(1)
    public void serializeConfig() throws IOException {
        TestUtils.loadLibraries();
        JacksonUtils.serializer(Path.of("settings.json"), REFLECTIVE_PIPELINE_SETTINGS);

        Logger.setLevel(LogGroup.General, Level.DE_PEST);
        configMgr.getConfig().addCameraConfig(cameraConfig);
        configMgr.save();

        var camConfDir =
                new File(
                        Path.of(ConfigManager.getRootFolder().toString(), "cameras", "TestCamera")
                                .toAbsolutePath()
                                .toString());
        Assertions.assertTrue(camConfDir.exists(), "TestCamera config folder not found!");

        Assertions.assertTrue(
                Files.exists(Path.of(ConfigManager.getRootFolder().toString(), "hardwareConfig.json")),
                "hardwareConfig.json file not found!");
        Assertions.assertTrue(
                Files.exists(Path.of(ConfigManager.getRootFolder().toString(), "networkSettings.json")),
                "networkSettings.json file not found!");
    }

    @Test
    @Order(2)
    public void deserializeConfig() {
        configMgr.load();

        var reflectivePipelineSettings =
                configMgr.getConfig().getCameraConfigurations().get("TestCamera").pipelineSettings.get(0);
        var coloredShapePipelineSettings =
                configMgr.getConfig().getCameraConfigurations().get("TestCamera").pipelineSettings.get(1);

        Assertions.assertEquals(REFLECTIVE_PIPELINE_SETTINGS, reflectivePipelineSettings);
        Assertions.assertEquals(COLORED_SHAPE_PIPELINE_SETTINGS, coloredShapePipelineSettings);

        Assertions.assertTrue(
                reflectivePipelineSettings instanceof ReflectivePipelineSettings,
                "Conig loaded pipeline settings for index 0 not of expected type ReflectivePipelineSettings!");
        Assertions.assertTrue(
                coloredShapePipelineSettings instanceof ColoredShapePipelineSettings,
                "Conig loaded pipeline settings for index 1 not of expected type ColoredShapePipelineSettings!");
    }

    @AfterAll
    public static void cleanup() {
        try {
            Files.deleteIfExists(Paths.get("settings.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        new File(ConfigManager.getRootFolder().toAbsolutePath().toString()).delete();
    }
}
