package com.chameleonvision.config.serializers;

import com.chameleonvision.config.CameraJsonConfig;
import com.chameleonvision.config.GeneralSettings;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CameraJsonConfigSerializer extends BaseSerializer<CameraJsonConfig>  {
    public CameraJsonConfigSerializer() {
        this(null);
    }

    public CameraJsonConfigSerializer(Class<CameraJsonConfig> t) {
        super(t);
    }

    @Override
    public void serialize(CameraJsonConfig s, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        System.out.println("[CameraJsonConfigSerializer] saving settings...");

        // set BaseSerializer generator reference.
        generator = gen;

        gen.writeStartObject();

        gen.writeNumberField("fov", s.fov);
        gen.writeStringField("path", s.path);
        gen.writeStringField("name", s.name);
        gen.writeStringField("nickname", s.nickname);
        gen.writeNumberField("videomode", s.videomode);
        writeEnum("streamDivisor", s.streamDivisor);
        gen.writeNumberField("tilt", s.tilt);

        gen.writeEndObject();
    }
}
