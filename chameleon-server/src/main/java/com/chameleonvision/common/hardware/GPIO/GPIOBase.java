package com.chameleonvision.common.hardware.GPIO;

import java.util.HashMap;

public abstract class GPIOBase {
    HashMap<String, String> commands = new HashMap<>(){{
        put("toggle", "");
        put("setLow", "");
        put("setHigh", "");
        put("setState", "");
        put("blink", "");
        put("pulse", "");
        put("shutdown", "");
    }};


    public void setToggleCommand(String command){
        commands.replace("toggle", command);
    }

    public void setLowCommand(String command){
        commands.replace("setLow", command);
    }

    public void setHighCommand(String command){
        commands.replace("setHigh", command);
    }

    public void setStateCommand(String command){
        commands.replace("setState", command);
    }

    public void setBlinkCommand(String command){
        commands.replace("blink", command);
    }

    public void setPulseCommand(String command){
        commands.replace("pulse", command);
    }

    public void setShutdownCommand(String command){
        commands.replace("shutdown", command);
    }

    public abstract void togglePin();

    public abstract void setLow();

    public abstract void setHigh();

    public abstract void setState(boolean state);

    public abstract void blink(long delay, long duration);

    public abstract void pulse(long duration, boolean blocking);

    public abstract boolean shutdown();

    public abstract boolean getState();


}
