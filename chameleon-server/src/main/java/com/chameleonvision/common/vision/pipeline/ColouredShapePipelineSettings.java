package com.chameleonvision.common.vision.pipeline;

import com.chameleonvision.common.vision.opencv.ContourGroupingMode;
import com.chameleonvision.common.vision.opencv.ContourIntersectionDirection;
import com.chameleonvision.common.vision.opencv.ContourShape;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Objects;

@JsonTypeName("ColoredShapePipelineSettings")
public class ColouredShapePipelineSettings extends AdvancedPipelineSettings {
    public ContourShape desiredShape = ContourShape.Triangle;
    public double minArea = Integer.MIN_VALUE;
    public double maxArea = Integer.MAX_VALUE;
    public double minPeri = Integer.MIN_VALUE;
    public double maxPeri = Integer.MAX_VALUE;
    public double accuracyPercentage = 10.0;
    // Circle detection
    public int allowableThreshold = 5;
    public int minRadius = 0;
    public int maxRadius = 0;
    public int minDist = 10;
    public int maxCannyThresh = 90;
    public int accuracy = 20;
    // how many contours to attempt to group (Single, Dual)
    public ContourGroupingMode contourGroupingMode = ContourGroupingMode.Single;

    // the direction in which contours must intersect to be considered intersecting
    public ContourIntersectionDirection contourIntersection = ContourIntersectionDirection.Up;

    public ColoredShapePipelineSettings() {
        super();
        pipelineType = PipelineType.ColoredShape;
    }
}
