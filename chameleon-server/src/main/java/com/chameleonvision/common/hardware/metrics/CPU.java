package com.chameleonvision.common.hardware.metrics;


public class CPU extends MetricsBase{
    private static final String memoryCommand = "vcgencmd get_mem arm";
    private static final String temperatureCommand = "cat /sys/class/thermal/thermal_zone0/temp";
    private static final String utilizationCommand = "top -b -n 1 | sed -n \"s/^%Cpu\"";

    public static double getMemory() { return execute(memoryCommand);
    }

    public static double getTemp() {
        return execute(temperatureCommand);
    }

    public static double getUtilization() {
        return execute(utilizationCommand);
    }
}
