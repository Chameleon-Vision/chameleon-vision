package com.chameleonvision.common.hardware.metrics;

import com.chameleonvision.common.hardware.RunCommand;

public class CPU {
    private static final String memoryCommand = "vcgencmd get_mem arm";
    private static final String temperatureCommand = "cat /sys/class/thermal/thermal_zone0/temp";
    private static final String utilizationCommand = "top -b -n 1 | sed -n \"s/^%Cpu\"";

    public static double getMemory() {
        return RunCommand.execute(memoryCommand);
    }

    public static double getTemp() {
        return RunCommand.execute(temperatureCommand);
    }

    public static double getUtilization() {
        return RunCommand.execute(utilizationCommand);
    }
}
