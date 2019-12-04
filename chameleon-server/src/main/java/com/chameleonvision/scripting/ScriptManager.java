package com.chameleonvision.scripting;

import com.chameleonvision.config.ConfigManager;
import com.chameleonvision.util.JacksonHelper;
import com.chameleonvision.util.LoopingRunnable;
import com.chameleonvision.util.ProgramDirectoryUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class ScriptManager {

    private ScriptManager() {}

    private static final List<ScriptEvent> events = new ArrayList<>();
    private static final LinkedBlockingDeque<ScriptEventType> queuedEvents = new LinkedBlockingDeque<>(25);

    public void initialize() {
        ScriptConfigManager.initialize();
        if (ScriptConfigManager.fileExists()) {
            ScriptConfigManager.loadConfig().stream().map(ScriptEvent::new).forEach(events::add);
        } else {
            System.err.println("Something went wrong initializing scripts!");
        }

        new Thread(new ScriptRunner(10L)).start();
    }

    private static class ScriptRunner extends LoopingRunnable {

        ScriptRunner(Long loopTimeMs) {
            super(loopTimeMs);
        }

        @Override
        protected void process() {
            try {
                handleEvent(queuedEvents.takeFirst());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void handleEvent(ScriptEventType eventType) {
            var toRun = events.parallelStream().filter(e -> e.config.eventType == eventType).findFirst().orElse(null);
            if (toRun != null) {
                try {
                    toRun.run();
                } catch (IOException e) {
                    System.err.printf("Failed to run script for event: %s, exception below.\n%s\n", eventType.value, e.getMessage());
                }
            }
        }
    }

    protected static class ScriptConfigManager {

        protected static final Path scriptConfigPath = Paths.get(ConfigManager.SettingsPath.toString(), "scripts.json");

        private ScriptConfigManager() {}

        static boolean fileExists() { return Files.exists(scriptConfigPath); }

        public static void initialize() {
            if (!fileExists()) {
                List<ScriptConfig> eventsConfig = new ArrayList<>();
                for (var eventType : ScriptEventType.values()) {
                    eventsConfig.add(new ScriptConfig(eventType));
                }

                try {
                    JacksonHelper.serializer(scriptConfigPath, eventsConfig.toArray(new ScriptConfig[0]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        static List<ScriptConfig> loadConfig() {
            try {
                var raw = JacksonHelper.deserializer(scriptConfigPath, ScriptConfig[].class);
                if (raw != null) {
                    return List.of(raw);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

        protected static void deleteConfig() {
            try {
                Files.delete(scriptConfigPath);
            } catch (IOException e) {
                //
            }
        }
    }

    public static void queueEvent(ScriptEventType eventType) {
        try {
            queuedEvents.putLast(eventType);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
