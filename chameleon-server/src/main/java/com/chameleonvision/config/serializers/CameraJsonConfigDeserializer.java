package com.chameleonvision.config.serializers;

import com.chameleonvision.config.CameraJsonConfig;
import com.chameleonvision.config.GeneralSettings;
import com.chameleonvision.network.NetworkIPMode;
import com.chameleonvision.vision.enums.StreamDivisor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;

import java.io.IOException;

public class CameraJsonConfigDeserializer extends BaseDeserializer<CameraJsonConfig>  {
    public CameraJsonConfigDeserializer() {
        this(null);
    }

    public CameraJsonConfigDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public CameraJsonConfig deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        System.out.println("[CameraJsonConfigDeserializer] reading settings...");

        baseNode = jsonParser.getCodec().readTree(jsonParser);

        var fov = getDouble("fov", 70.0);
        var path = getString("path", "");
        var name = getString("name", "");
        var nickname = getString("nickname", "");
        var videomode = getInt("videomode", 0);
        var streamDivisor = getEnum("streamDivisor", StreamDivisor.class, StreamDivisor.NONE);
        var tilt = getDouble("tilt", 0.0);

        return new CameraJsonConfig(fov, path, name, nickname, videomode, streamDivisor, tilt);
    }
}
