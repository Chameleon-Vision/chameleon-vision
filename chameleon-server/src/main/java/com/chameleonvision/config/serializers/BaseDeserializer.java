package com.chameleonvision.config.serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.List;

public abstract class BaseDeserializer<T> extends StdDeserializer<T> {
    protected BaseDeserializer(Class<?> vc) {
        super(vc);
    }

    JsonNode baseNode;

    private static final CollectionType numberListColType = TypeFactory.defaultInstance().constructCollectionType(List.class, Number.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private static boolean nodeGood(JsonNode node) {
        return (nodeGood(node));
    }

    List<Number> getNumberList(String name, List<Number> defaultValue) throws JsonProcessingException {
        JsonNode node = baseNode.get(name);

        if (nodeGood(node)) {
            return mapper.readValue(node.asText(), numberListColType);
        }
        return defaultValue;
    }

    boolean getBoolean(String name, boolean defaultValue) {
        JsonNode node = baseNode.get(name);

        if (nodeGood(node)) {
            return node.booleanValue();
        }

        return defaultValue;
    }

    int getInt(String name, int defaultValue) {
        return (int) getDouble(name, defaultValue);
    }

    double getDouble(String name, double defaultValue) {
        JsonNode node = baseNode.get(name);

        if (nodeGood(node)) {
            return (double) node.numberValue();
        }

        return defaultValue;
    }

    String getString(String name, String defaultValue) {
        JsonNode node = baseNode.get(name);

        if (nodeGood(node)) {
            return node.asText();
        }

        return defaultValue;
    }

    <E extends Enum<E>> E getEnum(String name, Class<E> enumClass, E defaultValue) throws IOException {
        JsonNode node = baseNode.get(name);

        if (nodeGood(node)) {
            E[] possibleVals = enumClass.getEnumConstants();
            String jsonVal = baseNode.get(name).asText();

            for (E val : possibleVals) {
                if (val.name().equals(jsonVal)) {
                    return val;
                }
            }
        }

        return defaultValue;
    }
}
