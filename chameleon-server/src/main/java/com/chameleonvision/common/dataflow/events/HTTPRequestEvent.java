package com.chameleonvision.common.dataflow.events;

import com.chameleonvision.common.dataflow.DataChangeDestination;
import com.chameleonvision.common.dataflow.DataChangeSource;

public class HTTPRequestEvent<T> extends DataChangeEvent<T> {
    public HTTPRequestEvent(
            DataChangeSource sourceType,
            DataChangeDestination destType,
            String propertyName,
            T newValue) {
        super(sourceType, destType, propertyName, newValue);
    }
}
