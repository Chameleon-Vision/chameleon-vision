package com.chameleonvision.scripting;

import com.chameleonvision.util.ShellExec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ScriptEvent {
    private static final ShellExec executor = new ShellExec(true, true);

    public final ScriptConfig config;

    public ScriptEvent(ScriptConfig config) {
        this.config = config;
    }

    public int run() throws IOException {
        List<String> fullArgs = new ArrayList<>();
        String command;
        if (config.commandType == ScriptCommandType.kDefault || config.commandType == ScriptCommandType.kBashScript) {
            command = config.path;
        } else {

        }
        fullArgs.add(config.path);
        fullArgs.addAll(Arrays.asList(config.arguments));
        int retVal = executor.execute(config.commandType.value, fullArgs.toArray(new String[0]));

        String output = executor.getOutput();
        String error = executor.getError();

        if (!error.isEmpty()) {
            System.err.printf("Error when running \"%s\" script: %s\n", config.eventType.name(), error);
        } else if (!output.isEmpty()) {
            System.out.printf("Output from \"%s\" script: %s\n", config.eventType.name(), output);
        }
        return retVal;
    }
}
