package com.example.imageprocessor;

import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

import static org.opencv.core.CvType.CV_32FC;
import static org.opencv.core.CvType.CV_64FC;
import static org.opencv.core.CvType.CV_8UC;

public class BasicMethods {
    static { OpenCVLoader.initDebug(); }

    static private int byte2UnsignedInt(byte number) {
        int newNumber = (int) number;
        if (newNumber < 0)
            newNumber = 256 + newNumber; // 256 + (-)number = 256 - number;
        return newNumber;
    }

    static Mat negate (Mat srcImage) {
        Mat dstImage = new Mat();
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_BGRA2BGR);

        Core.bitwise_not(srcImage, dstImage);

        return dstImage;
    }

    static Mat log (Mat srcImage) {
        Mat dstImage = new Mat();
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_BGRA2BGR);
        final int channels = srcImage.channels();
        final double c = 255 / Math.log(256.0);
        final Scalar ones = (channels == 3) ? new Scalar(1.0, 1.0, 1.0) : new Scalar(1.0);
        final Scalar cs = (channels == 3) ? new Scalar(c, c, c) : new Scalar(c);

        srcImage.convertTo(dstImage, CV_32FC( channels));
        Core.add(dstImage, ones, dstImage);
        Core.log(dstImage, dstImage);
        Core.multiply(dstImage, cs, dstImage);

        dstImage.convertTo(dstImage, CV_8UC(channels));
        return dstImage;
    }

    static Mat inverseLog (Mat srcImage) {
        Mat dstImage = new Mat();
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_BGRA2BGR);
        final int channels = srcImage.channels();
        final double c = Math.log(256.0) / 255;
        final Scalar ones = (channels == 3) ? new Scalar(1, 1, 1) : new Scalar(1);

        srcImage.convertTo(dstImage, CV_64FC(channels));
        Core.exp(dstImage, dstImage);
        Core.pow(dstImage, c, dstImage);
        Core.subtract(dstImage, ones, dstImage);

        dstImage.convertTo(dstImage, CV_8UC(channels));
        return dstImage;
    }

    static Mat powerLaw (Mat srcImage, final double gamma) {
        Mat dstImage = new Mat();
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_BGRA2BGR);
        final int channels = srcImage.channels();
        final Scalar max = (channels == 3) ? new Scalar(255.0, 255.0, 255.0) : new Scalar(255.0);

        srcImage.convertTo(dstImage, CV_32FC(channels));
        Core.divide(dstImage, max, dstImage);
        Core.pow(dstImage, gamma, dstImage);
        Core.multiply(dstImage, max, dstImage);

        dstImage.convertTo(dstImage, CV_8UC(channels));
        return dstImage;
    }

    static Mat contrastStretching (Mat srcImage, int lowX, int lowY, int highX, int highY) {
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_RGBA2RGB);

        final int channels  = srcImage.channels();
        double lowSlope     = (double) lowY / lowX;
        double midSlope     = (double)(highY - lowY) / (double)(highX - lowX);
        double midConstant  = lowY - (midSlope * lowX);
        double highSlope    = (double)(255 - highY) / (double)(255 - highX);
        double highConstant = 255.0 - (highSlope * 255);

        Mat dstImage = new Mat();
        Bitmap dstBitmap = Bitmap.createBitmap(srcImage.cols(), srcImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(srcImage, dstBitmap);
        int size = dstBitmap.getRowBytes() * dstBitmap.getHeight() * 4;

        ByteBuffer buffer = ByteBuffer.allocate(size);
        dstBitmap.copyPixelsToBuffer(buffer);

        byte[] bytesArray = buffer.array();

        for (int i = 0; i < size; i += 4) {
            for (int ch = 0; ch < channels; ch++) {
                double pixel = byte2UnsignedInt( bytesArray[i+ch] );

                if (pixel < lowX)
                    pixel = lowSlope * pixel;
                else if (pixel <= highX)
                    pixel = (midSlope * pixel) + midConstant;
                else
                    pixel = (highSlope * pixel) + highConstant;

                if (channels == 1) {
                    bytesArray[i] = (byte) Math.round(pixel);
                    bytesArray[i+1] = (byte) Math.round(pixel);
                    bytesArray[i+2] = (byte) Math.round(pixel);
                }
                else {
                    bytesArray[i+ch] = (byte) Math.round(pixel);
                }
            }
        }

        ByteBuffer returnBuffer = ByteBuffer.wrap(bytesArray);
        dstBitmap.copyPixelsFromBuffer(returnBuffer);
        Utils.bitmapToMat(dstBitmap, dstImage);

        return dstImage;
    }

    static Mat bitPlaneSlicing (Mat srcImage, int bit) {
//        Log.d(GalleryActivity.TAG, "PIXEL COLORS = " + srcImage.get(0,70)[0]);
//        Log.d(GalleryActivity.TAG, "PIXEL COLORS = " + srcImage.get(0,70)[1]);
//        Log.d(GalleryActivity.TAG, "PIXEL COLORS = " + srcImage.get(0,70)[2]);
//
//
//        List<Mat> planes = new ArrayList<>();
//        Core.split(srcImage, planes);
//
//        Mat zeros = new Mat(srcImage.cols(), srcImage.rows(), CV_8U);
//        planes.set(0, zeros);
//        planes.set(1, zeros);
//        Mat src_GB = new Mat();
//        Core.merge(planes, src_GB);
//
//        Log.d(GalleryActivity.TAG, "PIXEL COLORS = " + src_GB.get(0,70)[0]);
//        Log.d(GalleryActivity.TAG, "PIXEL COLORS = " + src_GB.get(0,70)[1]);
//        Log.d(GalleryActivity.TAG, "PIXEL COLORS = " + src_GB.get(0,70)[2]);

        Mat dstImage = new Mat();
        Mat grayImage = new Mat();

        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, grayImage, Imgproc.COLOR_RGBA2GRAY);
        else if (srcImage.channels() == 3)
            Imgproc.cvtColor(srcImage, grayImage, Imgproc.COLOR_RGB2GRAY);
        else
            srcImage.copyTo(grayImage);

        Bitmap srcBitmap = Bitmap.createBitmap(srcImage.cols(), srcImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(grayImage, srcBitmap);
        Bitmap outputBitmap = srcBitmap.copy(Bitmap.Config.ARGB_8888, true);

        int cols = outputBitmap.getWidth();
        int rows = outputBitmap.getHeight();
        int pixels = cols * rows;

        // Get the original pixels
        int[] pixelsArray = new int[pixels];
        outputBitmap.getPixels(pixelsArray, 0, cols, 0, 0, cols, rows);

        // Get the negative of bitmap using XOR bitwise operation
        int RGB_MASK = 0x00010101 * (int) Math.pow(2, (bit - 1));

        for (int i = 0; i < pixels; i++)
            if ( (pixelsArray[i] & RGB_MASK) != 0)
                pixelsArray[i] = pixelsArray[i] | 0xFFFFFFFF;
            else
                pixelsArray[i] = pixelsArray[i] & 0xFF000000;

        outputBitmap.setPixels(pixelsArray, 0, cols, 0, 0, cols, rows);
        Utils.bitmapToMat(outputBitmap, dstImage);

        return dstImage;
    }

    static Mat equilizeHist (Mat srcImage) {
        // INPUT MAT MUST BE CV_8UC1
        Mat dstImage = new Mat();
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_RGB2GRAY);
        else if (srcImage.channels() == 3)
            Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_RGB2GRAY);
        else
            srcImage.copyTo(dstImage);

        Imgproc.equalizeHist(dstImage, dstImage);
        return dstImage;
    }
}
