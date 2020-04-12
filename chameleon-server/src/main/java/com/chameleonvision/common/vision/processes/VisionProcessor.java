package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.pipeline.CVPipeline;
import com.chameleonvision.common.vision.pipeline.CVPipelineSettings;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** VisionProcess has a frame provider, many data consumers, and many frame consumers. */
public class VisionProcessor {

    private final Thread visionProcessThread;
    private final Supplier<Frame> frameSupplier;
    private final Supplier<CVPipeline> pipelineSupplier;
    private final Consumer<Data> dataConsumer;
    private final Consumer<Frame> frameConsumer;

    public VisionProcessor(
            Supplier<Frame> frameSupplier,
            Supplier<CVPipeline> pipelineSupplier,
            Consumer<Data> dataConsumer,
            Consumer<Frame> frameConsumer) {
        this.frameSupplier = frameSupplier;
        this.pipelineSupplier = pipelineSupplier;
        this.dataConsumer = dataConsumer;
        this.frameConsumer = frameConsumer;

        this.visionProcessThread = new Thread(this::update);
    }

    public void startProcess() {
        visionProcessThread.start();
    }

    private void update() {
        while (!Thread.interrupted()) {
            var pipeline = pipelineSupplier.get();
            run(pipeline, null);
        }
    }

    public <S extends CVPipelineSettings> void run(CVPipeline<?, S> pipeline, S settings) {
        var frame = frameSupplier.get();
        var result = pipeline.run(frame, settings);
        var data = new Data(); // TODO
        dataConsumer.accept(data);
        frameConsumer.accept(result.outputFrame);
    }
}
