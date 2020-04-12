package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameProvider;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
* VisionModule has a pipeline manager, vision process and data providers. The data providers
* provide info on setting changes. VisionModuleManager holds a list of all current vision modules.
*/
public class VisionModule {

    private final PipelineManager pipelineManager;
    private final VisionProcessor visionProcess;
    LinkedList<Consumer<Data>> dataConsumers;
    LinkedList<Consumer<Frame>> frameConsumers;

    public VisionModule(PipelineManager pipelineManager, FrameProvider frameProvider) {
        this.pipelineManager = pipelineManager;
        this.visionProcess =
                new VisionProcessor(
                        frameProvider::getFrame, pipelineManager::getCurrentPipeline,
                        this::consumeData, this::consumeFrame);
    }

    void consumeData(Data data) {
        for (var dataConsumer : dataConsumers) {
            dataConsumer.accept(data);
        }
    }

    void consumeFrame(Frame frame) {
        for (var frameConsumer : frameConsumers) {
            frameConsumer.accept(frame);
        }
    }
}
