package com.chameleonvision.config.serializers;

import com.chameleonvision.vision.enums.*;
import com.chameleonvision.vision.pipeline.CVPipelineSettings;
import com.chameleonvision.vision.pipeline.impl.StandardCVPipelineSettings;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;

import java.io.IOException;

public class DriverModePipelineSettingsDeserializer extends BaseDeserializer<CVPipelineSettings> {
    public DriverModePipelineSettingsDeserializer() {
        this(null);
    }

    private DriverModePipelineSettingsDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public CVPipelineSettings deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        // set BaseDeserializer parser reference.
        baseNode = jsonParser.getCodec().readTree(jsonParser);

        CVPipelineSettings pipeline = new CVPipelineSettings();

        pipeline.index = getInt("index", pipeline.index);

        pipeline.flipMode = getEnum("flipMode", ImageFlipMode.class, pipeline.flipMode);
        pipeline.rotationMode = getEnum("rotationMode", ImageRotationMode.class, pipeline.rotationMode);

        pipeline.nickname = getString("nickname", pipeline.nickname);

        pipeline.exposure = getDouble("exposure", pipeline.exposure);
        pipeline.brightness = getDouble("brightness", pipeline.brightness);
        pipeline.gain = getDouble("gain", pipeline.gain);

        pipeline.videoModeIndex = getInt("videoModeIndex", pipeline.videoModeIndex);

        pipeline.streamDivisor = getEnum("streamDivisor", StreamDivisor.class, pipeline.streamDivisor);

        return pipeline;
    }
}
