package com.chameleonvision.config.serializers;

import com.chameleonvision.config.GeneralSettings;
import com.chameleonvision.vision.pipeline.impl.StandardCVPipelineSettings;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class GeneralSettingsSerializer extends BaseSerializer<GeneralSettings>  {
    public GeneralSettingsSerializer() {
        this(null);
    }

    public GeneralSettingsSerializer(Class<GeneralSettings> t) {
        super(t);
    }

    @Override
    public void serialize(GeneralSettings s, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        System.out.println("[GeneralSettingsSerializer] saving settings...");

        // set BaseSerializer generator reference.
        generator = gen;

        gen.writeStartObject();

        gen.writeNumberField("teamNumber", s.teamNumber);
        writeEnum("connectionType", s.connectionType);
        gen.writeStringField("ip", s.ip);
        gen.writeStringField("gateway", s.gateway);
        gen.writeStringField("netmask", s.netmask);
        gen.writeStringField("hostname", s.hostname);
        gen.writeStringField("currentCamera", s.currentCamera);
        gen.writeNumberField("currentPipeline", s.currentPipeline);

        gen.writeEndObject();
    }
}
