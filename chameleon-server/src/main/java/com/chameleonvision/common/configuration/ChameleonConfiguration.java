package com.chameleonvision.common.configuration;

import java.util.HashMap;

public class ChameleonConfiguration {
    public HardwareConfig getHardwareConfig() {
        return hardwareConfig;
    }

    public NetworkConfig getNetworkConfig() {
        return networkConfig;
    }

    public HashMap<String, CameraConfiguration> getCameraConfigurations() {
        return cameraConfigurations;
    }

    public void addCameraConfig(CameraConfiguration config) {
        addCameraConfig(config.uniqueName, config);
    }

    public void addCameraConfig(String name, CameraConfiguration config) {
        cameraConfigurations.put(name, config);
    }

    private HardwareConfig hardwareConfig;
    private NetworkConfig networkConfig;

    private HashMap<String, CameraConfiguration> cameraConfigurations;

    public ChameleonConfiguration(HardwareConfig hardwareConfig, NetworkConfig networkConfig) {
        this(hardwareConfig, networkConfig, new HashMap<>());
    }

    public ChameleonConfiguration(
            HardwareConfig hardwareConfig,
            NetworkConfig networkConfig,
            HashMap<String, CameraConfiguration> cameraConfigurations) {
        this.hardwareConfig = hardwareConfig;
        this.networkConfig = networkConfig;
        this.cameraConfigurations = cameraConfigurations;
    }
}
