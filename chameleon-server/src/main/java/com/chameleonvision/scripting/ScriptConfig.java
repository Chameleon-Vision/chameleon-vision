package com.chameleonvision.scripting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ScriptConfig {
    public final ScriptEventType eventType;
    public final String command;
    public final String[] arguments;

    public ScriptConfig(ScriptEventType eventType) {
        this.eventType = eventType;
        this.command = "";
        this.arguments = new String[]{""};
    }

    @JsonCreator
    public ScriptConfig(
            @JsonProperty("eventType") ScriptEventType eventType,
            @JsonProperty("command") String command,
            @JsonProperty("arguments") String[] arguments
    ) {
        this.eventType = eventType;
        this.command = command;
        this.arguments = arguments;
    }
}
