package com.chameleonvision.common.dataflow;

import java.util.Arrays;
import java.util.List;

public enum DataChangeSource {
    DCS_WEBSOCKET,
    DCS_HTTP,
    DCS_NETWORKTABLES,
    DCS_VISIONMODULE,
    DCS_OTHER;

    public static final List<DataChangeSource> AllSources = Arrays.asList(DataChangeSource.values());
}
