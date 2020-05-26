package com.chameleonvision.common.dataflow;

import com.chameleonvision.common.dataflow.events.DataChangeEvent;
import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("rawtypes")
public class DataChangeService {

    private static final Logger logger = new Logger(DataChangeService.class, LogGroup.Server);

    private DataChangeService() {
        subscribers = new CopyOnWriteArrayList<>();
    }

    private static class ThreadSafeSingleton {
        private static final DataChangeService INSTANCE = new DataChangeService();
    }

    public static DataChangeService getInstance() {
        return ThreadSafeSingleton.INSTANCE;
    }

    private final CopyOnWriteArrayList<DataChangeSubscriber> subscribers;

    public void subscribe(DataChangeSubscriber subscriber) {
        if (!subscribers.addIfAbsent(subscriber)) {
            logger.warn("Attempted to add already added subscriber!");
        }
    }

    // TODO: Async-ify this somehow?
    public void sendEvent(DataChangeEvent event) {
        for (var subscriber : subscribers) {
            if (subscriber.wantedSources.contains(event.sourceType)
                    && subscriber.wantedDestinations.contains(event.destType))
                subscriber.onDataChangeEvent(event);
        }
    }

    // TODO: Async-ify this somehow?
    public void sendEvents(DataChangeEvent... events) {
        for (var event : events) {
            sendEvent(event);
        }
    }
}
