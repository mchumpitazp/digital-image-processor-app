package com.example.imageprocessor;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_8UC1;

public class FrequencyMethods {
    static { OpenCVLoader.initDebug(); }

    // =========================   F R E Q    I D E A L    F I L T E R   ===========================
    static Mat freqIdealFilter (final boolean isLowpass, Mat srcImage, final int D_0) {
        int padRows = Core.getOptimalDFTSize(2 * srcImage.rows());
        int padCols = Core.getOptimalDFTSize(2 * srcImage.cols());

        Mat idealFilter = new Mat();
        Bitmap idealFilterBitmap = Bitmap.createBitmap(padCols, padRows, Bitmap.Config.ARGB_8888);

        final int filterChanger = isLowpass ? 0 : 255;
        double D;

        int pixels = padCols * padRows;
        int[] pixelsArray = new int[pixels];
        for (int i = 0; i < pixels; i++) {
            int row = (int) Math.floor((double) i / padCols );
            int col = i - (row * padCols);
            D = Math.sqrt( Math.pow((float)(row - padRows / 2), 2) + Math.pow((float)(col - padCols / 2), 2) );

            if (D <= D_0) {
                pixelsArray[i] = (filterChanger > 0) ? 0xFF000000 : 0xFFFFFFFF;
            }
            else {
                pixelsArray[i] = (filterChanger > 0) ? 0xFFFFFFFF : 0xFF000000;
            }
        }
        idealFilterBitmap.setPixels(pixelsArray, 0, padCols, 0, 0, padCols, padRows);

        Utils.bitmapToMat(idealFilterBitmap, idealFilter);
        Imgproc.cvtColor(idealFilter, idealFilter, Imgproc.COLOR_RGBA2GRAY);
        idealFilter.convertTo(idealFilter, CV_32FC1);
        return idealFilter;
    }

    // ====================   F R E Q    B U T T E R W O R T H    F I L T E R  =====================
    static Mat freqButterworthFilter (final boolean isLowpass, Mat srcImage, final int D_0, final int n) {
        int padRows = Core.getOptimalDFTSize(2 * srcImage.rows());
        int padCols = Core.getOptimalDFTSize(2 * srcImage.cols());

        Mat butterworthFilter = new Mat();
        Bitmap butterworthFilterBitmap = Bitmap.createBitmap(padCols, padRows, Bitmap.Config.ARGB_8888);
        double D;
        double filterValue;
        int hexValue;

        int pixels = padCols * padRows;
        int[] pixelsArray = new int[pixels];

        for (int i = 0; i < pixels; i++) {
            int row = (int) Math.floor((double) i / padCols );
            int col = i - (row * padCols);

            D = Math.sqrt( Math.pow((float)(row - padRows / 2), 2) + Math.pow((float)(col - padCols / 2), 2) );

            D = Math.sqrt( Math.pow((float)(row - padRows / 2), 2) + Math.pow((float)(col - padCols / 2), 2) );

            filterValue = 1.0 / ( 1 + Math.pow(D / D_0, 2 * n) );
            if (!isLowpass)
                filterValue = 1 - filterValue;

            filterValue = Math.round(filterValue * 255);

            hexValue = (int)(filterValue / 16 );
            pixelsArray[i] = 0xFF000000 | (0x101010 * hexValue) | (0x010101 * ((int)filterValue - (hexValue * 16)));
        }
        butterworthFilterBitmap.setPixels(pixelsArray, 0, padCols, 0, 0, padCols, padRows);

        Utils.bitmapToMat(butterworthFilterBitmap, butterworthFilter);
        Imgproc.cvtColor(butterworthFilter, butterworthFilter, Imgproc.COLOR_BGRA2GRAY);
        butterworthFilter.convertTo(butterworthFilter, CV_32FC1);
        return butterworthFilter;
    }

