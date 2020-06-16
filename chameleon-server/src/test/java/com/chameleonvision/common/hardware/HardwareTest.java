package com.chameleonvision.common.hardware;

public class HardwareTest {
    public static void main(String[] args) {
        testHardware();
    };

    public static void testHardware() {

        System.out.println("Printing CPU Info:");
        System.out.println("Memory: " + CPU.MEMORY.value());
        System.out.println("Temperature: " + CPU.TEMPERATURE.value());
        System.out.println("Utilization: : " + CPU.UTILIZATION.value());

        System.out.println("Printing GPU Info:");
        System.out.println("Memory: " + GPU.MEMORY.value());
        System.out.println("Temperature: " + GPU.TEMPERATURE.value());
        System.out.println("Utilization: : " + GPU.UTILIZATION.value());

        System.out.println("Printing RAM Info: ");
        System.out.println("Utilization: : " + RAM.UTILIZATION.value());
    }
}
