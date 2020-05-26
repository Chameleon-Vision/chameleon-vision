package com.chameleonvision.common.dataflow.events;

import com.chameleonvision.common.dataflow.DataChangeDestination;
import com.chameleonvision.common.dataflow.DataChangeSource;
import java.util.HashMap;

public class IncomingWebSocketEvent<T> extends DataChangeEvent<T> {
    public IncomingWebSocketEvent(DataChangeDestination destType, String propertyName, T newValue) {
        super(DataChangeSource.DMS_WEBSOCKET, destType, propertyName, newValue);
    }

    @SuppressWarnings("unchecked")
    public IncomingWebSocketEvent(
            DataChangeDestination destType, String dataKey, HashMap<String, Object> data) {
        this(destType, dataKey, (T) data.get(dataKey));
    }
}
