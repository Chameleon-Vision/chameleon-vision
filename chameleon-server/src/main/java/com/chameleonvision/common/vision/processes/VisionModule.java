package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.dataflow.consumer.UIConsumer;
import com.chameleonvision.common.dataflow.providers.Provider;
import com.chameleonvision.common.dataflow.providers.UIProvider;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameConsumer;
import com.chameleonvision.common.vision.frame.consumer.MJPGFrameConsumer;
import com.chameleonvision.common.vision.pipeline.CVPipelineResult;
import io.reactivex.rxjava3.core.Observer;
import java.util.ArrayList;
import java.util.List;

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
    private final List<Observer<CVPipelineResult>> dataConsumers = new ArrayList<>();
    private final List<Provider> dataProviders = new ArrayList<>();
    private final List<FrameConsumer> frameConsumers = new ArrayList<>();

    public VisionModule(PipelineManager pipelineManager, VisionSource visionSource) {
        this.pipelineManager = pipelineManager;
        this.visionSource = visionSource;
        this.visionRunner =
                new VisionRunner(
                        this.visionSource.getFrameProvider(),
                        this.pipelineManager::getCurrentPipeline,
                        this::consumeResult);
        initMjpgStreamer();
        UIProvider ui = new UIProvider(this);
        dataProviders.add(ui);
        UIConsumer uiConsumer = new UIConsumer();
        dataConsumers.add(uiConsumer);
    }

    private void initMjpgStreamer() {
        String name = visionSource.getCameraConfiguration().nickname;
        int width = visionSource.getSettables().getCurrentVideoMode().width;
        int height = visionSource.getSettables().getCurrentVideoMode().height;
        MJPGFrameConsumer mjpgFrameConsumer = new MJPGFrameConsumer(name, width, height);
        addFrameConsumer(mjpgFrameConsumer);
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

    public void addDataConsumer(Observer<CVPipelineResult> dataConsumer) {
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

    public String getSourceNickname() {
        return visionSource.getCameraConfiguration().nickname;
    }

    public List<Observer<CVPipelineResult>> getDataConsumers() {
        return dataConsumers;
    }

    public UIProvider getUIDataProvider() {
        return (UIProvider)
                dataProviders.stream().filter(c -> c instanceof UIProvider).findFirst().orElse(null);
    }

    public VisionSource getVisionSource() {
        return visionSource;
    }
}
