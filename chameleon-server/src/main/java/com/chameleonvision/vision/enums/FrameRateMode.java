package com.chameleonvision.vision.enums;

public enum FrameRateMode {
    HIGH(30),
    MEDIUM(20),
    LOW(10);

    public final int frameRate;
    FrameRateMode(int frameRate){this.frameRate=frameRate;}
}
