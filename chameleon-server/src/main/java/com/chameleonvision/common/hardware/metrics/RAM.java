package com.chameleonvision.common.hardware.metrics;


public class RAM extends MetricsBase{
    private static final String utilizationCommand = "sudo vmstat -s  | awk -v i=2 -v j=1 'FNR == i {print $j}'";

    public static double getUtilization() {
        return execute(utilizationCommand) / 1000;
    }
}
