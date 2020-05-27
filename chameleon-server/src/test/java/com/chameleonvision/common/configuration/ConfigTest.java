package com.chameleonvision.common.configuration;

import java.io.IOException;
import java.nio.file.Path;

import com.chameleonvision.common.logging.Level;
import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.util.jackson.JacksonUtils;
import com.chameleonvision.common.vision.pipeline.ReflectivePipeline;
import com.chameleonvision.common.vision.pipeline.ReflectivePipelineSettings;
import com.chameleonvision.common.vision.target.TargetModel;
import org.junit.jupiter.api.Test;

public class ConfigTest {

    @Test
    public void testConfig() throws IOException {
        TestUtils.loadLibraries();

        Logger.setLevel(LogGroup.General, Level.DE_PEST);
        var config = ConfigManager.getInstance();
        var camConfig = new CameraConfiguration();
        camConfig.name = "meme";

        var s = new ReflectivePipelineSettings();
        s.targetModel = TargetModel.get2019Target();
        JacksonUtils.serializer(Path.of("settings.json"), s);
        JacksonUtils.deserialize(Path.of("settings.json"), ReflectivePipeline.class);

        if(true) return;

        camConfig.addPipelineSetting(new ReflectivePipelineSettings());
        ((ReflectivePipelineSettings) camConfig.pipelineSettings.get(0)).targetModel = TargetModel.get2019Target();
//        camConfig.addPipelineSetting(new ColoredShapePipelineSettings());
        config.getConfig().addCameraConfig(camConfig);
        config.save();

        config.load();

        System.out.println(config.getConfig().getCameraConfigurations().get("meme").pipelineSettings);
    }
}
