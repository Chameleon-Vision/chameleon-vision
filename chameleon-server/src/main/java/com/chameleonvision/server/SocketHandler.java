package com.chameleonvision.server;

import com.chameleonvision.common.dataflow.DataChangeDestination;
import com.chameleonvision.common.dataflow.DataChangeService;
import com.chameleonvision.common.dataflow.DataChangeSource;
import com.chameleonvision.common.dataflow.DataChangeSubscriber;
import com.chameleonvision.common.dataflow.events.DataChangeEvent;
import com.chameleonvision.common.dataflow.events.IncomingWebSocketEvent;
import com.chameleonvision.common.dataflow.events.OutgoingUIEvent;
import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.common.vision.pipeline.PipelineType;
import com.chameleonvision.common.vision.processes.PipelineManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.websocket.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.msgpack.jackson.dataformat.MessagePackFactory;

@SuppressWarnings("rawtypes")
public class SocketHandler {

    private final Logger logger = new Logger(SocketHandler.class, LogGroup.Server);
    private final List<WsContext> users = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
    private final DataChangeService dcService = DataChangeService.getInstance();

    @SuppressWarnings("FieldCanBeLocal")
    private final UIOutboundSubscriber uiOutboundSubscriber = new UIOutboundSubscriber();

    public static class UIMap extends HashMap<String, Object> {}

    private abstract static class SelectiveBroadcastPair extends Pair<UIMap, WsContext> {}

    @SuppressWarnings("rawtypes")
    private class UIOutboundSubscriber extends DataChangeSubscriber {

        public UIOutboundSubscriber() {
            super(DataChangeSource.AllSources, Collections.singletonList(DataChangeDestination.DCD_UI));
        }

