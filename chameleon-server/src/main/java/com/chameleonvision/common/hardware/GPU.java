package com.chameleonvision.common.hardware;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

public enum GPU {
    MEMORY("vcgencmd get_mem gpu"),
    TEMPERATURE("vcgencmd measure_temp"),
    UTILIZATION("top -b -n 1 | sed -n \"s/^%Gpu.*ni, \\([0-9.]*\\) .*$/\\1% Idle/p\"");

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

    GPU(String command) {
        this.command = command;
    }
}
