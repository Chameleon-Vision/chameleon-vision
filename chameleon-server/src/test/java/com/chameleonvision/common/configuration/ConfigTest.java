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

    private static final ConfigManager configMgr = ConfigManager.getInstance();
    private static final CameraConfiguration cameraConfig = new CameraConfiguration();
    private static final ReflectivePipelineSettings reflectivePipelineSettings =
            new ReflectivePipelineSettings();
    private static final ColoredShapePipelineSettings coloredShapePipelineSettings =
            new ColoredShapePipelineSettings();

    @BeforeAll
    public static void init() {
        TestUtils.loadLibraries();
        Logger.setLevel(LogGroup.General, Level.DE_PEST);

        cameraConfig.name = "TestCamera";

        reflectivePipelineSettings.pipelineNickname = "2019Tape";
        reflectivePipelineSettings.targetModel = TargetModel.get2019Target();

        coloredShapePipelineSettings.pipelineNickname = "2019Cargo";
        coloredShapePipelineSettings.pipelineIndex = 1;

        cameraConfig.addPipelineSetting(reflectivePipelineSettings);
        cameraConfig.addPipelineSetting(coloredShapePipelineSettings);
    }

    @Test
    @Order(1)
    public void serializeConfig() throws IOException {
        TestUtils.loadLibraries();
        JacksonUtils.serializer(Path.of("settings.json"), reflectivePipelineSettings);

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