    // ======================   F R E Q     G A U S S I A N    F I L T E R   =======================
    static Mat freqGaussianFilter (final boolean isLowpass, Mat srcImage, final int D_0) {
        int padRows = Core.getOptimalDFTSize(2 * srcImage.rows());
        int padCols = Core.getOptimalDFTSize(2 * srcImage.cols());

        Mat gaussianFilter = new Mat(padRows, padCols, CV_32F);
        Bitmap gaussianFilterBitmap = Bitmap.createBitmap(padCols, padRows, Bitmap.Config.ARGB_8888);

        final int constDenominator = 2 * D_0 * D_0;
        double D;
        double filterValue;
        int hexValue;

        int pixels = padCols * padRows;
        int[] pixelsArray = new int[pixels];

//        ByteBuffer buffer = ByteBuffer.allocate(pixels*4);
//        byte[] bufferArray = buffer.array();
        for (int i = 0; i < pixels; i++) {
            int row = (int) Math.floor((double) i / padCols );
            int col = i - (row * padCols);

            D = Math.sqrt( Math.pow((float)(row - padRows / 2), 2) + Math.pow((float)(col - padCols / 2), 2) );

            filterValue = Math.exp( - (D * D) / constDenominator );
            if (!isLowpass)
                filterValue = 1 - filterValue;


            filterValue = Math.round(filterValue * 255);

            hexValue = (int)(filterValue / 16 );
            pixelsArray[i] = 0xFF000000 | (0x101010 * hexValue) | (0x010101 * ((int)filterValue - (hexValue * 16)));
        }
        gaussianFilterBitmap.setPixels(pixelsArray, 0, padCols, 0, 0, padCols, padRows);

        Utils.bitmapToMat(gaussianFilterBitmap, gaussianFilter);
        Imgproc.cvtColor(gaussianFilter, gaussianFilter, Imgproc.COLOR_RGB2GRAY);
        gaussianFilter.convertTo(gaussianFilter, CV_32F);

//        for (int i = 0; i < pixels; i++) {
//            int row = (int) Math.floor((double) i / padCols );
//            int col = i - (row * padCols);
//
//            D = Math.sqrt( Math.pow((float)(row - padRows / 2), 2) + Math.pow((float)(col - padCols / 2), 2) );
//
//            filterValue = Math.exp( - (D * D) / constDenominator );
//            if (!isLowpass)
//                filterValue = 1 - filterValue;
//
//            filterValue = Math.round(filterValue * 255);
//
//            hexValue = (int)(filterValue / 16 );
//            pixelsArray[i] = 0xFF000000 | (0x101010 * hexValue) | (0x010101 * ((int)filterValue - (hexValue * 16)));
//        }
//        gaussianFilterBitmap.setPixels(pixelsArray, 0, padCols, 0, 0, padCols, padRows);
//
//        Utils.bitmapToMat(gaussianFilterBitmap, gaussianFilter);
//        Imgproc.cvtColor(gaussianFilter, gaussianFilter, Imgproc.COLOR_BGRA2GRAY);
//        gaussianFilter.convertTo(gaussianFilter, CV_32FC1);
//
//        for (int i = 0; i < (pixels * 4); i+=4) {
//            int row = (int) Math.floor((double) (i/4) / padCols );
//            int col = (i/4) - (row * padCols);
//
//            D = Math.sqrt( Math.pow((float)(row - padRows / 2), 2) + Math.pow((float)(col - padCols / 2), 2) );
//
//            filterValue = Math.exp( - (D * D) / constDenominator );
//            if (!isLowpass)
//                filterValue = 1 - filterValue;
//
//            filterValue = Math.round(filterValue * 255);
//
//            hexValue = (int)(filterValue / 16 );
//            bufferArray[i] =  (byte)( 0xFF000000 | (0x101010 * hexValue) | (0x010101 * ((int)filterValue - (hexValue * 16))) );
//            bufferArray[i+1] = bufferArray[i];
//            bufferArray[i+2] = bufferArray[i];
//        }
//
//        ByteBuffer returnBuffer = ByteBuffer.wrap(bufferArray);
//        gaussianFilterBitmap.copyPixelsFromBuffer(returnBuffer);
//
//        Utils.bitmapToMat(gaussianFilterBitmap, gaussianFilter);
//        Imgproc.cvtColor(gaussianFilter, gaussianFilter, Imgproc.COLOR_BGRA2GRAY);
//        gaussianFilter.convertTo(gaussianFilter, CV_32FC1);
        return gaussianFilter;
    }

