package com.chameleonvision.classabstraction.pipeline;

import org.opencv.core.Mat;

import java.util.List;

public abstract class CVPipelineResult<T> {
    public final List<T> targets;
    public final boolean hasTarget;
    public final Mat outputMat = new Mat();
    public final long processTime;

    public CVPipelineResult(List<T> targets, Mat outputMat, long processTime) {
        this.targets = targets;
        hasTarget = targets != null && !targets.isEmpty();
        outputMat.copyTo(this.outputMat);
        this.processTime = processTime;
    }
}