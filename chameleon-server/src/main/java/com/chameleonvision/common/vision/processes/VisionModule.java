package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameConsumer;
import com.chameleonvision.common.vision.pipeline.CVPipelineResult;
import io.reactivex.rxjava3.core.Observer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * This is the God Class
 *
 * <p>VisionModule has a pipeline manager, vision runner, and data providers. The data providers
 * provide info on settings changes. VisionModuleManager holds a list of all current vision modules.
 */


public class VisionModule {
    public final PipelineManager pipelineManager;
    private final VisionSource visionSource;
    private final VisionRunner visionRunner;
    private final LinkedList<Observer<CVPipelineResult>> dataConsumers = new LinkedList<>();
    private final LinkedList<FrameConsumer> frameConsumers = new LinkedList<>();

    public VisionModule(PipelineManager pipelineManager, VisionSource visionSource) {
        this.pipelineManager = pipelineManager;
        this.visionSource = visionSource;
        this.visionRunner =
                new VisionRunner(
                        this.visionSource.getFrameProvider(),
                        this.pipelineManager::getCurrentPipeline,
                        this::consumeResult);
    }

    public void start() {
        visionRunner.startProcess();
    }

    void consumeResult(CVPipelineResult result) {
        // TODO: put result in to Data (not this way!)
        consumeData(result);

        var frame = result.outputFrame;
        consumeFrame(frame);
    }

    void consumeData(CVPipelineResult data) {
        for (var dataConsumer : dataConsumers) {
            dataConsumer.onNext(data);
        }
    }

    public void addDataConsumer(Observer dataConsumer) {
        dataConsumers.add(dataConsumer);
    }

    public void addFrameConsumer(FrameConsumer frameConsumer) {
        frameConsumers.add(frameConsumer);
    }

    void consumeFrame(Frame frame) {
        for (var frameConsumer : frameConsumers) {
            frameConsumer.accept(frame);
        }
    }
    public String getSourceNickname(){
        return visionSource.getCameraConfiguration().nickname;
    }
}
