package com.chameleonvision.common.vision.pipe.impl;

import com.chameleonvision.common.vision.opencv.CVShape;
import com.chameleonvision.common.vision.opencv.Contour;
import com.chameleonvision.common.vision.pipe.CVPipe;

import java.util.List;

public class FindPolygonPipe extends CVPipe<List<Contour>, CVShape, FindPolygonPipe.FindPolygonPipeParams> {


    /**
     * Runs the process for the pipe.
     *
     * @param in Input for pipe processing.
     * @return Result of processing.
     */
    @Override
    protected CVShape process(List<Contour> in) {
        return null;
    }


    public static class FindPolygonPipeParams {


        public FindPolygonPipeParams(){

        }
    }
}



