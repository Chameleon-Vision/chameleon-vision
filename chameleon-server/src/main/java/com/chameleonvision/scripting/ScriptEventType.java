package com.chameleonvision.scripting;

public enum ScriptEventType {
    kProgramInit("Program Init"),
    kNTConnected("NT Connected"),
    kEnterDriverMode("Enter Driver Mode"),
    kExitDriverMode("Exit Driver Mode"),
    kFoundTarget("Found Target"),
    kFoundMultipleTarget("Found Multiple Target"),
    kLostTarget("Lost Target"),
    kPipelineLag("Pipeline Lag");

    public final String value;

    ScriptEventType(String value) {
        this.value = value;
    }
}
