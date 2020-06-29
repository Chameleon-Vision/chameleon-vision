package com.chameleonvision.common.dataflow.consumer;

import com.chameleonvision.common.NetworkTables.NTManager;
import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.common.vision.pipeline.CVPipelineResult;
import com.chameleonvision.common.vision.target.TrackedTarget;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.List;
import java.util.stream.Collectors;

public class NTConsumer implements Observer<CVPipelineResult> {
    private final Logger logger = new Logger(UIConsumer.class, LogGroup.VisionProcess);
    private final NetworkTableEntry ntYawEntry;
    private final NetworkTableEntry ntPitchEntry;
    private final NetworkTableEntry ntAuxListEntry;
    private final NetworkTableEntry ntAreaEntry;
    private final NetworkTableEntry ntLatencyEntry;
    private final NetworkTableEntry ntValidEntry;
    private final NetworkTableEntry ntPoseEntry;
    private final NetworkTableEntry ntFittedHeightEntry;
    private final NetworkTableEntry ntFittedWidthEntry;
    private final NetworkTableEntry ntBoundingHeightEntry;
    private final NetworkTableEntry ntBoundingWidthEntry;
    private final NetworkTableEntry ntTargetRotation;
    private final ObjectMapper objectMapper;

    public NTConsumer(String name) {
        objectMapper = new ObjectMapper();
        NetworkTable table = NetworkTableInstance.getDefault().getTable(NTManager.TableName + name);
        ntValidEntry = table.getEntry("isValid");

        ntPitchEntry = table.getEntry("targetPitch");
        ntYawEntry = table.getEntry("targetYaw");
        ntAreaEntry = table.getEntry("targetArea");

        ntFittedHeightEntry = table.getEntry("targetFittedHeight");
        ntFittedWidthEntry = table.getEntry("targetFittedWidth");
        ntBoundingHeightEntry = table.getEntry("targetBoundingHeight");
        ntBoundingWidthEntry = table.getEntry("targetBoundingWidth");
        ntTargetRotation = table.getEntry("targetRotation");

        ntPoseEntry = table.getEntry("targetPose");
        ntAuxListEntry = table.getEntry("auxTargets");
        ntLatencyEntry = table.getEntry("latency");
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {}

    @Override
    public void onNext(CVPipelineResult result) {
        TrackedTarget bestTarget = result.targets.get(0);
        ntValidEntry.setBoolean(result.hasTargets);

        ntPitchEntry.setNumber(bestTarget.getPitch());
        ntYawEntry.setNumber(bestTarget.getYaw());
        ntAreaEntry.setNumber(bestTarget.getArea());

        ntBoundingHeightEntry.setDouble(bestTarget.getBoundingRect().height);
        ntBoundingWidthEntry.setDouble(bestTarget.getBoundingRect().width);
        ntFittedHeightEntry.setDouble(bestTarget.getMinAreaRect().size.height);
        ntFittedWidthEntry.setDouble(bestTarget.getMinAreaRect().size.width);

        ntTargetRotation.setNumber(bestTarget.getMinAreaRect().angle);
        double[] targetArray = {
            bestTarget.getRobotRelativePose().getTranslation().getX(),
            bestTarget.getRobotRelativePose().getTranslation().getY(),
            bestTarget.getRobotRelativePose().getRotation().getDegrees()
        };
        ntPoseEntry.setDoubleArray(targetArray);

        try {
            ntAuxListEntry.setString(
                    objectMapper.writeValueAsString(
                            result.targets.stream()
                                    .map(
                                            it ->
                                                    List.of(
                                                            it.getPitch(),
                                                            it.getYaw(),
                                                            it.getArea(),
                                                            it.getBoundingRect().width,
                                                            it.getBoundingRect().height,
                                                            it.getMinAreaRect().size.width,
                                                            it.getMinAreaRect().size.height,
                                                            it.getMinAreaRect().angle,
                                                            it.getRobotRelativePose()))
                                    .collect(Collectors.toList())));
        } catch (JsonProcessingException e) {
            logger.error(e.toString());
        }
        ntLatencyEntry.setNumber(result.getLatencyMillis());

        NetworkTableInstance.getDefault().flush();
    }

    @Override
    public void onError(@NonNull Throwable e) {}

    @Override
    public void onComplete() {}
}
