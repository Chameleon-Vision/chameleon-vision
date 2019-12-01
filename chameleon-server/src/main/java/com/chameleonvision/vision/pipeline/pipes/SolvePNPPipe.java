package com.chameleonvision.vision.pipeline.pipes;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.RotatedRect;

import java.util.List;

public class SolvePNPPipe implements Pipe<List<RotatedRect>, List<Pose2d>> {

    

    @Override
    public Pair<List<Pose2d>, Long> run(List<RotatedRect> input) {
        return null;
    }
}
