
package com.chameleonvision.common.hardware;

import com.chameleonvision.common.hardware.GPIO.CustomGPIO;
import com.chameleonvision.common.hardware.GPIO.GPIOBase;
import com.chameleonvision.common.hardware.GPIO.PiGPIO;
import com.chameleonvision.common.hardware.metrics.CPU;
import com.chameleonvision.common.hardware.metrics.GPU;
import com.chameleonvision.common.hardware.metrics.RAM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HardwareTest {

    @Test
    public  void testHardware() {

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
    public  void testGPIO(){
        GPIOBase gpio;
        if(Platform.isRaspberryPi()){gpio = new PiGPIO(0);}
        else{
            gpio = new CustomGPIO();
            gpio.setToggleCommand("gpio toggle");
            gpio.setLowCommand("gpio setLow");
            gpio.setHighCommand("gpio setHigh");
            gpio.setStateCommand("gpio setState {s}");
            gpio.setBlinkCommand("gpio blink {delay} {duration}");
            gpio.setPulseCommand("gpio pulse {blocking} {duration}");
            gpio.setShutdownCommand("gpio shutdown");
        }

        gpio.setHigh();
        assertTrue(gpio.getState());

        gpio.setLow();
        assertTrue(gpio.getState());

    }


}