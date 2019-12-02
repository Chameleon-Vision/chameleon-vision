package com.chameleonvision.scripting;

import com.chameleonvision.util.ProgramDirectoryUtilities;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ScriptManager {

    private ScriptManager() {}

    public void initialize() {
        ScriptConfigManager.initialize();
        for (var eventType : ScriptEventType.values()) {
            new ScriptConfig(eventType);
        }
    }

    private static class ScriptConfigManager {

        private static final Path scriptConfigPath = Paths.get(ProgramDirectoryUtilities.getProgramDirectory(), "scripts");

        private ScriptConfigManager() {}

        public static void initialize() {
            if ()
        }
    }
}
