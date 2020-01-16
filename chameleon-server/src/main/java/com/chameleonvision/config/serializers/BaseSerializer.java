package com.chameleonvision.config.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.List;

public abstract class BaseSerializer<T> extends StdSerializer<T> {
    protected BaseSerializer(Class<T> t) {
        super(t);
    }

    JsonGenerator generator;

    void writeNumberListAsIntArray(String name, List<Number> list) throws IOException {
        generator.writeArrayFieldStart(name);
        int[] vals = list.stream().mapToInt(i->(Integer)i).toArray();
        generator.writeArray(vals, 0, vals.length);
        generator.writeEndArray();
    }

    void writeNumberListAsDoubleArray(String name, List<Number> list) throws IOException {
        generator.writeArrayFieldStart(name);
        double[] vals = list.stream().mapToDouble(i->(Double)i).toArray();
        generator.writeArray(vals, 0, vals.length);
        generator.writeEndArray();
    }

    <E extends Enum<E>> void writeEnum(String name, E num) throws IOException {
        generator.writeFieldName(name);
        generator.writeString(num.name());
    }
}
