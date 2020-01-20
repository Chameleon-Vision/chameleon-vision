package com.chameleonvision.config.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseSerializer<T> extends StdSerializer<T> {
    protected BaseSerializer(Class<T> t) {
        super(t);
    }

    JsonGenerator generator;

    void writeNumberListAsIntArray(String name, List<Number> list) throws IOException {
        generator.writeArrayFieldStart(name);
        int[] vals = list.stream().mapToInt(i -> (Integer) i).toArray();
        generator.writeArray(vals, 0, vals.length);
        generator.writeEndArray();
    }

    void writeNumberListAsDoubleArray(String name, List<Number> list) throws IOException {
        generator.writeArrayFieldStart(name);
        double[] vals = list.stream().mapToDouble(Number::doubleValue).toArray();
        generator.writeArray(vals, 0, vals.length);
        generator.writeEndArray();
    }

    <E extends Enum<E>> void writeEnum(String name, E num) throws IOException {
        generator.writeFieldName(name);
        generator.writeString(num.name());
    }

    void writeMatOfPoint3f(String name, MatOfPoint3f mat) throws IOException {
        List<Point3> point3List = mat.toList();
        generator.writeArrayFieldStart(name);

        for (Point3 point3 : point3List) {
            double[] tmp = {point3.x, point3.y, point3.z};
            generator.writeObject(tmp);
        }
        generator.writeEndArray();
    }
}
