package com.chameleonvision.common.hardware;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

public enum RAM {
    UTILIZATION("free -h");

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

    RAM(String command) {
        this.command = command;
    }
}
