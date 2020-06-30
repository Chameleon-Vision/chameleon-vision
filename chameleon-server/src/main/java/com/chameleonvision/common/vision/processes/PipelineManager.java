package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.vision.pipeline.*;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings({"rawtypes", "unused"})
public class PipelineManager {

    public static final int DRIVERMODE_INDEX = -1;
    public static final int CAL_3D_INDEX = -2;

    public final List<CVPipeline> userPipelines;
    private final Calibration3dPipeline calibration3dPipeline = new Calibration3dPipeline();
    private final DriverModePipeline driverModePipeline = new DriverModePipeline();

    /** Index of the currently active pipeline. */
    private int currentPipelineIndex = DRIVERMODE_INDEX;

    /**
    * Index of the last active user-created pipeline. <br>
    * <br>
    * Used only when switching from any of the built-in pipelines back to a user-created pipeline.
    */
    private int lastPipelineIndex;

    /**
    * Creates a PipelineManager with a DriverModePipeline, a Calibration3dPipeline, and all provided
    * pipelines.
    *
    * @param userPipelines Pipelines to add to the manager.
    */
    public PipelineManager(List<CVPipeline> userPipelines) {
        this.userPipelines = userPipelines;
    }

    /** Creates a PipelineManager with a DriverModePipeline, and a Calibration3dPipeline. */
    public PipelineManager() {
        this(List.of());
    }

    /**
    * Get a pipeline by index.
    *
    * @param index Index of desired pipeline.
    * @return The desired pipeline.
    */
    public CVPipeline getPipeline(int index) {
        if (index < 0) {
            switch (index) {
                case DRIVERMODE_INDEX:
                    return driverModePipeline;
                case CAL_3D_INDEX:
                    return calibration3dPipeline;
            }
        }

        return userPipelines.get(index);
    }

    /**
    * Get the settings for a pipeline by index.
    *
    * @param index Index of pipeline whose settings need getting.
    * @return The gotten settings of the pipeline whose index was provided.
    */
    public CVPipelineSettings getPipelineSettings(int index) {
        return getPipeline(index).getSettings();
    }

    /**
    * Get the currently active pipeline.
    *
    * @return The currently active pipeline.
    */
    public CVPipeline getCurrentPipeline() {
        return getPipeline(currentPipelineIndex);
    }

    /**
    * Get the currently active pipelines settings
    *
    * @return The currently active pipelines settings
    */
    public CVPipelineSettings getCurrentPipelineSettings() {
        return getPipelineSettings(currentPipelineIndex);
    }

    /**
    * Internal method for setting the active pipeline. <br>
    * <br>
    * All externally accessible methods that intend to change the active pipeline MUST go through
    * here to ensure all proper steps are taken.
    *
    * @param index Index of pipeline to be active
    */
    private void setPipelineInternal(int index) {
        if (index < 0) {
            lastPipelineIndex = currentPipelineIndex;
        }

        currentPipelineIndex = index;
    }

    /**
    * Leaves the current built-in pipeline, if applicable, and sets the active pipeline to the most
    * recently active user-created pipeline.
    */
    public void exitAuxiliaryPipeline() {
        if (currentPipelineIndex < 0) {
            setPipelineInternal(lastPipelineIndex);
        }
    }

    public void enterDriverMode() {
        setPipelineInternal(DRIVERMODE_INDEX);
    }

    public static final Comparator<CVPipelineSettings> PipelineSettingsIndexComparator =
            (o1, o2) -> {
                int o1Index = o1.pipelineIndex;
                int o2Index = o2.pipelineIndex;

                if (o1Index == o2Index) {
                    return 0;
                } else if (o1Index < o2Index) {
                    return -1;
                }
                return 1;
            };

    public static final Comparator<CVPipeline> PipelineIndexComparator =
            (o1, o2) -> PipelineSettingsIndexComparator.compare(o1.getSettings(), o2.getSettings());

    /**
    * Sorts the pipeline list by index, and reassigns their indexes to match the new order. <br>
    * <br>
    * I don't like this but I have no other ideas, and it works so ¯\_(ツ)_/¯
    */
    private void reassignIndexes() {
        userPipelines.sort(PipelineIndexComparator);
        for (int i = 0; i < userPipelines.size(); i++) {
            getPipelineSettings(i).pipelineIndex = i;
        }
    }

    public void removePipeline(int index) {
        if (index == currentPipelineIndex) {
            currentPipelineIndex -= 1;
        }
        userPipelines.remove(index);
        reassignIndexes();
    }

    public void removeCurrentPipeline() {
        removePipeline(currentPipelineIndex);
    }

    public void addPipeline(CVPipeline cvPipeline) {
        cvPipeline.getSettings().pipelineIndex = userPipelines.size() - 1;
        userPipelines.add(cvPipeline);
    }

    public void changeCurrentPipeline(int index) {
        setPipelineInternal(index);
    }
}
