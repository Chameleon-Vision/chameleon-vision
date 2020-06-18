package com.chameleonvision.common.hardware.metrics;


public class GPU extends MetricsBase{
    private static final String memoryCommand = "vcgencmd get_mem gpu";
    private static final String temperatureCommand = "vcgencmd measure_temp";
    private static final String utilizationCommand = "top -b -n 1 | sed -n \"s/^%Gpu\"";

    public static double getMemory() {
        return execute(memoryCommand);
    }

    public static double getTemp() {
        return execute(temperatureCommand);
    }

    public static double getUtilization() {
        return execute(utilizationCommand);
    }
}
