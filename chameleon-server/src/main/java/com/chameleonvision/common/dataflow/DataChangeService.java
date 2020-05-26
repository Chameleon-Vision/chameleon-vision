package com.chameleonvision.common.dataflow;

import com.chameleonvision.common.dataflow.events.DataChangeEvent;
import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("rawtypes")
public class DataChangeService {

    private static final Logger logger = new Logger(DataChangeService.class, LogGroup.Server);

    private static class ThreadSafeSingleton {
        private static final DataChangeService INSTANCE = new DataChangeService();
    }

    public static DataChangeService getInstance() {
        return ThreadSafeSingleton.INSTANCE;
    }

    private final CopyOnWriteArrayList<DataChangeSubscriber> subscribers;

    @SuppressWarnings("FieldCanBeLocal")
    private final Thread dispatchThread;

    private final BlockingQueue<DataChangeEvent> eventQueue = new LinkedBlockingQueue<>();

    private DataChangeService() {
        subscribers = new CopyOnWriteArrayList<>();
        dispatchThread = new Thread(this::dispatchFromQueue);
        dispatchThread.start();
    }

    private void dispatchFromQueue() {
        while (true) {
            try {
                var taken = eventQueue.take();
                for (var sub : subscribers) {
                    if (sub.wantedSources.contains(taken.sourceType)
                            && sub.wantedDestinations.contains(taken.destType)) {
                        sub.onDataChangeEvent(taken);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void subscribe(DataChangeSubscriber subscriber) {
        if (!subscribers.addIfAbsent(subscriber)) {
            logger.warn("Attempted to add already added subscriber!");
        }
    }

    public void publishEvent(DataChangeEvent event) {
        eventQueue.offer(event);
    }

    public void publishEvents(DataChangeEvent... events) {
        for (var event : events) {
            publishEvent(event);
        }
    }
}