        @Override
        public void onDataChangeEvent(DataChangeEvent event) {
            if (event instanceof OutgoingUIEvent) {
                var thisEvent = (OutgoingUIEvent) event;
                try {
                    switch (thisEvent.updateType) {
                        case BROADCAST:
                            {
                                if (event.data instanceof HashMap) {
                                    var data = (UIMap) event.data;
                                    broadcastMessage(data, null);
                                } else {
                                    broadcastMessage(event.data, null);
                                }
                                break;
                            }
                        case SINGLEUSER:
                            {
                                if (event.data instanceof Pair) {
                                    var pair = (SelectiveBroadcastPair) event.data;
                                    broadcastMessage(pair.getLeft(), pair.getRight());
                                }
                                break;
                            }
                    }
                } catch (JsonProcessingException e) {
                    // TODO: Log
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ThreadSafeSingleton {
        private static final SocketHandler INSTANCE = new SocketHandler();
    }

    public static SocketHandler getInstance() {
        return SocketHandler.ThreadSafeSingleton.INSTANCE;
    }

    private SocketHandler() {
        dcService.subscribe(uiOutboundSubscriber);
    }

    public void onConnect(WsConnectContext context) {
        users.add(context);
    }

    protected void onClose(WsCloseContext context) {
        users.remove(context);
    }

    @SuppressWarnings({"unchecked"})
    public void onBinaryMessage(WsBinaryMessageContext context) {
        try {
            Map<String, Object> deserializedData =
                    objectMapper.readValue(context.data(), new TypeReference<>() {});

            for (Map.Entry<String, Object> entry : deserializedData.entrySet()) {
                try {
                    var entryKey = entry.getKey();
                    var entryValue = entry.getValue();
                    var socketMessageType = SocketMessageType.fromEntryKey(entryKey);

                    if (socketMessageType == null) {
                        logger.error("Got unknown socket message type: " + entryKey);
                        continue;
                    }

                    switch (socketMessageType) {
                        case SMT_DRIVERMODE:
                            {
                                var data = (HashMap<String, Object>) entryValue;
                                var dmExpEvent =
                                        new IncomingWebSocketEvent<Integer>(
                                                DataChangeDestination.DCD_ACTIVEMODULE, "driverExposure", data);
                                var dmBrightEvent =
                                        new IncomingWebSocketEvent<Integer>(
                                                DataChangeDestination.DCD_ACTIVEMODULE, "driverBrightness", data);
                                var dmIsDriverEvent =
                                        new IncomingWebSocketEvent<Boolean>(
                                                DataChangeDestination.DCD_ACTIVEMODULE, "isDriver", data);

                                dcService.publishEvents(dmExpEvent, dmBrightEvent, dmIsDriverEvent);
                                break;
                            }
                        case SMT_CHANGECAMERANAME:
                            {
                                var ccnEvent =
                                        new IncomingWebSocketEvent<>(
                                                DataChangeDestination.DCD_ACTIVEMODULE,
                                                "cameraNickname",
                                                (String) entryValue);
                                dcService.publishEvent(ccnEvent);
                                break;
                            }
                        case SMT_CHANGEPIPELINENAME:
                            {
                                var cpnEvent =
                                        new IncomingWebSocketEvent<>(
                                                DataChangeDestination.DCD_ACTIVEMODULE,
                                                "pipelineName",
                                                (String) entryValue);
                                dcService.publishEvent(cpnEvent);
                                break;
                            }
                        case SMT_ADDNEWPIPELINE:
                            {
                                HashMap<String, Object> data = (HashMap<String, Object>) entryValue;
                                var type = (PipelineType) data.get("pipelineType");
                                var name = (String) data.get("pipelineName");

                                var newPipelineEvent =
                                        new IncomingWebSocketEvent<>(
                                                DataChangeDestination.DCD_ACTIVEMODULE,
                                                "newPipelineInfo",
                                                Pair.of(name, type));
                                dcService.publishEvent(newPipelineEvent);
                                break;
                            }
                        case SMT_COMMAND:
                            {
                                var cmd = SocketMessageCommandType.valueOf((String) entryValue);
                                switch (cmd) {
                                    case SMCT_DELETECURRENTPIPELINE:
                                        {
                                            var deleteCurrentPipelineEvent =
                                                    new IncomingWebSocketEvent<>(
                                                            DataChangeDestination.DCD_ACTIVEMODULE, "deleteCurrPipeline", 0);
                                            dcService.publishEvent(deleteCurrentPipelineEvent);
                                            break;
                                        }
                                    case SMCT_SAVE:
                                        {
                                            var saveEvent =
                                                    new IncomingWebSocketEvent<>(DataChangeDestination.DCD_OTHER, "save", 0);
                                            dcService.publishEvent(saveEvent);
                                            break;
                                        }
                                }
                            }
                        case SMT_CURRENTCAMERA:
                            {
                                var changeCurrentCameraEvent =
                                        new IncomingWebSocketEvent<>(
                                                DataChangeDestination.DCD_OTHER, "changeUICamera", (Integer) entryValue);
                                dcService.publishEvent(changeCurrentCameraEvent);
                                break;
                            }
                        case SMT_CURRENTPIPELINE:
                            {
                                var changePipelineEvent =
                                        new IncomingWebSocketEvent<>(
                                                DataChangeDestination.DCD_ACTIVEMODULE,
                                                "changePipeline",
                                                (Integer) entryValue);
                                dcService.publishEvent(changePipelineEvent);
                                break;
                            }
                        case SMT_ISPNPCALIBRATION:
                            {
                                var changePipelineEvent =
                                        new IncomingWebSocketEvent<>(
                                                DataChangeDestination.DCD_ACTIVEMODULE,
                                                "changePipeline",
                                                PipelineManager.CAL_3D_INDEX);
                                dcService.publishEvent(changePipelineEvent);
                                break;
                            }
                        case SMT_TAKECALIBRATIONSNAPSHOT:
                            {
                                var takeCalSnapshotEvent =
                                        new IncomingWebSocketEvent<>(
                                                DataChangeDestination.DCD_ACTIVEMODULE, "takeCalSnapshot", 0);
                                dcService.publishEvent(takeCalSnapshotEvent);
                                break;
                            }
                        case SMT_PIPELINESETTINGCHANGE:
                            {
                                HashMap<String, Object> data = (HashMap<String, Object>) entryValue;

                                // there shall only be one.
                                Map.Entry<String, Object> thisEntry = data.entrySet().iterator().next();

                                var pipelineSettingChangeEvent =
                                        new IncomingWebSocketEvent<>(
                                                DataChangeDestination.DCD_ACTIVEPIPELINESETTINGS,
                                                thisEntry.getKey(),
                                                thisEntry.getValue());

                                dcService.publishEvent(pipelineSettingChangeEvent);
                            }
                        default:
                            {
                                logger.warn("Unknown Socket Message - Name: " + entryKey);
                                break;
                            }
                    }
                } catch (Exception ex) {
                    // ignored
                }
            }
        } catch (IOException e) {
            // TODO: log
            e.printStackTrace();
        }
    }

    // TODO: change to use the DataFlow system
    private void sendMessage(Object message, WsContext user) throws JsonProcessingException {
        ByteBuffer b = ByteBuffer.wrap(objectMapper.writeValueAsBytes(message));
        user.send(b);
    }

    // TODO: change to use the DataFlow system
    private void broadcastMessage(Object message, WsContext userToSkip)
            throws JsonProcessingException {
        for (WsContext user : users) {
            if (user != userToSkip) {
                sendMessage(message, user);
            }
        }
    }
}