    // ============================   F R E Q      L A P L A C I A N   =============================
    static Mat freqLaplacianFilter (Mat srcImage) {
        int padRows = Core.getOptimalDFTSize(2 * srcImage.rows());
        int padCols = Core.getOptimalDFTSize(2 * srcImage.cols());
        Size padSize = new Size(padCols, padRows);

        Mat laplacianFilter = new Mat(padSize, CV_32F);
        Bitmap laplacianFilterBitmap = Bitmap.createBitmap(padCols, padRows, Bitmap.Config.ARGB_8888);

        double filterValue;
        double D;

//        int pixels = padCols * padRows;
//        int[] pixelsArray = new int[pixels];
//
//        double max = 0;
//        double min = 255;
//
//        for (int i = 0; i < pixels; i++) {
//            int row = (int) Math.floor((double) i / padCols );
//            int col = i - (row * padCols);
//            D = Math.sqrt( Math.pow((float)(row - padRows / 2), 2) + Math.pow((float)(col - padCols / 2), 2) );
//
//            filterValue = 1 + 4 * Math.pow(Math.PI, 2) * Math.pow(D, 2);
//
//            if (filterValue > max)
//                max = filterValue;
//            if (filterValue < min)
//                min = filterValue;
//        }

        for (int row = 0; row < padSize.height; row++) {
            for (int col = 0; col < padSize.width; col++) {
                D = Math.sqrt( Math.pow((float)(row - padSize.height / 2), 2) + Math.pow((float)(col - padSize.width / 2), 2) );

                filterValue = 1 + 4 * Math.pow(Math.PI, 2) * Math.pow(D, 2);
                laplacianFilter.put(row, col, filterValue);
            }
        }

//        Log.d(GalleryActivity.TAG, "MAX AND MIN OF LAPLACIAN FILTER:" + max + " " + min);

        return laplacianFilter;
    }

    // =============================   H E L P      F U N C T I O N S  =============================

    static Mat getSpectrum (Mat complexDFTImage) {
        Mat magnitudeDFTImage = new Mat();
        List<Mat> planes = new ArrayList<>();

        Core.split(complexDFTImage, planes);
        Core.magnitude( planes.get(0), planes.get(1), magnitudeDFTImage );
        Core.add( Mat.ones(magnitudeDFTImage.size(), CV_32F), magnitudeDFTImage, magnitudeDFTImage);
        Core.log( magnitudeDFTImage, magnitudeDFTImage);

        return magnitudeDFTImage;
    }

    static  Mat padImage (Mat image) {
        Mat paddedImage = new Mat();
        int addPixelRows = Core.getOptimalDFTSize( 2 * image.rows() );
        int addPixelCols = Core.getOptimalDFTSize( 2 * image.cols() );
        Core.copyMakeBorder(image, paddedImage, 0, addPixelRows - image.rows(), 0,addPixelCols - image.cols(), Core.BORDER_CONSTANT, Scalar.all(0));
        return paddedImage;
    }

    static  Mat getDFT (Mat srcImage) {
        Mat paddedImage;
        Mat dftImage  = new Mat();
        List<Mat> planes = new ArrayList<>();

        paddedImage = padImage(srcImage);
        paddedImage.convertTo(paddedImage, CvType.CV_32FC1);
        planes.add(paddedImage);
        planes.add(Mat.zeros(paddedImage.size(), CvType.CV_32FC1));
        Core.merge(planes, dftImage);
        Core.dft(dftImage, dftImage);

        return dftImage;
    }

