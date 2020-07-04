package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameProvider;
import com.chameleonvision.common.vision.pipeline.CVPipeline;
import com.chameleonvision.common.vision.pipeline.CVPipelineResult;
import edu.wpi.first.wpiutil.CircularBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** VisionRunner has a frame supplier, a pipeline supplier, and a result consumer */
@SuppressWarnings("rawtypes")
public class VisionRunner {

    private final Thread visionProcessThread;
    private final Supplier<Frame> frameSupplier;
    private final Supplier<CVPipeline> pipelineSupplier;
    private final Consumer<CVPipelineResult> pipelineResultConsumer;
    private static final Logger logger = new Logger(VisionRunner.class, LogGroup.VisionProcess);
    private long loopCount;
    volatile Double fps = 0.0;
    private CircularBuffer fpsAveragingBuffer = new CircularBuffer(7);

    /**
    * VisionRunner contains a <see cref="Thread">Thread</see> to run a pipeline, given a frame, and
    * will give the result to the consumer.
    *
    * @param frameSupplier The supplier of the latest frame.
    * @param pipelineSupplier The supplier of the current pipeline.
    * @param pipelineResultConsumer The consumer of the latest result.
    */
    public VisionRunner(
            FrameProvider frameSupplier,
            Supplier<CVPipeline> pipelineSupplier,
            Consumer<CVPipelineResult> pipelineResultConsumer) {
        this.frameSupplier = frameSupplier;
        this.pipelineSupplier = pipelineSupplier;
        this.pipelineResultConsumer = pipelineResultConsumer;

        this.visionProcessThread = new Thread(this::update);
        this.visionProcessThread.setName("VisionRunner - " + frameSupplier.getName());
    }

    public void startProcess() {
        visionProcessThread.start();
    }

    private boolean hasThrown;

    private void update() {
        while (!Thread.interrupted()) {
            var lastUpdateTimeNanos = System.nanoTime();
            loopCount++;
            var pipeline = pipelineSupplier.get();
            var frame = frameSupplier.get();
            if (frame.image.getMat().cols() > 0 && frame.image.getMat().rows() > 0) {
                try {
                    var pipelineResult = pipeline.run(frame);
                    pipelineResult.fps = fps;
                    pipelineResultConsumer.accept(pipelineResult);
                    pipelineResult.release();
                } catch (Exception ex) {
                    if (hasThrown) {
                        logger.error(
                                "Exception in thread \"" + visionProcessThread.getName() + "\", loop " + loopCount);
                        ex.printStackTrace();
                        hasThrown = true;
                    }
                }
            }
            var deltaTimeNanos = System.nanoTime() - lastUpdateTimeNanos;
            fpsAveragingBuffer.addFirst(1.0 / (deltaTimeNanos * 1E-09));
            fps = getAverageFPS();
        }
    }

    double getAverageFPS() {
        var temp = 0.0;
        for (int i = 0; i < 7; i++) {
            temp += fpsAveragingBuffer.get(i);
        }
        temp /= 7.0;
        return temp;
    }
}
