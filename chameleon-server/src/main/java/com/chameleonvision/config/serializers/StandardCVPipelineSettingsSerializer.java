package com.chameleonvision.config.serializers;

import com.chameleonvision.vision.pipeline.impl.StandardCVPipelineSettings;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.List;

public class StandardCVPipelineSettingsSerializer extends BaseSerializer<StandardCVPipelineSettings> {
    protected StandardCVPipelineSettingsSerializer(Class<StandardCVPipelineSettings> t) {
        super(t);
    }

    @Override
    public void serialize(StandardCVPipelineSettings pipeline, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // set BaseSerializer generator reference.
        generator = gen;

        gen.writeStartObject();

        gen.writeNumberField("index", pipeline.index);

        writeEnum("flipMode", pipeline.flipMode);
        writeEnum("rotationMode", pipeline.rotationMode);

        gen.writeStringField("nickname", pipeline.nickname);

        gen.writeNumberField("exposure", pipeline.exposure);
        gen.writeNumberField("brightness", pipeline.brightness);
        gen.writeNumberField("gain", pipeline.gain);

        gen.writeNumberField("videoModeIndex", pipeline.videoModeIndex);

        writeEnum("streamDivisor", pipeline.streamDivisor);

        writeNumberListAsIntArray("hue", pipeline.hue);
        writeNumberListAsIntArray("saturation", pipeline.saturation);
        writeNumberListAsIntArray("value", pipeline.value);

        gen.writeBooleanField("erode", pipeline.erode);
        gen.writeBooleanField("dilate", pipeline.dilate);

        writeNumberListAsDoubleArray("area", pipeline.area);
        writeNumberListAsDoubleArray("ratio", pipeline.ratio);
        writeNumberListAsDoubleArray("extent", pipeline.extent);

        // speckle rejection
        gen.writeNumberField("speckle", (Integer) pipeline.speckle);

        // stream output (camera feed, or thresholded feed)
        gen.writeBooleanField("isBinary", pipeline.isBinary);

        writeEnum("sortMode", pipeline.sortMode);
        writeEnum("targetRegion", pipeline.targetRegion);
        writeEnum("targetOrientation", pipeline.targetOrientation);

        // show multiple targets when drawing
        gen.writeBooleanField("multiple", pipeline.multiple);

        writeEnum("targetGroup", pipeline.targetGroup);
        writeEnum("targetIntersection", pipeline.targetIntersection);

        // single calibration point
        writeNumberListAsIntArray("point", pipeline.point);

        // target X/Y calibration
        writeEnum("calibrationMode", pipeline.calibrationMode);

        // TODO: better names? or use an array?
        gen.writeNumberField("dualTargetCalibrationM", pipeline.dualTargetCalibrationM);
        gen.writeNumberField("dualTargetCalibrationB", pipeline.dualTargetCalibrationB);

        // TODO: how tf do we do 'targetCornerMat'

        gen.writeBooleanField("is3D", pipeline.is3D);
        writeMatOfPoint3f("targetCornerMat", pipeline.targetCornerMat);
        gen.writeEndObject();
    }
}
