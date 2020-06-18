package com.chameleonvision.common.hardware.metrics;


public class RAM extends MetricsBase{
    private static final String utilizationCommand = "free -h";

    public static double getUtilization() {
        return execute(utilizationCommand);
    }
}
