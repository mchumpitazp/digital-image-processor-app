package com.example.imageprocessor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ColorMethods {
    static { OpenCVLoader.initDebug(); }

    static int byte2UnsignedInt(byte number) {
        int newNumber = (int) number;

        if (newNumber < 0) {
            newNumber = 256 + newNumber; // 256 + (-)number = 256 - number;
        }
        return newNumber;
    }

    static Mat RGBtoPlanes (Mat srcImage, int plane) { // 0 is red, 1 is green and 2 is blue
        Bitmap srcBitmap = Bitmap.createBitmap(srcImage.cols(), srcImage.rows(), Bitmap.Config.ARGB_8888);
        Bitmap dstBitmap = Bitmap.createBitmap(srcImage.cols(), srcImage.rows(), Bitmap.Config.ARGB_8888);
        Mat dstImage = new Mat();
        Utils.matToBitmap(srcImage, srcBitmap);
        int size = srcImage.cols() * srcImage.rows() * 4;

        ByteBuffer buffer = ByteBuffer.allocate(size);
        srcBitmap.copyPixelsToBuffer(buffer);

        byte[] bytesArray = buffer.array();
        byte[] dstArray   = new byte[bytesArray.length];

        for (int i = 0; i < size; i += 4) {
            int color = byte2UnsignedInt( bytesArray[i + plane] );

            if (plane == 0) {
                dstArray[i]   = (byte) color;
                dstArray[i+1] = (byte) 0;
                dstArray[i+2] = (byte) 0;
            }
            else if (plane == 1) {
                dstArray[i]   = (byte) 0;
                dstArray[i+1] = (byte) color;
                dstArray[i+2] = (byte) 0;
            }
            else {
                dstArray[i]   = (byte) 0;
                dstArray[i+1] = (byte) 0;
                dstArray[i+2] = (byte) color;
            }
            dstArray[i+3] = (byte) 255;
        }

        ByteBuffer returnBuffer = ByteBuffer.wrap(dstArray);
        dstBitmap.copyPixelsFromBuffer(returnBuffer);
        Utils.bitmapToMat(dstBitmap, dstImage);

        List<Mat> planes = new ArrayList<>();
        Core.split(dstImage, planes);
        planes.get(plane).copyTo(dstImage);

        return dstImage;
    }

    static Mat RGBtoHSI (Mat srcImage, int plane) { // 0 is hue, 1 is saturation and 2 is intenisty
        int width  = srcImage.cols();
        int height = srcImage.rows();
        Bitmap srcBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap dstBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Mat dstImage = new Mat();
        Utils.matToBitmap(srcImage, srcBitmap);
        int size = width * height * 4;

        ByteBuffer buffer = ByteBuffer.allocate(size);
        srcBitmap.copyPixelsToBuffer(buffer);

        byte[] bytesArray = buffer.array();
        byte[] dstArray = new byte[bytesArray.length];

        for (int i = 0; i < size; i += 4) {
            double R  = ( (double) byte2UnsignedInt( bytesArray[i] ) ) / 255;
            double G  = ( (double) byte2UnsignedInt( bytesArray[i+1] ) ) / 255;
            double B  = ( (double) byte2UnsignedInt( bytesArray[i+2] ) ) / 255;

            int value;

            switch (plane) {
                case 0:
                    double num = 0.5 * ((R - G) + (R - B));
                    double den = Math.sqrt( Math.pow((R - G), 2) + ((R - B) * (G - B)) );
                    double H = Math.toDegrees( Math.acos(num / (den + 0.000001)) );

                    if (B > G)
                        H = 360 - H;
                    value = (int)Math.round(H / 360 * 255);
                    break;
                case 1:
                    double S = 1 - (3 / (R + G + B + 0.000001) * Math.min(Math.min(R, G), B));
                    value = (int)Math.round(S * 255);
                    break;
                case 2:
                default:
                    double I = (R + G + B) / 3;
                    value = (int)Math.round(I * 255);
            }

            dstArray[i]   = (byte) value;
            dstArray[i+1] = (byte) value;
            dstArray[i+2] = (byte) value;
            dstArray[i+3] = (byte) 255;
        }
        ByteBuffer dstBuffer = ByteBuffer.wrap(dstArray);
        dstBitmap.copyPixelsFromBuffer(dstBuffer);
        Utils.bitmapToMat(dstBitmap, dstImage);

        return dstImage;
    }

    static Mat intensitySlicing (Mat srcImage, int intensity_1, int intensity_2, int intensity_3) {
        // ONLY WORK WITH GRAYSCALE AS INPUT
        Mat dstImage = new Mat();
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_RGBA2GRAY);
        else if (srcImage.channels() == 3)
            Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_RGB2GRAY);
        else
            srcImage.copyTo(dstImage);

        int size = srcImage.cols() * srcImage.rows() * 4;
        Bitmap dstBitmap = Bitmap.createBitmap(srcImage.cols(), srcImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dstImage, dstBitmap);

        ByteBuffer buffer = ByteBuffer.allocate(size);
        dstBitmap.copyPixelsToBuffer(buffer);

        byte[] bytesArray = buffer.array();

        for (int i = 0; i < size; i += 4) {
            int gray = byte2UnsignedInt( bytesArray[i] );

            if (gray == intensity_1){
                bytesArray[i]   = (byte)255;
                bytesArray[i+1] = (byte)255;
                bytesArray[i+2] = (byte)0;
                bytesArray[i+3] = (byte)255;
            }
            else if (gray == intensity_2) {
                bytesArray[i]   = (byte)255;
                bytesArray[i+1] = (byte)0;
                bytesArray[i+2] = (byte)255;
                bytesArray[i+3] = (byte)255;
            }
            else if (gray == intensity_3) {
                bytesArray[i]   = (byte)0;
                bytesArray[i+1] = (byte)255;
                bytesArray[i+2] = (byte)255;
                bytesArray[i+3] = (byte)255;
            }
            else {
                bytesArray[i]   = (byte)0;
                bytesArray[i+1] = (byte)0;
                bytesArray[i+2] = (byte)0;
                bytesArray[i+3] = (byte)255;
            }
        }
        ByteBuffer returnBuffer = ByteBuffer.wrap(bytesArray);
        dstBitmap.copyPixelsFromBuffer(returnBuffer);
        Utils.bitmapToMat(dstBitmap, dstImage);

        return dstImage;
    }

    static double sinFunction(double input, double freq, double shift) {
        return Math.abs(  Math.sin(  (input * freq * Math.PI) + (shift * Math.PI)   )  );
    }

    static Mat intensityToColor (Mat srcImage, double freq, double shiftR, double shiftG, double shiftB) {
        // ONLY WORK WITH GRAYSCALE AS INPUT
        Mat dstImage = new Mat();
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_RGBA2GRAY);
        else if (srcImage.channels() == 3)
            Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_RGB2GRAY);
        else
            srcImage.copyTo(dstImage);

        int size = srcImage.cols() * srcImage.rows() * 4;
        Bitmap dstBitmap = Bitmap.createBitmap(srcImage.cols(), srcImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dstImage, dstBitmap);

        ByteBuffer buffer = ByteBuffer.allocate(size);
        dstBitmap.copyPixelsToBuffer(buffer);

        byte[] bytesArray = buffer.array();
        byte[] dstArray = new byte[bytesArray.length];

        for (int i = 0; i < size; i +=4) {
            double gray  = ( (double) byte2UnsignedInt( bytesArray[i] ) ) / 255;

            double R = sinFunction(gray, freq, shiftR);
            double G = sinFunction(gray, freq, shiftG);
            double B = sinFunction(gray, freq, shiftB);
            dstArray[i]   = (byte) (R * 255);
            dstArray[i+1] = (byte) (G * 255);
            dstArray[i+2] = (byte) (B * 255);
            dstArray[i+3] = (byte) 255;

        }

        ByteBuffer returnBuffer = ByteBuffer.wrap(dstArray);
        dstBitmap.copyPixelsFromBuffer(returnBuffer);
        Utils.bitmapToMat(dstBitmap, dstImage);

        return dstImage;
    }

    static Mat colorBalance (Mat srcImage, float percent) {
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_RGBA2RGB);

        Mat dstImage = new Mat();
        srcImage.convertTo(dstImage, CvType.CV_32F);
        List<Mat> dstPlanes = new ArrayList<>();
        List<Mat> rgbPlanes = new ArrayList<>();
        Core.split(dstImage, rgbPlanes);

        int rows     = srcImage.rows();
        int cols     = srcImage.cols();
        double half_percent = percent / 200.0;

        for (int ch = 0; ch < 3; ch++) {
            Mat flat = new Mat();
            rgbPlanes.get(ch).reshape(1, 1).copyTo(flat);
            Core.sort(flat, flat, Core.SORT_EVERY_ROW + Core.SORT_ASCENDING);
            double lowVal = flat.get(0, (int)Math.floor((float)flat.cols() * half_percent))[0];
            double topVal = flat.get(0, (int)Math.ceil((float)flat.cols() * (1.0 - half_percent)))[0];

            Mat plane = rgbPlanes.get(ch);
            for (int m = 0; m < rows; m++) {
                for (int n = 0; n < cols; n++) {
                    if (plane.get(m, n)[0] < lowVal)
                        plane.put(m, n, lowVal);
                    if (plane.get(m, n)[0] > topVal)
                        plane.put(m, n, topVal);
                }
            }
            Core.normalize(plane, plane, 0, 255, Core.NORM_MINMAX);
            plane.convertTo(plane, CvType.CV_8U);
            dstPlanes.add(plane);
        }
        Core.merge(dstPlanes, dstImage);

        return dstImage;
    }


