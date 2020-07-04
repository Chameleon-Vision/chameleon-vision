package com.chameleonvision.common.dataflow.consumer;

import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.common.vision.pipeline.CVPipelineResult;
import com.chameleonvision.common.vision.processes.VisionModuleManager;
import com.chameleonvision.common.vision.target.TrackedTarget;
import com.chameleonvision.server.SocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class UIConsumer implements Observer<CVPipelineResult> {
    private final Logger logger = new Logger(UIConsumer.class, LogGroup.VisionProcess);
    long lastUIUpdateMs;

    @Override
    public void onSubscribe(@NonNull Disposable d) {}

    @Override
    public void onNext(@NotNull CVPipelineResult result) {
        long currentMillis = System.currentTimeMillis();
        if (currentMillis - lastUIUpdateMs > 1000 / 30) {
            lastUIUpdateMs = currentMillis;
            if (VisionModuleManager.getUIvisionModule().getDataConsumers().contains(this)) {
                HashMap<String, Object> WebSend = new HashMap<>();
                HashMap<String, Object> point = new HashMap<>();
                HashMap<String, Object> pointMap;
                ArrayList<Object> webTargets = new ArrayList<>();
                List<Double> center = new ArrayList<>();

                if (result.hasTargets) {
                    for (TrackedTarget target : result.targets) {
                        pointMap = new HashMap<>();
                        pointMap.put("pitch", target.getPitch());
                        pointMap.put("yaw", target.getYaw());
                        pointMap.put("area", target.getArea());
                        pointMap.put("pose", target.getRobotRelativePose());
                        webTargets.add(pointMap);
                    }
                    center.add(result.targets.get(0).getMinAreaRect().center.x);
                    center.add(result.targets.get(0).getMinAreaRect().center.y);

                } else {
                    center.add(null);
                    center.add(null);
                }
                point.put("fps", result.fps);
                point.put("targets", webTargets);
                point.put("rawPoint", center);
                WebSend.put("point", point);
                try {
                    SocketHandler.broadcastMessage(WebSend);
                } catch (JsonProcessingException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {
        logger.error(e.getMessage());
    }

    @Override
    public void onComplete() {}
}
