package com.chameleonvision.server;

import com.chameleonvision.common.dataflow.DataChangeDestination;
import com.chameleonvision.common.dataflow.DataChangeService;
import com.chameleonvision.common.dataflow.DataChangeSource;
import com.chameleonvision.common.dataflow.events.IncomingWebSocketEvent;
import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.websocket.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.msgpack.jackson.dataformat.MessagePackFactory;

public class SocketHandler {
    private static final Logger logger = new Logger(SocketHandler.class, LogGroup.Server);

    static List<WsContext> users = new ArrayList<>();
    static ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());

    private static final DataChangeService dcService = DataChangeService.getInstance();
    private static final DataChangeSource dcSource = DataChangeSource.DMS_WEBSOCKET;

    public static void onConnect(WsConnectContext context) {
        users.add(context);
    }

    protected static void onClose(WsCloseContext context) {
        users.remove(context);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onBinaryMessage(WsBinaryMessageContext context) {
        try {
            Map<String, Object> deserializedData =
                    objectMapper.readValue(context.data(), new TypeReference<>() {});

            for (Map.Entry<String, Object> entry : deserializedData.entrySet()) {
                try {
                    var entryKey = entry.getKey();
                    var socketMessageType = SocketMessageType.fromEntryKey(entryKey);

                    if (socketMessageType == null) {
                        logger.error("Got unknown socket message type: " + entryKey);
                        continue;
                    }

                    switch (socketMessageType) {
                        case SMT_DRIVERMODE:
                            {
                                HashMap<String, Object> data = (HashMap<String, Object>) entry.getValue();
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
                            break;
                        case SMT_CHANGEPIPELINENAME:
                            break;
                        case SMT_ADDNEWPIPELINE:
                            break;
                        case SMT_COMMAND:
                            break;
                        case SMT_CURRENTCAMERA:
                            break;
                        case SMT_IS3D:
                            break;
                        case SMT_CURRENTPIPELINE:
                            break;
                        case SMT_ISPNPCALIBRATION:
                            break;
                        case SMT_TAKECALIBRATIONSNAPSHOP:
                            break;
                        case SMT_ROTATIONMODE:
                            break;
                        case SMT_EXPOSURE:
                            break;
                        case SMT_BRIGHTNESS:
                            break;
                        case SMT_GAIN:
                            break;
                        case SMT_VIDEOMODEINDEX:
                            break;
                        case SMT_STREAMDIVISOR:
                            break;
                        default:
                            {
                                break;
                            }
                    }
                } catch (Exception ex) {
                    // ignored
                }
            }

            // TODO pass data to ui data provider
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: change to use the DataFlow system
    public static void sendMessage(Object message, WsContext user) throws JsonProcessingException {
        ByteBuffer b = ByteBuffer.wrap(objectMapper.writeValueAsBytes(message));
        user.send(b);
    }

    // TODO: change to use the DataFlow system
    public static void broadcastMessage(Object message, WsContext userToSkip)
            throws JsonProcessingException {
        for (WsContext user : users) {
            if (user != userToSkip) {
                sendMessage(message, user);
            }
        }
    }

    // TODO: change to use the DataFlow system
    public static void broadcastMessage(Object message) throws JsonProcessingException {
        broadcastMessage(message, null);
    }
}
