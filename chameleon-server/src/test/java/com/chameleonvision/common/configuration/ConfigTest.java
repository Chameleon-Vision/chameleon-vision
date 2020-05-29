package com.chameleonvision.common.configuration;

import com.chameleonvision.common.logging.Level;
import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.util.file.JacksonUtils;
import com.chameleonvision.common.vision.pipeline.CVPipelineSettings;
import com.chameleonvision.common.vision.pipeline.ColoredShapePipelineSettings;
import com.chameleonvision.common.vision.pipeline.ReflectivePipelineSettings;
import com.chameleonvision.common.vision.target.TargetModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigTest {

    @Test
    public void testConfig() throws IOException {
        TestUtils.loadLibraries();

        Logger.setLevel(LogGroup.General, Level.DE_PEST);
        var config = ConfigManager.getInstance();
        var camConfig = new CameraConfiguration();
        camConfig.name = "meme";

        camConfig.addPipelineSetting(new ReflectivePipelineSettings());
        ((ReflectivePipelineSettings) camConfig.pipelineSettings.get(0)).targetModel =
                TargetModel.get2019Target();
        camConfig.addPipelineSetting(new ColoredShapePipelineSettings());
        config.getConfig().addCameraConfig(camConfig);
        config.save();

        config.load();

        Assertions.assertTrue(
                config.getConfig().getCameraConfigurations().get("meme").pipelineSettings.get(0)
                        instanceof ReflectivePipelineSettings);
        Assertions.assertTrue(
                config.getConfig().getCameraConfigurations().get("meme").pipelineSettings.get(1)
                        instanceof ColoredShapePipelineSettings);
    }
}
