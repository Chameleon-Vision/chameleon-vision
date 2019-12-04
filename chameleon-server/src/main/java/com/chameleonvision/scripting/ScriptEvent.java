package com.chameleonvision.scripting;

import com.chameleonvision.util.ShellExec;

import java.io.IOException;

public class ScriptEvent {
    private static final ShellExec executor = new ShellExec(true, true);

    public final ScriptConfig config;

    public ScriptEvent(ScriptConfig config) {
        this.config = config;
    }

    public int run() throws IOException {
        return executor.execute(config.command, config.arguments);
    }
}
