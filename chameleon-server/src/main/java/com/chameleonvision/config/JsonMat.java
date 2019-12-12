package com.chameleonvision.config;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class JsonMat {
    public final int rows;
    public final int cols;
    public final int type;
    public final double[] data;

    public JsonMat(int rows, int cols, double[] data) {
        this.rows = rows;
        this.cols = cols;
        this.type = CvType.CV_64FC1;
        this.data = data;
    }

    public Mat toMat() {
        return toMat(this);
    }

    public static JsonMat fromMat(Mat mat) {
        if (mat.type() != CvType.CV_64FC1) return null;

        double[] data = new double[(int)(mat.total()*mat.elemSize())];
        mat.get(0, 0, data);

        double[] trimmedData = Arrays.copyOfRange(data, 0, 5);

        return new JsonMat(mat.rows(), mat.cols(), trimmedData);
    }

    public static Mat toMat(JsonMat jsonMat) {
        if (jsonMat.type != CvType.CV_64FC1) return null;

        Mat retMat = new Mat(jsonMat.rows, jsonMat.cols, jsonMat.type);
        retMat.put(0, 0, jsonMat.data);
        return retMat;
    }
}
