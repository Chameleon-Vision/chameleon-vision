package com.chameleonvision.common.vision.target;

import java.util.ArrayList;
import java.util.List;

import com.chameleonvision.common.vision.opencv.Releasable;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;

public class TargetModel implements Releasable {

    private final MatOfPoint3f realWorldTargetCoordinates;

    private final MatOfPoint3f visualizationBoxBottom = new MatOfPoint3f();
    private final MatOfPoint3f visualizationBoxTop = new MatOfPoint3f();

    public TargetModel(MatOfPoint3f realWorldTargetCoordinates, double boxHeight) {
        this.realWorldTargetCoordinates = realWorldTargetCoordinates;

        var bottomList = realWorldTargetCoordinates.toList();
        var topList = new ArrayList<>(bottomList);
        for (var c : bottomList) {
            topList.add(new Point3(c.x, c.y, c.z + boxHeight));
        }

        this.visualizationBoxBottom.fromList(bottomList);
        this.visualizationBoxTop.fromList(topList);
    }

    public TargetModel(List<Point3> points, double boxHeight) {
        this(listToMat(points), boxHeight);
    }

    private static MatOfPoint3f listToMat(List<Point3> points) {
        var mat = new MatOfPoint3f();
        mat.fromList(points);
        return mat;
    }

    public MatOfPoint3f getRealWorldTargetCoordinates() {
        return realWorldTargetCoordinates;
    }

    public MatOfPoint3f getVisualizationBoxBottom() {
        return visualizationBoxBottom;
    }

    public MatOfPoint3f getVisualizationBoxTop() {
        return visualizationBoxTop;
    }

    public static TargetModel get2020Target() {
        var corners = List.of(new Point3(-19.625, 0, 0),
                new Point3(-9.819867, -17, 0),
                new Point3(9.819867, -17, 0),
                new Point3(19.625, 0, 0));
        return new TargetModel(corners, 6d / 1000d * 25.4);
    }

    @Override
    public void release() {
        realWorldTargetCoordinates.release();
        visualizationBoxBottom.release();
        visualizationBoxTop.release();
    }
}