//    static List<Mat> RGBtoHSIfull (Mat srcImage) {
//        List<Mat> hsiPlanes = new ArrayList<>();
//        int width  = srcImage.cols();
//        int height = srcImage.rows();
//        Bitmap srcBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        Bitmap hueBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // Hue
//        Bitmap satBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // Saturation
//        Bitmap intBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // Intensity
//        Mat hueImage = new Mat();
//        Mat satImage = new Mat();
//        Mat intImage = new Mat();
//        Utils.matToBitmap(srcImage, srcBitmap);
//        int size = width * height * 4;
//
//        ByteBuffer buffer = ByteBuffer.allocate(size);
//        srcBitmap.copyPixelsToBuffer(buffer);
//
//        byte[] bytesArray = buffer.array();
//        byte[] hueArray = new byte[bytesArray.length];
//        byte[] satArray = new byte[bytesArray.length];
//        byte[] intArray = new byte[bytesArray.length];
//
//        for (int i = 0; i < size; i += 4) {
//            double R  = ( (double) byte2UnsignedInt( bytesArray[i] ) ) / 255;
//            double G  = ( (double) byte2UnsignedInt( bytesArray[i+1] ) ) / 255;
//            double B  = ( (double) byte2UnsignedInt( bytesArray[i+2] ) ) / 255;
//
//            double num = 1/2 * ((R - G) + (R - B));
//            double den = Math.sqrt( Math.pow((R - G), 2) + ((R - B) * (G - B)) );
//
//            double H = Math.acos(num / (den + 0.000001));
//            if (B > G)
//                H = 360 - H;
//            double S = 1 - (3 / (R + G + B + 0.000001) * Math.min(Math.min(R, G), B));
//            double I = (R + G + B) / 3;
//
//            int hue = (int)Math.floor(H / 360 * 255);
//            hueArray[i]   = (byte) hue;
//            hueArray[i+1] = (byte) hue;
//            hueArray[i+2] = (byte) hue;
//            hueArray[i+3] = (byte) 255;
//
//            int sat = (int)Math.floor(S * 255);
//            satArray[i]   = (byte) sat;
//            satArray[i+1] = (byte) sat;
//            satArray[i+2] = (byte) sat;
//            satArray[i+3] = (byte) 255;
//
//            int intensity = (int)Math.floor(I * 255);
//            intArray[i]   = (byte) intensity;
//            intArray[i+1] = (byte) intensity;
//            intArray[i+2] = (byte) intensity;
//            intArray[i+3] = (byte) 255;
//        }
//
//        ByteBuffer hueBuffer = ByteBuffer.wrap(hueArray);
//        ByteBuffer satBuffer = ByteBuffer.wrap(satArray);
//        ByteBuffer intBuffer = ByteBuffer.wrap(intArray);
//        hueBitmap.copyPixelsFromBuffer(hueBuffer);
//        satBitmap.copyPixelsFromBuffer(satBuffer);
//        intBitmap.copyPixelsFromBuffer(intBuffer);
//        Utils.bitmapToMat(hueBitmap, hueImage);
//        Utils.bitmapToMat(satBitmap, satImage);
//        Utils.bitmapToMat(intBitmap, intImage);
//        hsiPlanes.add(hueImage);
//        hsiPlanes.add(satImage);
//        hsiPlanes.add(intImage);
//
//        return hsiPlanes;
//    }
}
