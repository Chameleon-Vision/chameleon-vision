package com.chameleonvision.common.hardware.metrics;


public class CPU extends MetricsBase{
    private static final String memoryCommand = "sudo vcgencmd get_mem arm | grep -Eo '[0-9]+'";
    private static final String temperatureCommand = "sudo cat /sys/class/thermal/thermal_zone0/temp | grep -x -E '[0-9]+'";
    private static final String utilizationCommand = "sudo cat /proc/stat | grep 'cpu ' /proc/stat | awk '{usage=($2+$4)*100/($2+$4+$5)} END {print usage \"\"}'";

    public static double getMemory() { return execute(memoryCommand);
    }

    public static double getTemp() {
        return execute(temperatureCommand) / 1000;
    }

    public static double getUtilization() {
        return execute(utilizationCommand);
    }
}
