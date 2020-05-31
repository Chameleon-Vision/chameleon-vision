package com.chameleonvision.common.vision.pipeline.polygon.detect;

import com.chameleonvision.common.vision.opencv.CVShape;
import com.chameleonvision.common.vision.opencv.Contour;
import com.chameleonvision.common.vision.pipe.CVPipeResult;
import com.chameleonvision.common.vision.pipe.impl.FindPolygonPipe;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class FindPolygonPipeTest {

    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        FindPolygonPipe findPolygonPipe = new FindPolygonPipe();
        Mat srcGray = new Mat();

        String filename = args.length > 0 ? args[0] : "D:\\chameleon-vision\\chameleon-server\\src\\test\\resources\\polygons\\polygons.jpg";
        Mat src = Imgcodecs.imread(filename);
        Imgproc.resize(src, src, new Size(480, 324));
        if (src.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(srcGray, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        List<Contour> input = new ArrayList<>();

        contours.forEach(contour -> input.add(new Contour(contour)));

        CVPipeResult<List<CVShape>> output= findPolygonPipe.apply(input);

        output.result.forEach(corner -> System.out.println(corner.shape.sides));

    }
}
