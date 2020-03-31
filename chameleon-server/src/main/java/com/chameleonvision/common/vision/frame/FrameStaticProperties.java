package com.chameleonvision.common.vision.frame;

import edu.wpi.cscore.VideoMode;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.util.FastMath;

public class FrameStaticProperties {
    public final int imageWidth;
    public final int imageHeight;
    public final double fov;
    public final double imageArea;
    public final double centerX;
    public final double centerY;
    public final double horizontalFocalLength;
    public final double verticalFocalLength;

    public FrameStaticProperties(VideoMode mode, double fov) {
        this(mode.width, mode.height, fov);
    }

    public FrameStaticProperties(int imageWidth, int imageHeight, double fov) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.fov = fov;
        imageArea = this.imageWidth * this.imageHeight;
        centerX = ((double) this.imageWidth / 2) - 0.5;
        centerY = ((double) this.imageHeight / 2) - 0.5;

        // pinhole model calculations
        double diagonalView = FastMath.toRadians(this.fov);
        Fraction aspectFraction = new Fraction(this.imageWidth, this.imageHeight);
        int horizontalRatio = aspectFraction.getNumerator();
        int verticalRatio = aspectFraction.getDenominator();
        double diagonalAspect = FastMath.hypot(horizontalRatio, verticalRatio);
        double horizontalView =
                FastMath.atan(FastMath.tan(diagonalView / 2) * (horizontalRatio / diagonalAspect)) * 2;
        double verticalView =
                FastMath.atan(FastMath.tan(diagonalView / 2) * (verticalRatio / diagonalAspect)) * 2;
        horizontalFocalLength = this.imageWidth / (2 * FastMath.tan(horizontalView / 2));
        verticalFocalLength = this.imageHeight / (2 * FastMath.tan(verticalView / 2));
    }
}
