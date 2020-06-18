package com.chameleonvision.common.dataflow.events;

import com.chameleonvision.common.dataflow.DataChangeDestination;
import com.chameleonvision.common.dataflow.DataChangeSource;
import com.chameleonvision.server.UIUpdateType;
import java.util.HashMap;

public class OutgoingUIEvent<T> extends DataChangeEvent<T> {
    public final UIUpdateType updateType;

    public OutgoingUIEvent(UIUpdateType updateType, String propertyName, T newValue) {
        super(DataChangeSource.DCS_WEBSOCKET, DataChangeDestination.DCD_UI, propertyName, newValue);
        this.updateType = updateType;
    }

    @SuppressWarnings("unchecked")
    public OutgoingUIEvent(UIUpdateType updateType, String dataKey, HashMap<String, Object> data) {
        this(updateType, dataKey, (T) data.get(dataKey));
    }
}
