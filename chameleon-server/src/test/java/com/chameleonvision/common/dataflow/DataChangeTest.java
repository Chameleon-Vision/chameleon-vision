package com.chameleonvision.common.dataflow;

import com.chameleonvision.common.dataflow.events.DataChangeEvent;
import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataChangeTest {

    private static class TestSubscriber extends DataChangeSubscriber {
        private final Logger logger = new Logger(this.getClass(), LogGroup.General);

        private int eventCount;

        public TestSubscriber(
                List<DataChangeSource> wantedSources, List<DataChangeDestination> wantedDestinations) {
            super(wantedSources, wantedDestinations);
        }

        @Override
        public void onDataChangeEvent(DataChangeEvent event) {
            eventCount++;
            logger.debug(
                    "got event - "
                            + "src: "
                            + event.sourceType
                            + ", dest: "
                            + event.destType
                            + ", prop: "
                            + event.propertyName
                            + ", val: "
                            + event.data);
        }
    }

    @Test
    public void testSubscriptions() {
        var dcService = DataChangeService.getInstance();

        var websocketSubscriber =
                new TestSubscriber(
                        List.of(DataChangeSource.DCS_WEBSOCKET), DataChangeDestination.AllDestinations);
        var httpSubscriber =
                new TestSubscriber(
                        List.of(DataChangeSource.DCS_HTTP), DataChangeDestination.AllDestinations);

        dcService.subscribe(websocketSubscriber);
        dcService.subscribe(httpSubscriber);

        dcService.publishEvent(
                new DataChangeEvent(
                        DataChangeSource.DCS_WEBSOCKET,
                        DataChangeDestination.DCD_ACTIVEMODULE,
                        "something",
                        0));
        dcService.publishEvent(
                new DataChangeEvent(
                        DataChangeSource.DCS_HTTP, DataChangeDestination.DCD_ACTIVEMODULE, "somethingElse", 0));

        // hack to ensure event gets processed before asserting.
        while (dcService.hasEvents()) {}

        Assertions.assertEquals(
                1, websocketSubscriber.eventCount, "Websocket Subscriber did not receive event!");
        Assertions.assertEquals(1, httpSubscriber.eventCount, "HTTP Subscriber did not receive event!");
    }
}
