package com.chameleonvision;

import com.chameleonvision.common.NetworkTables.NTManager;
import com.chameleonvision.common.configuration.CameraConfiguration;
import com.chameleonvision.common.configuration.ConfigManager;
import com.chameleonvision.common.networking.NetworkManager;
import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.vision.processes.VisionModuleManager;
import com.chameleonvision.common.vision.processes.VisionSource;
import com.chameleonvision.common.vision.processes.VisionSourceManager;
import com.chameleonvision.server.Server;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TestUtils.loadLibraries();
        ConfigManager configManager = ConfigManager.getInstance();

        NetworkManager networkManager = NetworkManager.getInstance();
        NTManager.setTeamClientMode();
        VisionSourceManager visionSourceManager = new VisionSourceManager();

        HashMap<String, CameraConfiguration> camConfigs =
                ConfigManager.getInstance().getConfig().getCameraConfigurations();
        List<CameraConfiguration> matchedCameras =
                visionSourceManager.LoadAllSources(new ArrayList<CameraConfiguration>(camConfigs.values()));

        List<VisionSource> sources = visionSourceManager.loadUSBCameraSources(matchedCameras);
        VisionModuleManager moduleManager = new VisionModuleManager(sources);
        moduleManager.startModules();
        Server.main(80);
    }
}
