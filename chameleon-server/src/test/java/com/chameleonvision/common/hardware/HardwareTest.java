package com.chameleonvision.common.hardware;

import static org.junit.jupiter.api.Assertions.*;

import com.chameleonvision.common.hardware.GPIO.CustomGPIO;
import com.chameleonvision.common.hardware.GPIO.GPIOBase;
import com.chameleonvision.common.hardware.GPIO.PiGPIO;
import com.chameleonvision.common.hardware.PWM.CustomPWM;
import com.chameleonvision.common.hardware.PWM.PWMBase;
import com.chameleonvision.common.hardware.PWM.PiPWM;
import com.chameleonvision.common.hardware.metrics.CPU;
import com.chameleonvision.common.hardware.metrics.GPU;
import com.chameleonvision.common.hardware.metrics.RAM;
import org.junit.jupiter.api.Test;

public class HardwareTest {

    @Test
    public void testHardware() {

        System.out.println("Printing CPU Info:");
        System.out.println("Memory: " + CPU.getMemory());
        System.out.println("Temperature: " + CPU.getTemp());
        System.out.println("Utilization: : " + CPU.getUtilization());

        System.out.println("Printing GPU Info:");
        System.out.println("Memory: " + GPU.getMemory());
        System.out.println("Temperature: " + GPU.getTemp());
        System.out.println("Utilization: : " + GPU.getUtilization());

        System.out.println("Printing RAM Info: ");
        System.out.println("Utilization: : " + RAM.getUtilization());
    }

    @Test
    public void testGPIO() {
        GPIOBase gpio;
        if (Platform.isRaspberryPi()) {
            gpio = new PiGPIO(0);
        } else {
            gpio = new CustomGPIO();
            gpio.setToggleCommand("gpio toggle");
            gpio.setLowCommand("gpio setLow");
            gpio.setHighCommand("gpio setHigh");
            gpio.setStateCommand("gpio setState {s}");
            gpio.setBlinkCommand("gpio blink {delay} {duration}");
            gpio.setPulseCommand("gpio pulse {blocking} {duration}");
            gpio.setShutdownCommand("gpio shutdown");
        }

        gpio.setHigh(); // HIGH
        assertTrue(gpio.getState());

        gpio.setLow(); // LOW
        assertFalse(gpio.getState());

        gpio.togglePin(); // HIGH
        assertTrue(gpio.getState());

        gpio.togglePin(); // LOW
        assertFalse(gpio.getState());

        gpio.setState(true); // HIGH
        assertTrue(gpio.getState());

        gpio.setState(false); // LOW
        assertFalse(gpio.getState());

        var success = gpio.shutdown();
        assertTrue(success);

        gpio.pulse(10, false);
        gpio.blink(2, 10);
    }

    @Test
    public void testPWM() {
        PWMBase pwm;
        if (Platform.isRaspberryPi()) {
            pwm = new PiPWM(0);
        } else {
            pwm = new CustomPWM();
            pwm.setPwmRateCommand("pwm setRate {rate}");
            pwm.setPwmRangeCommand("pwm setRange {range}");
        }
        pwm.setPwmRange(100);
        assertEquals(pwm.getPwmRange(), 100);

        pwm.setPwmRate(10);
        assertEquals(pwm.getPwmRate(), 10);

        var success = pwm.shutdown();
        assertTrue(success);
    }
}
