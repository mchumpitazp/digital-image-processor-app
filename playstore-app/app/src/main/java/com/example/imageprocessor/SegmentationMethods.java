package com.example.imageprocessor;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class SegmentationMethods {
    static { OpenCVLoader.initDebug(); }

    static private double otsuThresholdValue = 0;

    static Mat binaryThreshold (Mat srcImage, int thresh) {
        // INPUT IMAGE SHOULD BE GRAYSCALE
        Mat dstImage = new Mat();
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_RGBA2GRAY);
        else if (srcImage.channels() == 3)
            Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_RGB2GRAY);
        else
            srcImage.copyTo(dstImage);

        Imgproc.threshold(dstImage, dstImage, thresh, 255, Imgproc.THRESH_BINARY);
        return dstImage;
    }

    static Mat otsuThreshold (Mat srcImage) {
        // INPUT IMAGE SHOULD BE GRAYSCALE
        Mat dstImage = new Mat();
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_RGBA2GRAY);
        else if (srcImage.channels() == 3)
            Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_RGB2GRAY);
        else
            srcImage.copyTo(dstImage);

        otsuThresholdValue = Imgproc.threshold(dstImage, dstImage, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        return dstImage;
    }

    static int getOtsuValue () {
        return (int) otsuThresholdValue;
    }

    static Mat houghCircle (Mat srcImage, int minDistFactor, int cannyParam, int minRadius, int maxRadius) {
        Mat dstImage = new Mat();
        Mat grayImage = new Mat();
        Mat circles = new Mat();


        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_BGRA2BGR);

        srcImage.copyTo(dstImage);

        if (srcImage.channels() == 3)
            Imgproc.cvtColor(srcImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        else
            srcImage.copyTo(grayImage);


        Imgproc.medianBlur(grayImage, grayImage, 3);

        Imgproc.HoughCircles(grayImage, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                (double)grayImage.rows()/minDistFactor, cannyParam, 20,
                minRadius, maxRadius);

        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // circle center
            Imgproc.circle(dstImage, center, 1, new Scalar(255, 100, 0), 2, 8, 0);
            //circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(dstImage, center, radius, new Scalar(255, 0, 0), 2, 8, 0);
        }

        return dstImage;
    }

}
