package com.chameleonvision.common.NetworkTables;

import com.chameleonvision.common.configuration.ConfigManager;
import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class NTManager {

    private static final Logger logger = new Logger(NTManager.class, LogGroup.General);

    private static final NetworkTableInstance ntInstance = NetworkTableInstance.getDefault();

    public static final String TableName = "/chameleon-vision/";
    public static final NetworkTable kRootTable =
            NetworkTableInstance.getDefault().getTable(TableName);

    public static boolean isServer = false;

    private static int getTeamNumber() {
        return ConfigManager.getInstance().getConfig().getNetworkConfig().teamNumber;

    }

    public static void setClientMode(String host) {
        isServer = false;
        logger.info("Starting NT Client");
        ntInstance.stopServer();
        if (host != null) {
            ntInstance.startClient(host);
        } else {
            ntInstance.startClientTeam(getTeamNumber());
            if (ntInstance.isConnected()) {
                logger.info("[NetworkTablesManager] Connected to the robot!");
            } else {
                logger.info(
                        "[NetworkTablesManager] Could NOT to the robot! Will retry in the background...");
            }
        }
    }

    public static void setTeamClientMode() {
        setClientMode(null);
    }

    public static void setServerMode() {
        isServer = true;
        logger.info("Starting NT Server");
        ntInstance.stopClient();
        ntInstance.startServer();
    }
}

