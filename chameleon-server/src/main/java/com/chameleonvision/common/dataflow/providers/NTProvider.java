package com.chameleonvision.common.dataflow.providers;

import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.common.vision.processes.VisionModule;
import edu.wpi.first.networktables.*;

public class NTProvider extends Provider {
    private final Logger logger = new Logger(NTProvider.class, LogGroup.VisionProcess);
    NetworkTableEntry DriverModeEntry;
    NetworkTableEntry PipelineEntry;

    public NTProvider(VisionModule module) {
        super(module);
        NetworkTable table =
                NetworkTableInstance.getDefault()
                        .getTable("/chameleon-vision/" + module.getSourceNickname());

        DriverModeEntry.addListener(this::ChangeDriverMode, EntryListenerFlags.kUpdate);
        PipelineEntry.addListener(this::ChangePipeline, EntryListenerFlags.kUpdate);
    }

    private void ChangeDriverMode(EntryNotification driverModeEntryNotification) {
        boolean state = driverModeEntryNotification.value.getBoolean();
        if (state) {
            parentModule.pipelineManager.enterDriverMode();
        } else {
            parentModule.pipelineManager.exitAuxiliaryPipeline();
        }
    }

    private void ChangePipeline(EntryNotification pipelineEntryNotification) {
        int index = (int) pipelineEntryNotification.value.getDouble();
        parentModule.pipelineManager.changeCurrentPipeline(index);
    }
}
