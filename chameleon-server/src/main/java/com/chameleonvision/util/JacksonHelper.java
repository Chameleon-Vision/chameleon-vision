package com.chameleonvision.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JacksonHelper {
    private JacksonHelper() {} // no construction, utility class

    public static <T> void serializer(Path path, T object) throws IOException {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType(object.getClass()).build();
        ObjectMapper objectMapper = JsonMapper.builder().activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT).build();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(path.toString()), object);
    }

    public static <T> T deserializer(Path path, Class<T> ref) throws IOException {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType(ref).build();
        ObjectMapper objectMapper = JsonMapper.builder().activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT).build();
        File jsonFile = new File(path.toString());
        if (jsonFile.exists() && jsonFile.length() > 0) {
            return objectMapper.readValue(jsonFile, ref);
        }
        return null;
    }

    public static class CoeffMatSerializer extends StdSerializer<Mat> {

        protected CoeffMatSerializer(Class<Mat> t) {
            super(t);
        }

        @Override
        public void serialize(Mat mat, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();

            boolean isValidMat = mat.isContinuous() && mat.type() == 6;
            boolean isCamMatrix = mat.rows() == 3 && mat.cols() == 3;
            boolean isDistCoeffs = mat.rows() == 1 && mat.cols() == 5;

            if (!isCamMatrix || !isDistCoeffs || !isValidMat) {
                System.err.println("Tried to serialize non-coefficient Mat!");
                return;
            }

            gen.writeNumberField("rows", mat.rows());
            gen.writeNumberField("cols", mat.cols());
            gen.writeNumberField("type", mat.type());

            double[] data = new double[(int)(mat.total()*mat.elemSize())];
            mat.get(0, 0, data);

            gen.writeArrayFieldStart("data");
            for (double dat : data) {
                gen.writeNumber(dat);
            }
            gen.writeEndArray();
        }
    }

}
