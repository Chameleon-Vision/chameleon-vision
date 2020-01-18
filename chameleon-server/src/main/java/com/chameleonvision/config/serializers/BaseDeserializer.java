package com.chameleonvision.config.serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDeserializer<T> extends StdDeserializer<T> {
    protected BaseDeserializer(Class<?> vc) {
        super(vc);
    }

    JsonNode baseNode;

    private static final CollectionType numberListColType = TypeFactory.defaultInstance().constructCollectionType(List.class, Number.class);
    private CollectionType pointListColType = TypeFactory.defaultInstance().constructCollectionType(List.class, Object.class);
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
    MatOfPoint3f getMatOfPoint3f(String name, MatOfPoint3f defaultValue) throws JsonProcessingException {
        JsonNode node = baseNode.get(name);
        if (nodeGood(node)){
            List<List<Double>> doubleList = mapper.readValue(node.asText(), pointListColType);
            List<Point3> point3List = new ArrayList<>();
            for (List<Double> tmp : doubleList){
                Point3 p = new Point3();
                p.x = tmp.get(0);
                p.y = tmp.get(1);
                p.z = tmp.get(2);
                point3List.add(p);
            }
            MatOfPoint3f mat = new MatOfPoint3f();
            mat.fromList(point3List);
            return mat;
        }

        return defaultValue;
    }
}
