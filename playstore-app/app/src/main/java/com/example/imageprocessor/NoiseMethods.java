package com.example.imageprocessor;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.CV_16UC;
import static org.opencv.core.CvType.CV_32FC;
import static org.opencv.core.CvType.CV_8UC;

public class NoiseMethods {
    static { OpenCVLoader.initDebug(); }

    static Mat geometricMeanFilter (Mat srcImage, int width, int height) {
        Mat dstImage = BasicMethods.log(srcImage);
        Imgproc.boxFilter(dstImage, dstImage, -1, new Size(width, height));
        dstImage = BasicMethods.inverseLog(dstImage);
        return dstImage;
    }

    static Mat contraharmonicMeanFilter (Mat srcImage, double Q, int width, int height) {
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_RGBA2RGB);
        Mat dstImage = new Mat();
        Mat num = new Mat();
        Mat den = new Mat();
        Scalar ones = (srcImage.channels() == 3) ? new Scalar(0.00000001, 0.00000001, 0.00000001) : new Scalar(0.00000001);

        srcImage.convertTo(dstImage, CV_32FC(srcImage.channels()));
        Core.add(dstImage, ones, dstImage);
        Core.pow(dstImage, Q + 1, num);
        Core.pow(dstImage, Q, den);
        Imgproc.boxFilter(num, num, -1, new Size(width, height));
        Imgproc.boxFilter(den, den, -1, new Size(width, height));
        Core.divide(num, den, dstImage);

        dstImage.convertTo(dstImage, CV_8UC(srcImage.channels()));
        return dstImage;
    }

    static Mat medianFilter (Mat srcImage, final int kernelSize) {
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_RGBA2RGB);
        Mat dstImage = new Mat();
        Imgproc.medianBlur(srcImage, dstImage, kernelSize);
        return dstImage;
    }

    static Mat minFilter (Mat srcImage, final int width, final int height) {
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_RGBA2RGB);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(width, height));
        Mat dstImage = new Mat();
        Imgproc.erode(srcImage, dstImage, kernel);
        return dstImage;
    }

    static Mat maxFilter (Mat srcImage, final int width, final int height) {
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_RGBA2RGB);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(width, height));
        Mat dstImage = new Mat();
        Imgproc.dilate(srcImage, dstImage, kernel);
        return dstImage;
    }

    static Mat midpointFilter (Mat srcImage, final int width, final int height) {
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_BGRA2BGR);

        Scalar scalar = (srcImage.channels() == 3) ? new Scalar(2, 2, 2) : new Scalar(2);

        Mat minImage = minFilter(srcImage, width, height);
        minImage.convertTo(minImage, CV_16UC(srcImage.channels()));

        Mat dstImage = maxFilter(srcImage, width, height);
        dstImage.convertTo(dstImage, CV_16UC(srcImage.channels()));

        Core.add(dstImage, minImage, dstImage);
        Core.divide(dstImage, scalar, dstImage);
        dstImage.convertTo(dstImage, CV_8UC(srcImage.channels()));

        return dstImage;
    }
}
