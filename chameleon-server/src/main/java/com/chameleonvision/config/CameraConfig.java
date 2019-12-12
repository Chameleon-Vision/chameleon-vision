package com.chameleonvision.config;

import com.chameleonvision.util.JacksonHelper;
import com.chameleonvision.vision.pipeline.CVPipelineSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CameraConfig {

    private static final Path camerasConfigFolderPath = Path.of(ConfigManager.SettingsPath.toString(), "cameras");

    private final CameraJsonConfig preliminaryConfig;
    private final Path configFolderPath;
    private final Path configPath;
    private final Path driverModePath;
    private final Path calibrationPath;

    public final PipelineConfig pipelineConfig;

    CameraConfig(CameraJsonConfig config) {
        preliminaryConfig = config;
        String cameraConfigName = preliminaryConfig.name.replace(' ', '_');
        pipelineConfig = new PipelineConfig(this);

        configFolderPath = Path.of(camerasConfigFolderPath.toString(), cameraConfigName);
        configPath = Path.of(configFolderPath.toString(), "camera.json");
        driverModePath = Path.of(configFolderPath.toString(), "drivermode.json");
        calibrationPath = Path.of(configFolderPath.toString(), "calibration.json");
    }

    public FullCameraConfiguration load() {
        checkFolder();
        checkConfig();
        checkDriverMode();
        checkCalibration();
        pipelineConfig.check();

        return new FullCameraConfiguration(loadConfig(), pipelineConfig.load(), loadDriverMode(), loadCalibration(), this);
    }

    private CameraJsonConfig loadConfig() {
        CameraJsonConfig config = preliminaryConfig;
        try {
            config = JacksonHelper.deserializer(configPath, CameraJsonConfig.class);
        } catch (IOException e) {
            System.err.printf("Failed to load camera config: %s - using default.\n", configPath.toString());
        }
        return config;
    }

    private CVPipelineSettings loadDriverMode() {
        CVPipelineSettings driverMode = new CVPipelineSettings();
        try {
            driverMode = JacksonHelper.deserializer(driverModePath, CVPipelineSettings.class);
        } catch (IOException e) {
            System.err.println("Failed to load camera drivermode: " + driverModePath.toString());
        }
        if (driverMode != null) {
            driverMode.nickname = "DRIVERMODE";
            driverMode.index = -1;
        }
        return driverMode;
    }

    private List<CameraCalibrationConfig> loadCalibration() {
        List<CameraCalibrationConfig> calibrations = new ArrayList<>();
        try {
            calibrations = List.of(Objects.requireNonNull(JacksonHelper.deserializer(calibrationPath, CameraCalibrationConfig[].class)));
        } catch (IOException e) {
            System.err.println("Failed to load camera calibration: " + driverModePath.toString());
        }
        return calibrations;
    }

    void saveConfig(CameraJsonConfig config) {
        try {
            JacksonHelper.serializer(configPath, config);
        } catch (IOException e) {
            System.err.println("Failed to save camera config file: " + configPath.toString());
        }
    }

    void savePipelines(List<CVPipelineSettings> pipelines) {
        pipelineConfig.save(pipelines);
    }

    public void saveDriverMode(CVPipelineSettings driverMode) {
        try {
            JacksonHelper.serializer(driverModePath, driverMode);
        } catch (IOException e) {
            System.err.println("Failed to save camera drivermode file: " + driverModePath.toString());
        }
    }

    void checkFolder() {
        if (!configFolderExists()) {
            try {
                if (!(new File(configFolderPath.toUri()).mkdirs())) {
                    System.err.println("Failed to create camera config folder: " + configFolderPath.toString());
                }
            } catch(Exception e) {
                System.err.println("Failed to create camera config folder: " + configFolderPath.toString());
            }
        }
    }

    private void checkConfig() {
        if (!configExists()) {
            try {
                JacksonHelper.serializer(configPath, preliminaryConfig);
            } catch (IOException e) {
                System.err.println("Failed to create camera config file: " + configPath.toString());
            }
        }
    }

    private void checkDriverMode() {
        if (!driverModeExists()) {
            try {
                CVPipelineSettings newDriverModeSettings = new CVPipelineSettings();
                newDriverModeSettings.nickname = "DRIVERMODE";
                JacksonHelper.serializer(driverModePath, newDriverModeSettings);
            } catch (IOException e) {
                System.err.println("Failed to create camera drivermode file: " + driverModePath.toString());
            }
        }
    }

    private void checkCalibration() {
        if (!calibrationExists()) {
            try {
                List<CameraCalibrationConfig> calibrations = new ArrayList<>();
                JacksonHelper.serializer(calibrationPath, calibrations.toArray());
            } catch (IOException e) {
                System.err.println("Failed to create camera calibration file: " + calibrationPath.toString());
            }
        }
    }

    private boolean configFolderExists() {
        return Files.exists(configFolderPath);
    }

    Path getPipelineFolderPath() {
        return Paths.get(configFolderPath.toString(), "pipelines");
    }

    private boolean configExists() {
        return configFolderExists() && Files.exists(configPath);
    }

    private boolean driverModeExists() {
        return configFolderExists() && Files.exists(driverModePath);
    }

    private boolean calibrationExists() {
        return configFolderExists() && Files.exists(calibrationPath);
    }
}
