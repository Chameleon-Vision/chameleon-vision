package com.chameleonvision.scripting;

import com.chameleonvision.config.ConfigManager;
import com.chameleonvision.util.ProgramDirectoryUtilities;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScriptManager {

    private ScriptManager() {}

    public void initialize() {
        if (ScriptConfigManager.initialize()) {
            for (var eventType : ScriptEventType.values()) {
                new ScriptConfig(eventType);
            }
        }
    }

    private static class ScriptConfigManager {

        private static final Path scriptConfigPath = Paths.get(ConfigManager., "scripts.json");

        private ScriptConfigManager() {}

        public static boolean fileExists() { return Files.exists(scriptConfigPath); }

        public static boolean initialize() {
            if (Files.exists(scriptConfigPath)) {
                return true;
            } else return new File(scriptConfigPath.toString()).mkdirs();
        }

        public static void createFolder() {
            new File(scriptConfigPath.toString()).mkdirs();
        }

        public static void writeBlankScriptConfig() {
            createFolder();
        }
    }
}
