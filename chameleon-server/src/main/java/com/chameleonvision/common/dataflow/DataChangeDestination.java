package com.chameleonvision.common.dataflow;

import java.util.Arrays;
import java.util.List;

public enum DataChangeDestination {
    DCD_ACTIVEMODULE,
    DCD_ACTIVEPIPELINESETTINGS,
    DCD_GENSETTINGS,
    DCD_OTHER;

    public static final List<DataChangeDestination> AllDestinations = Arrays.asList(DataChangeDestination.values());
}
