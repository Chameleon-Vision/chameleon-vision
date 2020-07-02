package com.chameleonvision.common.vision.frame;

import java.util.function.Consumer;

public interface FrameConsumer extends Consumer<Frame> {
    public int getPort();

    public void setResolution(int width, int height);
}
