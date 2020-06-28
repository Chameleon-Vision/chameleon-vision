package com.chameleonvision.server;

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

    public void onConnect(WsConnectContext context) {
        users.add(context);
    }

    protected void onClose(WsCloseContext context) {
        users.remove(context);
    }

    public void onBinaryMessage(WsBinaryMessageContext context) {
        try {
            Map<String, Object> deserializedData =
                    objectMapper.readValue(context.data(), new TypeReference<>() {
                    });

            for (Map.Entry<String, Object> entry : deserializedData.entrySet()) {
                try {
                    var entryKey = entry.getKey();
                    var entryValue = entry.getValue();
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
