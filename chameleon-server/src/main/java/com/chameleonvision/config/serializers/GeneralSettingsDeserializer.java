package com.chameleonvision.config.serializers;

import com.chameleonvision.config.GeneralSettings;
import com.chameleonvision.network.NetworkIPMode;
import com.chameleonvision.vision.pipeline.impl.StandardCVPipelineSettings;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;

import java.io.IOException;

public class GeneralSettingsDeserializer extends BaseDeserializer<GeneralSettings>  {
    public GeneralSettingsDeserializer() {
        this(null);
    }

    public GeneralSettingsDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public GeneralSettings deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        System.out.println("[GeneralSettingsDeserializer] reading settings...");

        baseNode = jsonParser.getCodec().readTree(jsonParser);

        GeneralSettings settings = new GeneralSettings();
        settings.teamNumber = getInt("teamNumber", 1577);
        settings.connectionType = getEnum("connectionType", NetworkIPMode.class, NetworkIPMode.DHCP);
        settings.ip = getString("ip", "");
        settings.gateway = getString("gateway", "");
        settings.netmask = getString("netmask", "");
        settings.hostname = getString("hostname", "Chameleon-vision");
        settings.currentCamera = getString("currentCamera", "");
        settings.currentPipeline = getInt("currentPipeline", -1);

        return settings;
    }
}
