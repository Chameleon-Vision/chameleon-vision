package com.chameleonvision.common.hardware.metrics;

import com.chameleonvision.common.hardware.RunCommand;

public class RAM {
    private static final String utilizationCommand = "free -h";

    public static double getUtilization() {
        return RunCommand.execute(utilizationCommand);
    }
}