    static private void shiftQuadrants (Mat image) {
        image = image.submat(new Rect(0, 0, image.cols() & -2, image.rows() & -2));
        int cx = image.cols() / 2;
        int cy = image.rows() / 2;

        Mat q0 = new Mat(image, new Rect(0, 0, cx, cy));
        Mat q1 = new Mat(image, new Rect(cx, 0, cx, cy));
        Mat q2 = new Mat(image, new Rect(0, cy, cx, cy));
        Mat q3 = new Mat(image, new Rect(cx, cy, cx, cy));

        Mat tmp = new Mat();
        q0.copyTo(tmp);
        q3.copyTo(q0);
        tmp.copyTo(q3);

        q1.copyTo(tmp);
        q2.copyTo(q1);
        tmp.copyTo(q2);
    }

    static Mat dftSpectrum (Mat spatialImage) {
        List<Mat> allSamePlanes = new ArrayList<>();
        Core.split(spatialImage, allSamePlanes);
        spatialImage = allSamePlanes.get(0);

        Mat freqImage     = getDFT (spatialImage);
        Mat spectrumImage = getSpectrum (freqImage);
        shiftQuadrants (spectrumImage);

//        double max = 0.0;
//        double min = 255.0;
//        for (int i = 0; i < spectrumImage.rows(); i ++) {
//            for (int j = 0; j < spectrumImage.cols(); j++) {
//                if (spectrumImage.get(i,j)[0] > max)
//                    max = spectrumImage.get(i,j)[0];
//                if (spectrumImage.get(i,j)[0] < min)
//                    min = spectrumImage.get(i,j)[0];
//            }
//        }
//        Log.d(GalleryActivity.TAG, "MAX AND MIN : " + max + " and " + min);

        Core.normalize(spectrumImage, spectrumImage, 0, 255, Core.NORM_MINMAX, CV_8UC1);
        return spectrumImage;
    }

    // ==============================   C O N V O L V E     D F T    ===============================
    static Mat convolveDFT (Mat sourceImage, Mat filter) {
        Mat dftImage;
        Mat realPlane = new Mat();
        Mat imaginaryPlane = new Mat();
        Mat filteredImage = new Mat();
        List<Mat> planes = new ArrayList<>();

        // Make sure that source image is grayscale
        Mat srcImage = new Mat();
        List<Mat> allSamePlanes = new ArrayList<>();
        Core.split(sourceImage, allSamePlanes);
        srcImage = allSamePlanes.get(0);

//        if (sourceImage.channels() == 4)
//            Imgproc.cvtColor(sourceImage, srcImage, Imgproc.COLOR_RGBA2GRAY);
//        else if (sourceImage.channels() == 3)
//            Imgproc.cvtColor(sourceImage, srcImage, Imgproc.COLOR_RGB2GRAY);
//        else
//            sourceImage.copyTo(srcImage);

        // Split DFT image into real and imaginary planes
        dftImage = getDFT(srcImage);
        shiftQuadrants(dftImage);
        Core.split(dftImage, planes);

//        Log.d(TAG, "TYPE DFT : " + typeToString(dftImage.type()));
//        Log.d(TAG, "TYPE PLANE 0 : " + typeToString(planes.get(0).type()));
//        Log.d(TAG, "TYPE PLANE 1 : " + typeToString(planes.get(1).type()));
//
//        Log.d(TAG, "SIZE PLANE 0 : " + planes.get(0).cols() + " " + planes.get(0).rows());
//        Log.d(TAG, "SIZE PLANE 1 : " + planes.get(1).cols() + " " + planes.get(1).rows());
//        Log.d(TAG, "SIZE FILTER : " + filter.cols() + " " + filter.rows());

        // Apply frequency filter to each plane
        Core.multiply(planes.get(0), filter, realPlane);
        Core.multiply(planes.get(1), filter, imaginaryPlane);
        planes.clear();
        planes.add(realPlane);
        planes.add(imaginaryPlane);
        Core.merge(planes, filteredImage);

        Core.idft(filteredImage, filteredImage);
        planes.clear();
        Core.split(filteredImage, planes);
        Core.magnitude(planes.get(0), planes.get(1), filteredImage);
        filteredImage = filteredImage.submat(new Rect(0, 0, srcImage.cols(), srcImage.rows()));
        Core.normalize(filteredImage, filteredImage, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);

        return filteredImage;
    }



}
