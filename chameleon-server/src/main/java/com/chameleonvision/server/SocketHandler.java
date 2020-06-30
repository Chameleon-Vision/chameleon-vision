package com.chameleonvision.server;

import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.common.vision.processes.VisionModuleManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.websocket.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import org.msgpack.jackson.dataformat.MessagePackFactory;

@SuppressWarnings("rawtypes")
public class SocketHandler {

    private static final Logger logger = new Logger(SocketHandler.class, LogGroup.Server);
    private static final List<WsContext> users = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());

    public void onConnect(WsConnectContext context) {
        users.add(context);
        sendFullSettings();
    }

    protected void onClose(WsCloseContext context) {
        users.remove(context);
    }

    public void onBinaryMessage(WsBinaryMessageContext context) {
        try {
            Map<String, Object> deserializedData =
                    objectMapper.readValue(context.data(), new TypeReference<>() {});

            for (Map.Entry<String, Object> entry : deserializedData.entrySet()) {
                try {
                    var entryKey = entry.getKey();
                    var entryValue = entry.getValue();
                    switch (entryKey) {
                        case "currentCamera":
                            {
                                VisionModuleManager.changeCamera((Integer) entryValue);
                                break;
                            }
                        default:
                            {
                                VisionModuleManager.getUIvisionModule()
                                        .getUIDataProvider()
                                        .onMessage(entryKey, entryValue);
                                break;
                            }
                    }
                } catch (Exception ex) {
                    // ignored
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static void sendFullSettings() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("cameraSettings", VisionModuleManager.getUIvisionModule().getOrdinalCamera());
        map.put(
                "pipeline", VisionModuleManager.getUIvisionModule().pipelineManager.getOrdinalPipeline());
        map.put(
                "pipelineList",
                VisionModuleManager.getUIvisionModule().pipelineManager.getPipelineNameList());
        map.put("cameraList", VisionModuleManager.getCameraNickNameList());
        try {
            broadcastMessage(map);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
    }

    public static void sendMessage(Object message, WsContext user) throws JsonProcessingException {
        ByteBuffer b = ByteBuffer.wrap(objectMapper.writeValueAsBytes(message));
        user.send(b);
    }

    public static void broadcastMessage(Object message) throws JsonProcessingException {
        broadcastMessage(message, null);
    }

    public static void broadcastMessage(Object message, WsContext userToSkip)
            throws JsonProcessingException {
        for (WsContext user : users) {
            if (!user.getSessionId().equals(userToSkip)) {
                sendMessage(message, user);
            }
        }
    }
}
