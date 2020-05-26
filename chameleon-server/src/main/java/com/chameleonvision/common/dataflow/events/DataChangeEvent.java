package com.chameleonvision.common.dataflow.events;

import com.chameleonvision.common.dataflow.DataChangeDestination;
import com.chameleonvision.common.dataflow.DataChangeSource;

public class DataChangeEvent<T> {
    public final DataChangeSource sourceType;
    public final DataChangeDestination destType;
    public final String propertyName;
    public final T newValue;

    public DataChangeEvent(
            DataChangeSource sourceType,
            DataChangeDestination destType,
            String propertyName,
            T newValue) {
        this.sourceType = sourceType;
        this.destType = destType;
        this.propertyName = propertyName;
        this.newValue = newValue;
    }
}
