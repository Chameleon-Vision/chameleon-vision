package com.chameleonvision.common.hardware;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

public class RunCommand {
    public static double execute(String command) {
        try {
            return Double.parseDouble(
                    IOUtils.toString(
                                    new ProcessBuilder("bash", "-c", command).start().getInputStream(),
                                    StandardCharsets.UTF_8)
                            .replaceAll("[^\\d.]", ""));
        } catch (IOException | NumberFormatException e) {
            return Double.NaN;
        }
    }
}
