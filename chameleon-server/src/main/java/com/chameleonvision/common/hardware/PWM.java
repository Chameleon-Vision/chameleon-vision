package com.chameleonvision.common.hardware;

import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;

public class PWM {
    private static final GpioController gpio = GpioFactory.getInstance();
    private final GpioPinPwmOutput pwm;

    public PWM(int address) {
        this.pwm =
                gpio.provisionPwmOutputPin(
                        CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.getPinByAddress(address)));
    }

    public PWM(String name) {
        this.pwm =
                gpio.provisionPwmOutputPin(
                        CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.getPinByName(name)));
    }

    public void setPwmRate(int rate) {
        pwm.setPwm(rate);
    }

    public void setPwmRange(int range) {
        pwm.setPwmRange(range);
    }

    public int getPwmRate() {
        return pwm.getPwm();
    }

    public static boolean shutdown() {
        gpio.shutdown();
        return gpio.isShutdown();
    }
}
