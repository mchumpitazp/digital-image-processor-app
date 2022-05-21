package com.example.imageprocessor;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MorphologyMethods {
    static { OpenCVLoader.initDebug(); }


    // Shapes of structural elements
    static Mat getStrel (final String strelShape, int width, int height) {
        switch (strelShape) {
            case "Rect":    return strelRectangle(width, height);
            case "Cross":   return strelCross(width, height);
            case "Ellipse": return strelEllipse(width, height);
            case "Disk":
            default:        return strelDisk(width); // case "Disk"
        }
    }

    static Mat getStrel (final String strelShape, int radius) {
        return getStrel(strelShape, radius, radius);
    }

    static Mat strelRectangle (final int width, final int height) {
        return Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(width, height));
    }

    static Mat strelCross (final int width, final int height) {
        return  Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(width, height));
    }

    static Mat strelEllipse (final int width, final int height) {
        return  Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(width, height));
    }

    static Mat strelDisk (final int radius) {
        Mat kernel = null;

        switch (radius) {
            case 2:
                kernel = new Mat(5, 5, CvType.CV_8UC1) {
                    {
                        put(0,0,0); put(0,1,0); put(0,2,1); put(0,3,0); put(0,4,0);
                        put(1,0,0); put(1,1,1); put(1,2,1); put(1,3,1); put(1,4,0);
                        put(2,0,1); put(2,1,1); put(2,2,1); put(2,3,1); put(2,4,1);
                        put(3,0,0); put(3,1,1); put(3,2,1); put(3,3,1); put(3,4,0);
                        put(4,0,0); put(4,1,0); put(4,2,1); put(4,3,0); put(4,4,0);
                    } };
                break;
            case 3:
                kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
                break;
            case 4:
                kernel = new Mat(7, 7, CvType.CV_8UC1) {
                    {
                        put(0,0,0); put(0,1,0); put(0,2,1); put(0,3,1); put(0,4,1); put(0, 5, 0); put(0,6,0);
                        put(1,0,0); put(1,1,1); put(1,2,1); put(1,3,1); put(1,4,1); put(1, 5, 1); put(1,6,0);
                        put(2,0,1); put(2,1,1); put(2,2,1); put(2,3,1); put(2,4,1); put(2, 5, 1); put(2,6,1);
                        put(3,0,1); put(3,1,1); put(3,2,1); put(3,3,1); put(3,4,1); put(3, 5, 1); put(3,6,1);
                        put(4,0,1); put(4,1,1); put(4,2,1); put(4,3,1); put(4,4,1); put(4, 5, 1); put(4,6,1);
                        put(5,0,0); put(5,1,1); put(5,2,1); put(5,3,1); put(5,4,1); put(5, 5, 1); put(5,6,0);
                        put(6,0,0); put(6,1,0); put(6,2,1); put(6,3,1); put(6,4,1); put(6, 5, 0); put(6,6,0);
                    } };
                break;
            case 5:
                kernel = new Mat(9, 9, CvType.CV_8UC1);
                // Row 0
                kernel.put(0, 0, 0);
                kernel.put(0, 1, 0);
                for (int j = 2; j < 7; j++)
                    kernel.put(0, j, 1);
                kernel.put(0, 7, 0);
                kernel.put(0, 8, 0);
                // Row 1
                kernel.put(1, 0, 0);
                for (int j = 1; j < 8; j++)
                    kernel.put(1, j, 1);
                kernel.put(1, 8, 0);
                // Row 2 to 6
                for (int i = 2; i < 7; i++) {
                    for (int j = 0; j < 9; j++)
                        kernel.put(i, j, 1);
                }
                // Row 7
                kernel.put(7, 0, 0);
                for (int j = 1; j < 8; j++)
                    kernel.put(7, j, 1);
                kernel.put(7, 8, 0);
                // Row 8
                kernel.put(8, 0, 0);
                kernel.put(8, 1, 0);
                for (int j = 2; j < 7; j++)
                    kernel.put(8, j, 1);
                kernel.put(8, 7, 0);
                kernel.put(8, 8, 0);
                break;
            case 1:
            default:
                kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3));
                break;
        }

        return kernel;
    }

    // Morphological operations

    static Mat morphologyMethod (String morphMethodName, Mat srcImage, Mat kernel) {
        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_RGBA2RGB);

        switch (morphMethodName) {
            case "Erosion":   return morphErode(srcImage, kernel);
            case "Dilation":  return morphDilate(srcImage, kernel);
            case "Opening":   return morphOpen(srcImage, kernel);
            case "Closing":   return morphClose(srcImage, kernel);
            case "Smoothing": return morphSmoothing(srcImage, kernel);
            case "Gradient":  return morphGradient(srcImage, kernel);
            default:          return BasicMethods.negate(srcImage);
        }
    }

    static private Mat morphErode (Mat srcImage, Mat kernel) {
        Mat dstImage = new Mat();
        Imgproc.erode(srcImage, dstImage, kernel);
        return dstImage;
    }

    static private Mat morphDilate (Mat srcImage, Mat kernel) {
        Mat dstImage = new Mat();
        Imgproc.dilate(srcImage, dstImage, kernel);
        return dstImage;
    }

    static private Mat morphOpen (Mat srcImage, Mat kernel) {
        Mat dstImage = new Mat();
        Imgproc.morphologyEx(srcImage, dstImage, Imgproc.MORPH_OPEN, kernel);
        return dstImage;
    }

    static private Mat morphClose (Mat srcImage, Mat kernel) {
        Mat dstImage = new Mat();
        Imgproc.morphologyEx(srcImage, dstImage, Imgproc.MORPH_CLOSE, kernel);
        return dstImage;
    }

    static private Mat morphSmoothing (Mat srcImage, Mat kernel) {
        Mat dstImage = new Mat();
        Imgproc.morphologyEx(srcImage, dstImage, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(dstImage, dstImage, Imgproc.MORPH_CLOSE, kernel);
        return dstImage;
    }

    static private Mat morphGradient (Mat srcImage, Mat kernel) {
        // CORREGIR QUE DE NUEVO ESTA BOTAN BLANCO
        // LA IMAGEN SE PASA POR REFERENCIA Y SE RESTA CON ELLA MISMA
        // COMPARAR EL DESEMPEÑO SI NO SALTAR ENTRE ESPACIOS DE MEMORIA
        Mat erodedImage = morphErode(srcImage, kernel);
        Mat dstImage = morphDilate(srcImage, kernel);
        Core.subtract(dstImage, erodedImage, dstImage);
        return dstImage;
    }
}
