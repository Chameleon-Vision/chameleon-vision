package com.chameleonvision.common.hardware;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

public class GPIO {
    private static final GpioController gpio = GpioFactory.getInstance();
    private final GpioPinDigitalOutput pin;

    public GPIO(int address) {
        this.pin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(address));
    }

    public GPIO(String name) {
        this.pin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(name));
    }

    public void togglePin() {
        pin.toggle();
    }

    public void setLow() {
        pin.low();
    }

    public void setHigh() {
        pin.high();
    }

    public void setState(boolean state) {
        pin.setState(state);
    }

    public void blink(long delay, long duration) {
        pin.blink(delay, duration);
    }

    public void pulse(long duration, boolean blocking) {
        pin.pulse(duration, blocking);
    }

    public static boolean shutdown() {
        gpio.shutdown();
        return gpio.isShutdown();
    }
}
