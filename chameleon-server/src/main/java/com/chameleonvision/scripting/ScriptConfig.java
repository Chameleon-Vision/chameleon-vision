package com.chameleonvision.scripting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ScriptConfig {
    public final ScriptEventType eventType;
    public final ScriptCommandType commandType;
    public final String path;
    public final String[] arguments;

    public ScriptConfig(ScriptEventType eventType) {
        this.eventType = eventType;
        this.commandType = ScriptCommandType.kDefault;
        this.path = "";
        this.arguments = new String[]{""};
    }

    @JsonCreator
    public ScriptConfig(
            @JsonProperty("eventType") ScriptEventType eventType,
            @JsonProperty("commandType") ScriptCommandType commandType,
            @JsonProperty("path") String path,
            @JsonProperty("arguments") String[] arguments
    ) {
        this.eventType = eventType;
        this.commandType = commandType;
        this.path = path;
        this.arguments = arguments;
    }
}
