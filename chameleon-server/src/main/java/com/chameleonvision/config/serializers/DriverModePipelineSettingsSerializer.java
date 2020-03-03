package com.chameleonvision.config.serializers;

import com.chameleonvision.vision.pipeline.CVPipelineSettings;
import com.chameleonvision.vision.pipeline.impl.StandardCVPipelineSettings;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class DriverModePipelineSettingsSerializer extends BaseSerializer<CVPipelineSettings> {
    public DriverModePipelineSettingsSerializer() {
        this(null);
    }

    private DriverModePipelineSettingsSerializer(Class<CVPipelineSettings> t) {
        super(t);
    }

    @Override
    public void serialize(CVPipelineSettings pipeline, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // set BaseSerializer generator reference.
        generator = gen;

        gen.writeStartObject();

        gen.writeNumberField("index", pipeline.index);

        writeEnum("flipMode", pipeline.flipMode);
        writeEnum("rotationMode", pipeline.rotationMode);

        gen.writeStringField("nickname", pipeline.nickname);

        gen.writeNumberField("exposure", pipeline.exposure);
        gen.writeNumberField("brightness", pipeline.brightness);
        gen.writeNumberField("gain", pipeline.gain);

        gen.writeNumberField("videoModeIndex", pipeline.videoModeIndex);

        writeEnum("streamDivisor", pipeline.streamDivisor);

        gen.writeEndObject();
    }
}
