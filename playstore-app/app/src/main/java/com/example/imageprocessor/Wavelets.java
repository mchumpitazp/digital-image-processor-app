package com.example.imageprocessor;

import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

public class Wavelets {
    public static final String TAG = "GalleryActivity";
    static { OpenCVLoader.initDebug(); }

    static private Mat haarLevel1Filter (Mat srcImage, boolean isLowpass) {
        Mat grayImage = new Mat();
        srcImage.convertTo(grayImage, CvType.CV_64F);
        Core.divide(grayImage, new Scalar(255.0), grayImage);

        final int rows = (srcImage.rows() % 2 == 1) ? srcImage.rows() - 1 : srcImage.rows();
        final int cols = (srcImage.cols() % 2 == 1) ? srcImage.cols() - 1 : srcImage.cols();
        Mat filter = new Mat(rows, cols/2, CvType.CV_32F);

        // Decomposition
        // Level 1
        float a, b, c;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col += 2) {

                a = (float) grayImage.get(row, col)[0];
                b = (float) grayImage.get(row, col+1)[0];
                if (isLowpass) b = -b;
                c = (float) ( (a-b) * 0.707 );

                filter.put(row, col / 2, c);
            }
        }

        return filter;
    }

    static Mat haarLevel1 (Mat srcImage, boolean isLowpassL1, boolean isLowpassL2) {
        final Mat filterL1 = haarLevel1Filter(srcImage, isLowpassL1);
        final int rows = filterL1.rows();
        final int cols = filterL1.cols();

        Mat compressionL1 = new Mat(rows/2, cols, CvType.CV_32F);

        float a, b, c;
        for (int row = 0; row < rows; row += 2) {
            for (int col = 0; col < cols; col ++) {

                a = (float) filterL1.get(row, col)[0];
                b = (float) filterL1.get(row+1, col)[0];
                if (isLowpassL2) b = -b;
                c = (float) ( (a-b) * 0.707 );

                compressionL1.put(row / 2, col, c);
            }
        }

        Core.normalize(compressionL1, compressionL1, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);
        return compressionL1;
    }

    static private Mat haarLevel2Filter (Mat srcImage, boolean isLowpass) {
        final Mat wA_1 = haarLevel1(srcImage, true, true);
        final int rows = (srcImage.rows() % 2 == 1) ? srcImage.rows() - 1 : srcImage.rows();
        final int cols = (srcImage.cols() % 2 == 1) ? srcImage.cols() - 1 : srcImage.cols();
        final int rows_2 = (rows/2 % 2 == 1) ? rows - 1 : rows;
        final int cols_2 = (cols/2 % 2 == 1) ? cols - 1 : cols;

        Mat filter = new Mat(rows_2/2, cols_2/4, CvType.CV_32F);

        // Level 2
        float a, b, c;
        for (int row = 0; row < rows_2/2; row++) {
            for (int col = 0; col < cols_2/2; col += 2) {

                a = (float) wA_1.get(row, col)[0];
                b = (float) wA_1.get(row, col+1)[0];
                if (isLowpass) b = -b;
                c = (float) ( (a-b) * 0.707 );

                filter.put(row, col / 2, c);
            }
        }

        return filter;
    }

    static Mat haarLevel2 (Mat srcImage, boolean isLowpassL1, boolean isLowpassL2) {
        final Mat filterL2 = haarLevel2Filter(srcImage, isLowpassL1);
        final int rows = filterL2.rows();
        final int cols = filterL2.cols();

        Mat compressionL2 = new Mat(rows/2, cols, CvType.CV_32F);

        float a, b, c;
        for (int row = 0; row < rows; row += 2) {
            for (int col = 0; col < cols; col ++) {

                a = (float) filterL2.get(row, col)[0];
                b = (float) filterL2.get(row+1, col)[0];
                if (isLowpassL2) b = -b;
                c = (float) ( (a-b) * 0.707 );

                compressionL2.put(row / 2, col, c);
            }
        }

        Core.normalize(compressionL2, compressionL2, 0,255, Core.NORM_MINMAX, CvType.CV_8UC1);
        return compressionL2;
    }

    static Mat haarForward (Mat srcImage, int index) {
        switch (index) {
            // Level 1 - Approximation
            case 0: return haarLevel1(srcImage, true, true);

            // Level 1 - Horizontal
            case 1: return haarLevel1(srcImage, true, false);

            // Level 1 - Vertical
            case 2: return haarLevel1(srcImage, false, true);

            // Level 1 - Diagonal
            case 3: return haarLevel1(srcImage, false, false);

            // Level 2 - Approximation
            case 4: return haarLevel2(srcImage, true, true);

            // Level 2 - Horizontal
            case 5: return haarLevel2(srcImage, true, false);

            // Level 2 - Vertical
            case 6: return haarLevel2(srcImage, false, true);

            // Level 2 - Diagonal
            case 7: return haarLevel2(srcImage, false, false);

            // Full Haar Transform
            default: return haarForwardFull(srcImage);
        }
    }

    static Mat haarForwardFull (Mat srcImage) {

        Mat dstImage = new Mat(srcImage.rows(), srcImage.cols(), CvType.CV_8UC1);
        Mat grayImage = new Mat();

        srcImage.convertTo(grayImage, CvType.CV_64F);
        Core.divide(grayImage, new Scalar(255.0), grayImage);

        final int rows = (srcImage.rows() % 2 == 1) ? srcImage.rows() - 1 : srcImage.rows();
        final int cols = (srcImage.cols() % 2 == 1) ? srcImage.cols() - 1 : srcImage.cols();
        Mat wLP_1 = new Mat(rows, cols/2, CvType.CV_32F);
        Mat wHP_1 = new Mat(rows, cols/2, CvType.CV_32F);
        Mat wA_1  = new Mat(rows/2, cols/2, CvType.CV_32F);
        Mat wH_1  = new Mat(rows/2, cols/2, CvType.CV_32F);
        Mat wV_1  = new Mat(rows/2, cols/2, CvType.CV_32F);
        Mat wD_1  = new Mat(rows/2, cols/2, CvType.CV_32F);

        final int rows_2 = (rows/2 % 2 == 1) ? rows - 1 : rows;
        final int cols_2 = (cols/2 % 2 == 1) ? cols - 1 : cols;
        Mat wLP_2 = new Mat(rows_2/2, cols_2/4, CvType.CV_32F);
        Mat wHP_2 = new Mat(rows_2/2, cols_2/4, CvType.CV_32F);
        Mat wA_2  = new Mat(rows_2/4, cols_2/4, CvType.CV_32F);
        Mat wH_2  = new Mat(rows_2/4, cols_2/4, CvType.CV_32F);
        Mat wV_2  = new Mat(rows_2/4, cols_2/4, CvType.CV_32F);
        Mat wD_2  = new Mat(rows_2/4, cols_2/4, CvType.CV_32F);

        // Decomposition
        // Level 1
        float a, b, c, d;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col += 2) {

                a = (float) grayImage.get(row, col)[0];
                b = (float) grayImage.get(row, col+1)[0];
                c = (float) ( (a+b) * 0.707 );
                d = (float) ( (a-b) * 0.707 );

                int _col = col / 2;
                wLP_1.put(row, _col, c);
                wHP_1.put(row, _col, d);
            }
        }

        for (int row = 0; row < rows; row += 2) {
            for (int col = 0; col < cols / 2; col ++) {

                a = (float) wLP_1.get(row, col)[0];
                b = (float) wLP_1.get(row+1, col)[0];
                c = (float) ( (a+b) * 0.707 );
                d = (float) ( (a-b) * 0.707 );

                int _row = row / 2;
                wA_1.put(_row, col, c);
                wH_1.put(_row, col, d);
            }
        }

        for (int row = 0; row < rows; row += 2) {
            for (int col = 0; col < cols / 2; col ++) {

                a = (float) wHP_1.get(row, col)[0];
                b = (float) wHP_1.get(row+1, col)[0];
                c = (float) ( (a+b) * 0.707 );
                d = (float) ( (a-b) * 0.707 );

                int _row = row / 2;
                wV_1.put(_row, col, c);
                wD_1.put(_row, col, d);
            }
        }

        // Level 2
        for (int row = 0; row < rows_2/2; row++) {
            for (int col = 0; col < cols_2/2; col += 2) {

                a = (float) wA_1.get(row, col)[0];
                b = (float) wA_1.get(row, col+1)[0];
                c = (float) ( (a+b) * 0.707 );
                d = (float) ( (a-b) * 0.707 );

                int _col = col / 2;
                wLP_2.put(row, _col, c);
                wHP_2.put(row, _col, d);
            }
        }

        for (int row = 0; row < rows_2/2; row += 2) {
            for (int col = 0; col < cols_2/4; col ++) {

                a = (float) wLP_2.get(row, col)[0];
                b = (float) wLP_2.get(row+1, col)[0];
                c = (float) ( (a+b) * 0.707 );
                d = (float) ( (a-b) * 0.707 );

                int _row = row / 2;
                wA_2.put(_row, col, c);
                wH_2.put(_row, col, d);
            }
        }

        for (int row = 0; row < rows_2 / 2; row += 2) {
            for (int col = 0; col < cols_2 / 4; col++) {

                a = (float) wHP_2.get(row, col)[0];
                b = (float) wHP_2.get(row+1, col)[0];
                c = (float) ( (a+b) * 0.707 );
                d = (float) ( (a-b) * 0.707 );

                int _row = row / 2;
                wV_2.put(_row, col, c);
                wD_2.put(_row, col, d);
            }
        }

        Core.normalize(wA_1, wA_1, 0,255, Core.NORM_MINMAX, CvType.CV_8UC1);
        Core.normalize(wH_1, wH_1, 0,255, Core.NORM_MINMAX, CvType.CV_8UC1);
        Core.normalize(wV_1, wV_1, 0,255, Core.NORM_MINMAX, CvType.CV_8UC1);
        Core.normalize(wD_1, wD_1, 0,255, Core.NORM_MINMAX, CvType.CV_8UC1);

        Core.normalize(wA_2, wA_2, 0,255, Core.NORM_MINMAX, CvType.CV_8UC1);
        Core.normalize(wH_2, wH_2, 0,255, Core.NORM_MINMAX, CvType.CV_8UC1);
        Core.normalize(wV_2, wV_2, 0,255, Core.NORM_MINMAX, CvType.CV_8UC1);
        Core.normalize(wD_2, wD_2, 0,255, Core.NORM_MINMAX, CvType.CV_8UC1);


        Mat q0 = new Mat(dstImage, new Rect(0, 0, cols/2, rows/2));
        Mat q1 = new Mat(dstImage, new Rect(cols/2, 0, cols/2, rows/2));
        Mat q2 = new Mat(dstImage, new Rect(0, rows/2, cols/2, rows/2));
        Mat q3 = new Mat(dstImage, new Rect(cols/2, rows/2, cols/2, rows/2));

        Mat q4 = new Mat(dstImage, new Rect(0, 0, q0.cols()/2, q0.rows()/2));
        Mat q5 = new Mat(dstImage, new Rect(q0.cols()/2, 0, q0.cols()/2, q0.rows()/2));
        Mat q6 = new Mat(dstImage, new Rect(0, q0.rows()/2, q0.cols()/2, q0.rows()/2));
        Mat q7 = new Mat(dstImage, new Rect(q0.cols()/2, q0.rows()/2, q0.cols()/2, q0.rows()/2));


        wA_2.copyTo(q4);
        wH_2.copyTo(q5);
        wV_2.copyTo(q6);
        wD_2.copyTo(q7);
        wH_1.copyTo(q1);
        wV_1.copyTo(q2);
        wD_1.copyTo(q3);

        return dstImage;
    }
}
