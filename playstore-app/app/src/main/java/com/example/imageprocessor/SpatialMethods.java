package com.example.imageprocessor;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_16S;
import static org.opencv.core.CvType.CV_16SC1;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_8UC;
import static org.opencv.core.CvType.CV_8UC1;

public class SpatialMethods {
    static { OpenCVLoader.initDebug(); }

    static Mat boxFilter (Mat srcImage, final int width, final int height) {
        Mat dstImage = new Mat();
        Imgproc.boxFilter(srcImage, dstImage, -1, new Size(width, height));
        return dstImage;
    }

    static  Mat GaussianBlur (Mat srcImage, int kernel_width, int kernel_height, double sigma) {
        // MANEJAR EXPECION: EL KERNEL DEBE SER POSITIVO E IMPAR
        Mat dstImage = new Mat();
        Imgproc.GaussianBlur(srcImage, dstImage, new Size(kernel_width, kernel_height), sigma);
        return dstImage;
    }

    static Mat spatialLaplacian (Mat srcImage, int kernelSize) {

        // MANEJAR EXPECION: EL KERNEL DEBE SER POSITIVO E IMPAR
        // MANEJAR EXPECION: KERNEL SIZE CAN BE 1, 3, 5, 7 O FILTER_SCHARR
        Mat dstImage = new Mat();
        Mat grayImage = new Mat();

        if (srcImage.channels() == 4)
            Imgproc.cvtColor(srcImage, grayImage, Imgproc.COLOR_RGBA2GRAY);
        else if (srcImage.channels() == 3)
            Imgproc.cvtColor(srcImage, grayImage, Imgproc.COLOR_RGB2GRAY);
        else
            srcImage.copyTo(grayImage);

        grayImage.convertTo(dstImage, CV_32F);

//        Mat kernel = new Mat(3, 3, CV_32F) {
//            {
//                put(0,0,1); put(0,1,1); put(0,2,1);
//                put(1,0,1);  put(1,1,-8);  put(1,2,1);
//                put(2,0,1);  put(2,1,1);  put(2,2,1);
//            }
//        };
//        Imgproc.filter2D(dstImage, dstImage, -1, kernel);

        Imgproc.Laplacian(dstImage, dstImage, -1, kernelSize);

//        Core.normalize(dstImage, dstImage, 0, 255, Core.NORM_MINMAX);
        dstImage.convertTo(dstImage, CV_8UC1);
        Core.subtract(grayImage, dstImage, dstImage);

        return dstImage;
    }

    static Mat firstDerivative (Mat srcImage, int kernelType) {
        Mat grayImage = new Mat();
        Mat dstImage = new Mat();
        Mat kernelX = null;
        Mat kernelY = null;

        srcImage.convertTo(grayImage, CV_16SC1);

        switch (kernelType) {
            case 0: // Sobel
                Mat SobelX = new Mat();
                Mat SobelY = new Mat();
                Imgproc.Sobel(grayImage, SobelX, CV_16SC1, 1, 0, 3);
                Imgproc.Sobel(grayImage, SobelY, CV_16SC1, 0, 1, 3);
                Core.convertScaleAbs(SobelX, SobelX);
                Core.convertScaleAbs(SobelY, SobelY);
                Core.add(SobelX, SobelY, dstImage);
                break;
            case 1: // Sobel X
                Imgproc.Sobel(grayImage, dstImage, CV_16SC1, 0, 1, 3);
                Core.convertScaleAbs(dstImage, dstImage);
                break;
            case 2: // Sobel Y
                Imgproc.Sobel(grayImage, dstImage, CV_16SC1, 1, 0, 3);
                Core.convertScaleAbs(dstImage, dstImage);
                break;
            case 3: // Prewitt
                Mat PrewittX = new Mat();
                Mat PrewittY = new Mat();
                kernelX = new Mat(3, 3, CV_16SC1) {
                    {
                        put(0,0,-1); put(0,1,-1); put(0,2,-1);
                        put(1,0,0);  put(1,1,0);  put(1,2,0);
                        put(2,0,1);  put(2,1,1);  put(2,2,1);
                    }
                };

                kernelY = new Mat(3, 3, CV_16SC1) {
                    {
                        put(0,0,-1);  put(0,1,0);  put(0,2,1);
                        put(1,0,-1);  put(1,1,0);  put(1,2,1);
                        put(2,0,-1);  put(2,1,0);  put(2,2,1);
                    }
                };

                Imgproc.filter2D(grayImage, PrewittX, -1, kernelX);
                Imgproc.filter2D(grayImage, PrewittY, -1, kernelY);
                Core.convertScaleAbs(PrewittX, PrewittX);
                Core.convertScaleAbs(PrewittY, PrewittY);
                Core.add(PrewittX, PrewittY, dstImage);
                break;
            case 4: // Prewitt X
                kernelX = new Mat(3, 3, CV_16SC1) {
                    {
                        put(0,0,1); put(0,1,1); put(0,2,1);
                        put(1,0,0);  put(1,1,0);  put(1,2,0);
                        put(2,0,-1);  put(2,1,-1);  put(2,2,-1);
                    }
                };
                Imgproc.filter2D(grayImage, dstImage, -1, kernelX);
                Core.convertScaleAbs(dstImage, dstImage);
                break;
            case 5: // Prewitt Y
                kernelY = new Mat(3, 3, CV_16SC1) {
                    {
                        put(0,0,-1);  put(0,1,0);  put(0,2,1);
                        put(1,0,-1);  put(1,1,0);  put(1,2,1);
                        put(2,0,-1);  put(2,1,0);  put(2,2,1);
                    }
                };

                Imgproc.filter2D(grayImage, dstImage, -1, kernelY);
                Core.convertScaleAbs(dstImage, dstImage);
                break;
            case 6: // Roberts
                Mat RobertsX = new Mat();
                Mat RobertsY = new Mat();
                kernelX = new Mat(3, 3, CV_16SC1) {
                    {
                        put(0,0,0); put(0,1,0); put(0,2,0);
                        put(1,0,0);  put(1,1,-1);  put(1,2,0);
                        put(2,0,0);  put(2,1,0);  put(2,2,1);
                    }
                };

                kernelY = new Mat(3, 3, CV_16SC1) {
                    {
                        put(0,0,0);  put(0,1,0);  put(0,2,0);
                        put(1,0,0);  put(1,1,0);  put(1,2,-1);
                        put(2,0,0);  put(2,1,1);  put(2,2,0);
                    }
                };
                Imgproc.filter2D(grayImage, RobertsX, -1, kernelX);
                Imgproc.filter2D(grayImage, RobertsY, -1, kernelY);
                Core.convertScaleAbs(RobertsX, RobertsX);
                Core.convertScaleAbs(RobertsY, RobertsY);
                Core.add(RobertsX, RobertsY, dstImage);
                break;
            default:
                break;
        }
        dstImage.convertTo(dstImage, CV_8UC1); // se puede omitir, convertScaleAbs ya transforma y la suma de dos 8-bits sigue siendo 8-bits
        return dstImage;
    }
}
