package com.chameleonvision.common.hardware.PWM;

import com.chameleonvision.common.hardware.RunCommand;

public class CustomPWM extends PWMBase {
    private int pwmRate = 0;
    private int pwmRange = 0;

    @Override
    public void setPwmRate(int rate) {
        RunCommand.execute(commands.get("setRate").replace("{rate}", String.valueOf(rate)));
        pwmRate = rate;
    }

    @Override
    public void setPwmRange(int range) {
        RunCommand.execute(commands.get("setRange").replace("{rate}", String.valueOf(range)));
        pwmRange = range;
    }

    @Override
    public int getPwmRate() {
        return pwmRate;
    }

    @Override
    public int getPwmRange() {
        return pwmRange;
    }

    @Override
    public boolean shutdown() {
        RunCommand.execute(commands.get("shutdown"));
        return true;
    }
}
