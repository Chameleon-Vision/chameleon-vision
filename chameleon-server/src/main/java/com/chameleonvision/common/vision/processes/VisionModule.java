package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.dataflow.consumer.UIConsumer;
import com.chameleonvision.common.dataflow.providers.Provider;
import com.chameleonvision.common.dataflow.providers.UIProvider;
import com.chameleonvision.common.util.VideoModeHelper;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameConsumer;
import com.chameleonvision.common.vision.frame.consumer.MJPGFrameConsumer;
import com.chameleonvision.common.vision.pipeline.CVPipelineResult;
import edu.wpi.cscore.VideoMode;
import io.reactivex.rxjava3.core.Observer;
import java.util.ArrayList;
import java.util.HashMap;
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
    private FrameConsumer frameConsumer;

    public VisionModule(PipelineManager pipelineManager, VisionSource visionSource) {
        this.pipelineManager = pipelineManager;
        this.visionSource = visionSource;
        this.visionRunner =
                new VisionRunner(
                        this.visionSource.getFrameProvider(),
                        this.pipelineManager::getCurrentPipeline,
                        this::consumeResult);
        UIProvider ui = new UIProvider(this);
        dataProviders.add(ui);
        UIConsumer uiConsumer = new UIConsumer();
        dataConsumers.add(uiConsumer);
        initMjpgStreamer();
    }

    public void initMjpgStreamer() {
        String name = visionSource.getCameraConfiguration().nickname;
        int width = visionSource.getSettables().getCurrentVideoMode().width;
        int height = visionSource.getSettables().getCurrentVideoMode().height;
        MJPGFrameConsumer mjpgFrameConsumer = new MJPGFrameConsumer(name, width, height);
        setFrameConsumer(mjpgFrameConsumer);
    }

    public void start() {
        visionRunner.startProcess();
    }

    void consumeResult(CVPipelineResult result) {
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

    public void setFrameConsumer(FrameConsumer frameConsumer) {
        this.frameConsumer = frameConsumer;
    }

    public FrameConsumer getFrameConsumer() {
        return frameConsumer;
    }

    void consumeFrame(Frame frame) {
        frameConsumer.accept(frame);
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

    public HashMap<String, Object> getOrdinalCamera() {
        HashMap<String, Object> tmp = new HashMap<>();
        tmp.put("fov", getVisionSource().getCameraConfiguration().FOV);
        //        tmp.put("tilt", getVisionSource().getCameraConfiguration().);
        tmp.put("calibration", getVisionSource().getCameraConfiguration().calibration);
        tmp.put("port", getFrameConsumer().getPort());
        tmp.put(
                "resolutionList",
                VideoModeHelper.videoModeToHashMapList(
                        new ArrayList<>(getVisionSource().getSettables().getAllVideoModes().values())));
        return tmp;
    }

    public void setStreamResolution() {
        int divisor = pipelineManager.getCurrentPipeline().getSettings().outputFrameDivisor.value;
        VideoMode mode = visionSource.getSettables().getCurrentVideoMode();
        int newWidth = mode.width / divisor;
        int newHeight = mode.height / divisor;
        frameConsumer.setResolution(newWidth, newHeight);
    }
}
