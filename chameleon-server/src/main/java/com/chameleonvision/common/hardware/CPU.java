package com.chameleonvision.common.hardware;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

public enum CPU {
    MEMORY("vcgencmd get_mem arm"),
    TEMPERATURE("cat /sys/class/thermal/thermal_zone0/temp"),
    UTILIZATION("top -b -n 1 | sed -n \"s/^%Cpu.*ni, \\([0-9.]*\\) .*$/\\1% Idle/p\"");

    public final String command;

    double value() {
        if (!Platform.isRaspberryPi()) return Double.NaN;
        try {
            return Double.parseDouble(
                    IOUtils.toString(
                                    new ProcessBuilder("bash", "-c", command).start().getInputStream(),
                                    StandardCharsets.UTF_8)
                            .replaceAll("[^\\d.]", ""));
        } catch (IOException e) {
            e.printStackTrace();
            return Double.NaN;
        }
    }

    CPU(String command) {
        this.command = command;
    }
}
