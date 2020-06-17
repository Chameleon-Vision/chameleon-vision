package com.chameleonvision.common.hardware.GPIO;

import com.chameleonvision.common.hardware.RunCommand;

public class CustomGPIO extends GPIOBase{

    private boolean currentState;

    @Override
    public void togglePin() {
        RunCommand.execute(commands.get("toggle"));
        currentState = !currentState;
    }

    @Override
    public void setLow() {
        RunCommand.execute(commands.get("setLow"));
        currentState = false;
    }

    @Override
    public void setHigh() {
        RunCommand.execute(commands.get("setHigh"));
        currentState = true;
    }

    @Override
    public void setState(boolean state) {
        RunCommand.execute(commands.get("setState").replace("{s}", String.valueOf(state)));
        currentState = state;
    }

    @Override
    public void blink(long delay, long duration) {
        RunCommand.execute(commands.get("setState").replace("{delay}", String.valueOf(delay)).replace("{duration}",  String.valueOf(duration)));
    }

    @Override
    public void pulse(long duration, boolean blocking) {
        RunCommand.execute(commands.get("pulse").replace("{blocking}", String.valueOf(blocking)).replace("{duration}",  String.valueOf(duration)));
    }

    @Override
    public  boolean shutdown() {
        RunCommand.execute(commands.get("shutdown"));
        return true;
    }

    @Override
    public boolean getState(){
        return currentState;
    }



}
