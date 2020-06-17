package com.chameleonvision.common.hardware.metrics;
import com.chameleonvision.common.hardware.RunCommand;
public class  GPU {
    private static final String memoryCommand  = "vcgencmd get_mem gpu";
    private static final String temperatureCommand=  "vcgencmd measure_temp";
    private static final String utilizationCommand = "top -b -n 1 | sed -n \"s/^%Gpu\"";

    public static double getMemory(){
        return RunCommand.execute(memoryCommand);
    }

    public static double getTemp(){
        return RunCommand.execute(temperatureCommand);
    }

    public static double getUtilization(){
        return RunCommand.execute(utilizationCommand);
    }


}
