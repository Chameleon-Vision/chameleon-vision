package com.chameleonvision.classabstraction.util;

/**
 * A thread that tries to run at a specified loop time
 */
public abstract class LoopingRunnable implements Runnable {
    private final Long loopTimeMs;

    protected abstract void process();

    public LoopingRunnable(Long loopTimeMs) {
        this.loopTimeMs = loopTimeMs;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            var now = System.currentTimeMillis();

            // Do the thing
            process();

            // sleep for the remaining time
            var timeElapsed = System.currentTimeMillis() - now;
            var delta = loopTimeMs - timeElapsed;
            if(delta > 0.0) {
                try {
                    Thread.sleep(delta, 0);
                } catch (Exception ignored) {}
            }
        }
    }
}