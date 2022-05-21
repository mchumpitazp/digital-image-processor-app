//
//  Wrapper.m
//  imageprocessor
//
//  Created by Mauro Chumpitaz Polino on 29/11/21.
//

#import "Wrapper.h"
#import <opencv2/opencv.hpp>
#import <opencv2/imgcodecs/ios.h>
#import <opencv2/imgproc.hpp>
#include <numeric>

@implementation Wrapper : NSObject

+ (NSString *) openCVVsersionString {
    return [NSString stringWithFormat:@"OpenCV Version %s", CV_VERSION];
}

+(int) getImageInfo : (UIImage *) image {
    cv::Mat srcImage;
    UIImageToMat(image, srcImage);
    
//    std::cout << cv::typeToString(srcImage.type()) << "\n";
    return srcImage.channels();
}

+ (UIImage *) toGrayscale: (UIImage *) image {
    cv::Mat srcImage;
    UIImageToMat(image, srcImage);
    
    // Solution to undesired rotation of image
    const int rows = image.size.height;
    cv::cvtColor(srcImage, srcImage, cv::COLOR_RGB2GRAY);
    if (rows != MatToUIImage(srcImage).size.height) {
        cv::rotate(srcImage, srcImage, cv::ROTATE_90_CLOCKWISE);
    }
    return MatToUIImage(srcImage);
}

+(UIImage *) rotate : (UIImage *) image {
    cv::Mat srcImage;
    UIImageToMat(image, srcImage);
    
    cv::rotate(srcImage, srcImage, cv::ROTATE_90_CLOCKWISE);
    return MatToUIImage(srcImage);
    
}

+ (Boolean) isColor: (UIImage *) image {

    cv::Mat srcImage;
    UIImageToMat(image, srcImage);
    
    return srcImage.channels() > 1;
}

//  ============================================  B A S I C     T R A N S F O R M S  =======================================

+ (UIImage *) negate : (UIImage *) image {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    
    cv::bitwise_not(srcImage, dstImage);
    return MatToUIImage(dstImage);
}

+ (UIImage *) log : (UIImage *) image {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    
    const int channels = srcImage.channels();
    const double c = 255 / log(256.0);
    const cv::Scalar ones = (channels == 3) ? cv::Scalar(1.0, 1.0, 1.0) : cv::Scalar(1.0);
    const cv::Scalar cs = (channels == 3) ? cv::Scalar(c, c, c) : cv::Scalar(c);
    
    srcImage.convertTo(dstImage, CV_32FC(channels));
    cv::add(dstImage, ones, dstImage);
    cv::log(dstImage, dstImage);
    cv::multiply(dstImage, cs, dstImage);
    
    dstImage.convertTo(dstImage, CV_8UC(channels));
    return MatToUIImage(dstImage);
}

+ (UIImage *) inverseLog : (UIImage *) image {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    
    const int channels = srcImage.channels();
    const double c = log(256.0) / 255;
    const cv::Scalar ones = (channels == 3) ? cv::Scalar(1.0, 1.0, 1.0) : cv::Scalar(1.0);
    
    srcImage.convertTo(dstImage, CV_64FC(channels));
    cv::exp(dstImage, dstImage);
    cv::pow(dstImage, c, dstImage);
    cv::subtract(dstImage, ones, dstImage);
    
    dstImage.convertTo(dstImage, CV_8UC(channels));
    return MatToUIImage(dstImage);
}

+ (UIImage *) powerLaw : (UIImage *) image singleParam: (double) gamma {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_BGRA2BGR);
    
    const int channels = srcImage.channels();
    const cv::Scalar max = (channels == 3) ? cv::Scalar(255.0, 255.0, 255.0) : cv::Scalar(255.0);
    
    srcImage.convertTo(dstImage, CV_32FC(channels));
    cv::divide(dstImage, max, dstImage);
    cv::pow(dstImage, gamma, dstImage);
    cv::multiply(dstImage, max, dstImage);
    
    dstImage.convertTo(dstImage, CV_8UC(channels));
    return MatToUIImage(dstImage);
}

+(UIImage *) contrastStretching: (UIImage *) image r1:(int)lowX s1:(int)lowY r2:(int)highX s2:(int)highY {
    cv::Mat dstImage;
    UIImageToMat(image, dstImage);
    
    if (dstImage.channels() == 4)
        cv::cvtColor(dstImage, dstImage, cv::COLOR_RGBA2RGB);
    
    const double lowSlope     = (double) lowY / lowX;
    const double midSlope     = (double) (highY - lowY) / (highX - lowX);
    const double midConstant  = lowY - (midSlope * lowX);
    const double highSlope    = (double) (255 - highY) / (255 - highX);
    const double highConstant = 255.0 - (highSlope * 255);
    
    std::vector<uchar> lookUpTable(256);
    for (size_t m = 0; m < 256; m++) {
        if (m < lowX)
            lookUpTable[m] = static_cast<uchar>(round((double)m * lowSlope));
        else if (m <= highX)
            lookUpTable[m] = static_cast<uchar>(round((double)m * midSlope + midConstant));
        else
            lookUpTable[m] = static_cast<uchar>(round((double)m * highSlope + highConstant));
    }
        
    int nRows = dstImage.rows;
    int nCols = dstImage.cols * dstImage.channels();
    uchar* p;
    int i, j;
    
    if (dstImage.isContinuous()) {
        nCols *= nRows;
        nRows = 1;
    }
        
    for (i = 0; i < nRows; i++) {
        p = dstImage.ptr<uchar>(i);
        for (j = 0; j < nCols; j++) {
            p[j] = lookUpTable[p[j]];
        }
    }
    
    return MatToUIImage(dstImage);
}

+(UIImage *) bitPlaneSlicing: (UIImage *) image bitPlane:(int)plane {
    cv::Mat dstImage;
    UIImageToMat(image, dstImage);
    
    const int mask = int(pow(2, plane));
    int nRows = dstImage.rows;
    int nCols = dstImage.cols;
    uchar* p;
    int i, j;
    
    if (dstImage.isContinuous()) {
        nCols *= nRows;
        nRows = 1;
    }
        
    for (i = 0; i < nRows; i++) {
        p = dstImage.ptr<uchar>(i);
        for (j = 0; j < nCols; j++) {
            if ((p[j] & mask) != 0)
                p[j] = 255;
            else
                p[j] = 0;
        }
    }
    
    return MatToUIImage(dstImage);
}

+(UIImage *) equalizeHist: (UIImage *) image {
    cv::Mat dstImage;
    UIImageToMat(image, dstImage);
    
    cv::equalizeHist(dstImage, dstImage);
    return MatToUIImage(dstImage);
}


//  ============================================  S P A T I A L     D O M A I N  =======================================

+(UIImage *) meanBlur: (UIImage *) image width:(int)width height:(int)height {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    cv::boxFilter(srcImage, dstImage, -1, cv::Size(width, height));
    return MatToUIImage(dstImage);
}

+(UIImage *) gaussianBlur: (UIImage *) image width:(int)width height:(int)height sigma:(double)sigma {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    cv::GaussianBlur(srcImage, dstImage, cv::Size(width, height), sigma);
    return MatToUIImage(dstImage);
}

+(UIImage *) spatialLaplacian: (UIImage *) image kernelSize:(int)size {
    cv::Mat dstImage;
    cv::Mat grayImage;
    UIImageToMat(image, grayImage);
    
    grayImage.convertTo(dstImage, CV_32F);
    
    /*
    float arrKernel[3][3] = {
        {1, 1, 1},
        {1, -8, 1},
        {1, 1, 1},
    };
    
    cv::Mat kernel = cv::Mat(3, 3, CV_32F, &arrKernel);
    cv::filter2D(grayImage, dstImage, -1, kernel);
     */
    
    cv::Laplacian(dstImage, dstImage, -1, size);
    //cv::normalize(dstImage, dstImage, 0, 255, cv::NORM_MINMAX);
    dstImage.convertTo(dstImage, CV_8U);
    cv::subtract(grayImage, dstImage, dstImage);
    
    return MatToUIImage(dstImage);
}

+(UIImage *) firstDerivative: (UIImage *) image kernelType:(NSString *)type {
    cv::Mat dstImage;
    cv::Mat grayImage;
    cv::Mat kernelX;
    cv::Mat kernelY;
    UIImageToMat(image, grayImage);

    grayImage.convertTo(grayImage, CV_16SC1);
    
    if ([type  isEqual: @"Sobel"]) {
        cv::Mat SobelX;
        cv::Mat SobelY;
        cv::Sobel(grayImage, SobelX, CV_16SC1, 1, 0, 3);
        cv::Sobel(grayImage, SobelY, CV_16SC1, 0, 1, 3);
        cv::convertScaleAbs(SobelX, SobelX);
        cv::convertScaleAbs(SobelY, SobelY);
        cv::add(SobelX, SobelY, dstImage);
    }
    else if ([type  isEqual: @"SobelX"]) {
        cv::Sobel(grayImage, dstImage, CV_16SC1, 0, 1, 3);
        cv::convertScaleAbs(dstImage, dstImage);
    }
    else if ([type  isEqual: @"SobelY"]) {
        cv::Sobel(grayImage, dstImage, CV_16SC1, 1, 0, 3);
        cv::convertScaleAbs(dstImage, dstImage);
    }
    else if ([type  isEqual: @"Prewitt"]) {
        cv::Mat PrewittX;
        cv::Mat PrewittY;
        
        int16_t arrKernelX[3][3] = {
            {1, 1, 1},
            {0, 0, 0},
            {-1, -1, -1},
        };
        int16_t arrKernelY[3][3] = {
            {-1, 0, 1},
            {-1, 0, 1},
            {-1, 0, 1},
        };
        
        kernelX = cv::Mat(3, 3, CV_16SC1, &arrKernelX);
        kernelY = cv::Mat(3, 3, CV_16SC1, &arrKernelY);

        cv::filter2D(grayImage, PrewittX, -1, kernelX);
        cv::filter2D(grayImage, PrewittY, -1, kernelY);
        cv::convertScaleAbs(PrewittX, PrewittX);
        cv::convertScaleAbs(PrewittY, PrewittY);
        cv::add(PrewittX, PrewittY, dstImage);
    }
    else if ([type  isEqual: @"PrewittX"]) {
        int16_t arrKernelX[3][3] = {
            {1, 1, 1},
            {0, 0, 0},
            {-1, -1, -1},
        };
        
        kernelX = cv::Mat(3, 3, CV_16SC1, &arrKernelX);
        cv::filter2D(grayImage, dstImage, -1, kernelX);
        cv::convertScaleAbs(dstImage, dstImage);
    }
    else if ([type  isEqual: @"PrewittY"]) {
        int16_t arrKernelY[3][3] = {
            {-1, 0, 1},
            {-1, 0, 1},
            {-1, 0, 1},
        };
        
        kernelY = cv::Mat(3, 3, CV_16SC1, &arrKernelY);
        cv::filter2D(grayImage, dstImage, -1, kernelY);
        cv::convertScaleAbs(dstImage, dstImage);
    }
    else if ([type  isEqual: @"Roberts"]) {
        cv::Mat RobertsX;
        cv::Mat RobertsY;
        
        int16_t arrKernelX[3][3] = {
            {0, 0, 0},
            {0, -1, 0},
            {0, 0, 1},
        };
        int16_t arrKernelY[3][3] = {
            {0, 0, 0},
            {0, 0, -1},
            {0, 1, 0},
        };
        
        kernelX = cv::Mat(3, 3, CV_16SC1, &arrKernelX);
        kernelY = cv::Mat(3, 3, CV_16SC1, &arrKernelY);
        
        cv::filter2D(grayImage, RobertsX, -1, kernelX);
        cv::filter2D(grayImage, RobertsY, -1, kernelY);
        cv::convertScaleAbs(RobertsX, RobertsX);
        cv::convertScaleAbs(RobertsY, RobertsY);
        cv::add(RobertsX, RobertsY, dstImage);
    }
    else {
        grayImage.copyTo(dstImage);
    }
    dstImage.convertTo(dstImage, CV_8UC1); // se puede omitir, convertScaleAbs ya transforma y la suma de dos 8-bits sigue siendo 8-bits
    return MatToUIImage(dstImage);
}

//  ============================================  F R E Q U E N C Y    D O M A I N  =======================================
    
+ (cv::Mat) getDFT: (cv::Mat)srcImage padRows:(int)padRows padCols:(int)padCols {
    cv::Mat paddedImage;
    cv::Mat dftImage;
    std::vector<cv::Mat> planes;
    
    // Image padding
    cv::copyMakeBorder(srcImage, paddedImage, 0, padRows - srcImage.rows, 0, padCols - srcImage.cols, cv::BORDER_CONSTANT, cv::Scalar::all(0));
    paddedImage.convertTo(paddedImage, CV_32FC1);
    planes.push_back(paddedImage);
    planes.push_back(cv::Mat::zeros(paddedImage.size(), CV_32FC1));
    cv::merge(planes, dftImage);
    cv::dft(dftImage, dftImage);
    
    return dftImage;
}

+ (void) shiftQuadrants: (cv::Mat)image {
    image = image(cv::Rect(0, 0, image.cols & -2, image.rows & -2));
    const int cx = image.cols / 2;
    const int cy = image.rows / 2;
    
    cv::Mat q0 = cv::Mat(image, cv::Rect(0, 0, cx, cy));
    cv::Mat q1 = cv::Mat(image, cv::Rect(cx, 0, cx, cy));
    cv::Mat q2 = cv::Mat(image, cv::Rect(0, cy, cx, cy));
    cv::Mat q3 = cv::Mat(image, cv::Rect(cx, cy, cx, cy));
    
    cv::Mat tmp;
    q0.copyTo(tmp);
    q3.copyTo(q0);
    tmp.copyTo(q3);
    
    q1.copyTo(tmp);
    q2.copyTo(q1);
    tmp.copyTo(q2);
}

+ (cv::Mat) convolveDFT: (cv::Mat) srcImage filter:(cv::Mat)filter padRows:(int)padRows padCols:(int)padCols {
    cv::Mat dftImage;
    cv::Mat realPlane;
    cv::Mat imaginaryPlane;
    cv::Mat filteredImage;
    std::vector<cv::Mat> planes;
    
    dftImage = [self getDFT:srcImage padRows:padRows padCols:padCols];
    [self shiftQuadrants:dftImage];
    cv::split(dftImage, planes);
    
    cv::multiply(planes.at(0), filter, realPlane);
    cv::multiply(planes.at(1), filter, imaginaryPlane);
    planes.clear();
    planes.push_back(realPlane);
    planes.push_back(imaginaryPlane);
    cv::merge(planes, filteredImage);
    
    cv::idft(filteredImage, filteredImage);
    planes.clear();
    cv::split(filteredImage, planes);
    cv::magnitude(planes.at(0), planes.at(1), filteredImage);
    filteredImage = filteredImage(cv::Rect(0, 0, srcImage.cols, srcImage.rows));
    cv::normalize(filteredImage, filteredImage, 0, 255, cv::NORM_MINMAX, CV_8UC1);
    
    return filteredImage;
}

+(UIImage *) freqIdeal: (UIImage *) image isLowpass:(bool)isLowpass cutOffFrequency:(int)D_o {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    const int padRows = cv::getOptimalDFTSize(2 * srcImage.rows);
    const int padCols = cv::getOptimalDFTSize(2 * srcImage.cols);
    const int midRows = padRows / 2;
    const int midCols = padCols / 2;
    cv::Mat idealFilter = cv::Mat(padRows, padCols, CV_8UC1);
    const int filterChanger = isLowpass ? 0 : 255;
    double D;
    
    uchar* p;
    int i, j;
    
    for (i = 0; i < padRows; i++) {
        p = idealFilter.ptr<uchar>(i);

        for (j = 0; j < padCols; j++) {
            D = sqrt(pow(i - midRows, 2) + pow(j - midCols, 2));

            if (D <= D_o)
                p[j] = (uchar) 255 - filterChanger;
            else
                p[j] = (uchar) filterChanger;
        }
    }

    idealFilter.convertTo(idealFilter, CV_32FC1);
    return MatToUIImage([self convolveDFT:srcImage filter:idealFilter padRows:padRows padCols:padCols]);
}

+(UIImage *) freqButterworth: (UIImage *) image isLowpass:(bool)isLowpass cutOffFrequency:(int)D_o n:(int)n {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    const int padRows = cv::getOptimalDFTSize(2 * srcImage.rows);
    const int padCols = cv::getOptimalDFTSize(2 * srcImage.cols);
    const int midRows = padRows / 2;
    const int midCols = padCols / 2;
//    cv::Mat butterworthFilter = cv::Mat(padRows, padCols, CV_32FC1);
    cv::Mat butterworthFilter = cv::Mat(padRows, padCols, CV_32FC1);
    double D;
    double pixelValue;
    float* p;
    
    for (int i = 0; i < padRows; i++) {
        p = butterworthFilter.ptr<float>(i);
        
        for (int j = 0; j < padCols; j++) {
            D = sqrt(pow(i - midRows, 2) + pow(j - midCols, 2));
            
            pixelValue = 1.0 / (1 + pow(D/D_o, 2*n));
            if (!isLowpass)
                pixelValue = 1 - pixelValue;
            
            p[j] = pixelValue;
        }
    }
    
    return MatToUIImage([self convolveDFT:srcImage filter:butterworthFilter padRows:padRows padCols:padCols]);
}

+(UIImage *) freqGaussian: (UIImage *) image isLowpass:(bool)isLowpass cutOffFrequency:(int)D_o {
//    std::chrono::steady_clock::time_point begin = std::chrono::steady_clock::now();
    
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    const int padRows = cv::getOptimalDFTSize(2 * srcImage.rows);
    const int padCols = cv::getOptimalDFTSize(2 * srcImage.cols);
    cv::Mat gaussianFilter = cv::Mat(padRows, padCols, CV_32FC1);
    double D;
    float pixelValue;
    const int constDenominator = 2 * D_o * D_o;
    float* p;
        
    for (int i = 0; i < padRows; i++) {
        p = gaussianFilter.ptr<float>(i);
        for (int j = 0; j < padCols; j++) {
            D = sqrt(pow(i - padRows/2, 2) + pow(j - padCols/2, 2));

            pixelValue = exp(-(D*D) / constDenominator);
            if (!isLowpass)
                pixelValue = 1 - pixelValue;

            p[j] = pixelValue;
        }
    }
    
//    std::chrono::steady_clock::time_point end = std::chrono::steady_clock::now();
//
//    std::cout << "Time difference = " << std::chrono::duration_cast<std::chrono::milliseconds>(end - begin).count() << "[ms]" << std::endl;
    
    return MatToUIImage([self convolveDFT:srcImage filter:gaussianFilter padRows:padRows padCols:padCols]);
}

+(UIImage *) freqLaplacian: (UIImage *) image; {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    const int padRows = cv::getOptimalDFTSize(2 * srcImage.rows);
    const int padCols = cv::getOptimalDFTSize(2 * srcImage.cols);
    cv::Mat laplacianFilter = cv::Mat(padRows, padCols, CV_32FC1);
    double D;
    double pixelValue;
    float* p;
    
    for (int i = 0; i < padRows; i++) {
        p = laplacianFilter.ptr<float>(i);
        for (int j = 0; j < padCols; j++) {
            D = sqrt(pow(i - padRows/2, 2) + pow(j - padCols/2, 2));
            
            pixelValue = 1 + 4 * pow(CV_PI, 2) * pow(D, 2);
            p[j] = pixelValue;
        }
    }
    
    return MatToUIImage([self convolveDFT:srcImage filter:laplacianFilter padRows:padRows padCols:padCols]);
}

//  ============================================   N O I S E     F I L T E R I N G  =======================================

+(UIImage *) geometricMean: (UIImage *) image width:(int)width height:(int)height {
    cv::Mat srcImage;
    cv::Mat dstImage;
    
    UIImage *dstImageUI = [self log:image];
    UIImageToMat(dstImageUI, dstImage);
    cv::boxFilter(dstImage, dstImage, -1, cv::Size(width, height));
    dstImageUI = [self inverseLog:MatToUIImage(dstImage)];
    
    return dstImageUI;
}

+(UIImage *) contraharmonicMean: (UIImage *) image powerQ:(double)Q width:(int)width height:(int)height {
    cv::Mat srcImage;
    cv::Mat dstImage;
    cv::Mat num, den;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    
    const int channels = srcImage.channels();
    cv::Scalar ones = (channels == 3) ? cv::Scalar(0.00000001, 0.00000001, 0.00000001) : cv::Scalar(0.00000001);
    
    srcImage.convertTo(dstImage, CV_32FC(channels));
    
    cv::add(dstImage, ones, dstImage);
    cv::pow(dstImage, Q+1, num);
    cv::pow(dstImage, Q, den);
    cv::boxFilter(num, num, -1, cv::Size(width, height));
    cv::boxFilter(den, den, -1, cv::Size(width, height));
    cv::divide(num, den, dstImage);
    
    dstImage.convertTo(dstImage, CV_8UC(channels));
    return MatToUIImage(dstImage);
}

+(UIImage *) medianFilter: (UIImage *) image kernelSize:(int)size {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    
    cv::medianBlur(srcImage, dstImage, size);
    return MatToUIImage(dstImage);
}

+(UIImage *) minFilter: (UIImage *) image width:(int)width height:(int)height {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    
    cv::Mat kernel = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(width, height));
    cv::erode(srcImage, dstImage, kernel);
    return MatToUIImage(dstImage);
}

+(UIImage *) maxFilter: (UIImage *) image width:(int)width height:(int)height {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    
    cv::Mat kernel = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(width, height));
    cv::dilate(srcImage, dstImage, kernel);
    return MatToUIImage(dstImage);
}

+(UIImage *) midpointFilter: (UIImage *) image width:(int)width height:(int)height {
    cv::Mat srcImage;
    cv::Mat dstImage;
    cv::Mat minImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    
    cv::Scalar scalar = (srcImage.channels() == 3) ? cv::Scalar(2, 2, 2) : cv::Scalar(2);
    
    cv::Mat kernel = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(width, height));
    cv::erode(srcImage, minImage, kernel);
    minImage.convertTo(minImage, CV_16UC(srcImage.channels()));
    cv::dilate(srcImage, dstImage, kernel);
    dstImage.convertTo(dstImage, CV_16UC(srcImage.channels()));
    
    cv::add(dstImage, minImage, dstImage);
    cv::divide(dstImage, scalar, dstImage);
    dstImage.convertTo(dstImage, CV_8UC(srcImage.channels()));
    
    return MatToUIImage(dstImage);
}

+(UIImage *) alphaTrimmedMean: (UIImage *) image width:(int)width height:(int)height d:(int)d {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);

    std::cout << "NOT ENDED";
    
    if (srcImage.channels() == 4) {
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    }

    const int den = (width * height) - d;
    const int channels = srcImage.channels();
    const int maxWidthShift  = (width - 1) / 2;
    const int maxHeightShift = (height - 1) / 2;
    if (d % 2 == 1) {
        d += 1;
    }
    
    dstImage = cv::Mat(srcImage.rows, srcImage.cols, CV_32FC(channels));
    std::deque<float> sortedKernel;
    float val;
    

    for (int row = 0; row < srcImage.rows; row++) {
        for (int col = 0; col < srcImage.cols; col++) {
            
            if ((row - maxWidthShift) > 0 & (row + maxWidthShift) < srcImage.rows &
                (col - maxHeightShift) > 0 & (col + maxHeightShift) < srcImage.cols) {
                
                for (int ch = 0; ch < channels; ch++) {
                    for (int i = -maxWidthShift; i <= maxWidthShift; i++) {
                        for (int j = -maxHeightShift; j <= maxHeightShift; j++) {
//                            sortedKernel.push_back(srcImage.at<cv::Vec3b>(row+i, col+j)[ch]);
                            sortedKernel.push_back(srcImage.at<float>(row+i, col+j, ch));
                        }
                    }
                    std::sort(sortedKernel.begin(), sortedKernel.end());
                    for (int r = 0; r < d/2; r++) {
                        sortedKernel.pop_back();
                        sortedKernel.pop_front();
                    }
                    val = std::accumulate(sortedKernel.begin(), sortedKernel.end(), 0);
//                    dstImage.at<cv::Vec3b>(row, col)[ch] = val / den;
                    dstImage.at<float>(row, col, ch) = val / den;
                    sortedKernel.clear();
                }
            }
            else {
                dstImage.at<cv::Vec3b>(row, col) = srcImage.at<cv::Vec3b>(row, col);
            }
        }
    }
    
    dstImage.convertTo(dstImage, CV_8UC(channels));
    std::cout << "ENDED";

    return MatToUIImage(dstImage);
}

//  ============================================  C O L O R     P R O C E S S I N G  =======================================
+(UIImage *) rgb2planes: (UIImage *) image planeRGB:(int)plane {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
//
//    if (image.size.height != srcImage.rows) {
//        cv::rotate(srcImage, srcImage, cv::ROTATE_90_CLOCKWISE);
//    }
    
    int nRows = srcImage.rows;
    int nCols = srcImage.cols * srcImage.channels();
    dstImage = cv::Mat(nRows, srcImage.cols, CV_8UC1);
    
    if (srcImage.isContinuous()) {
        nCols *= nRows;
        nRows = 1;
    }
    
    int i, j;
    uchar* srcP;
    uchar* dstP;
    
    for (i = 0; i < nRows; ++i) {
        srcP = srcImage.ptr<uchar>(i);
        dstP = dstImage.ptr<uchar>(i);
        
        for (j = 0; j < nCols; j+=3) {
            dstP[(int)ceil((double)j/3)] = srcP[j+plane];
        }
    }
    
    return MatToUIImage(dstImage);
}

+(UIImage *) rgb2hsi: (UIImage *) image planeHSI:(int)plane {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    
    
    const double toDegrees = 180.0 / CV_PI;
    int nRows = srcImage.rows;
    int nCols = srcImage.cols * srcImage.channels();
    dstImage = cv::Mat(nRows, srcImage.cols, CV_8UC1);
    
    if (srcImage.isContinuous()) {
        nCols *= nRows;
        nRows = 1;
    }
    
    int i, j;
    uchar* srcP;
    uchar* dstP;
        
    switch (plane) {
        case 0: // Hue plane
            double num, den, H;
            
            for (i = 0; i < nRows; ++i) {
                srcP = srcImage.ptr<uchar>(i);
                dstP = dstImage.ptr<uchar>(i);
                
                for (j = 0; j < nCols; j+=3) {
                    double R = double(srcP[j]) / 255;
                    double G = double(srcP[j+1]) / 255;
                    double B = double(srcP[j+2]) / 255;
                    
                    num = 0.5 * ((R - G) + (R - B));
                    den = sqrt(pow(R - G, 2) + ((R - B) * (G - B)));
                    H = acos(num / (den + 0.000001)) * toDegrees;
                    
                    if (B > G)
                        H = 360 - H;
                                        
                    dstP[(int)ceil((double)j/3)] = (uchar) round(H / 360 * 255);
                    
                }
            }
            break;
        case 1: // Saturation plane
            double S;
            
            for (i = 0; i < nRows; ++i) {
                srcP = srcImage.ptr<uchar>(i);
                dstP = dstImage.ptr<uchar>(i);
                
                for (j = 0; j < nCols; j+=3) {
                    double R = double(srcP[j]) / 255;
                    double G = double(srcP[j+1]) / 255;
                    double B = double(srcP[j+2]) / 255;
                    
                    S = 1 - (3 / (R + G + B + 0.000001) * MIN(MIN(R, G), B));
                    dstP[(int)ceil((double)j/3)] = (uchar) round(S * 255);
                    
                }
            }
            break;
        case 2: // Intensity plane
            for (i = 0; i < nRows; ++i) {
                double I;
                
                srcP = srcImage.ptr<uchar>(i);
                dstP = dstImage.ptr<uchar>(i);
                
                for (j = 0; j < nCols; j+=3) {
                    double R = double(srcP[j]) / 255;
                    double G = double(srcP[j+1]) / 255;
                    double B = double(srcP[j+2]) / 255;
                    
                    I = (R + G + B) / 3;
                    dstP[(int)ceil((double)j/3)] = (uchar) round(I * 255);
                }
            }
            break;
        default:
            return MatToUIImage(srcImage);
    }
    
    return MatToUIImage(dstImage);
}


+(UIImage *) graySlicing: (UIImage *) image intensity1:(int)i1 intensity2:(int)i2 intensity3:(int)i3 {
    cv::Mat grayImage;
    cv::Mat dstImage;
    UIImageToMat(image, grayImage);
        
    int nRows = grayImage.rows;
    int nCols = grayImage.cols * 3;
    
    dstImage = cv::Mat(nRows, nCols/3, CV_8UC3);
    
    if (grayImage.isContinuous()) {
        nCols *= nRows;
        nRows = 1;
    }
    
    int i, j;
    uchar  gray;
    uchar* dstP;
    uchar* srcP;
    
    for (i = 0; i < nRows; ++i) {
        srcP = grayImage.ptr<uchar>(i);
        dstP = dstImage.ptr<uchar>(i);
        
        for (j = 0; j < nCols; j+=3) {
            gray = srcP[(int)ceil((double)j/3)];
            
            if (gray == i1) {
                dstP[j]   = (uchar) (255);
                dstP[j+1] = (uchar) (255);
                dstP[j+2] = (uchar) (0);
            }
            else if (gray == i2) {
                dstP[j]   = (uchar) (255);
                dstP[j+1] = (uchar) (0);
                dstP[j+2] = (uchar) (255);
            }
            else if (gray == i3) {
                dstP[j]   = (uchar) (0);
                dstP[j+1] = (uchar) (255);
                dstP[j+2] = (uchar) (255);
            }
            else {
                dstP[j]   = (uchar) (0);
                dstP[j+1] = (uchar) (0);
                dstP[j+2] = (uchar) (0);
            }
            
        }
    }
    return MatToUIImage(dstImage);
}

+(double) sinFunction: (double)input :(double)freq :(double)shift {
    return abs( sin( (input * freq * CV_PI) + (shift * CV_PI) ) );
}

+(UIImage *) gray2color: (UIImage *) image frequency:(double)freq shiftRed:(double)shiftR shiftGreen:(double)shiftG shiftBlue:(double)shiftB {
    cv::Mat grayImage;
    cv::Mat dstImage;
    UIImageToMat(image, grayImage);
        
    int nRows = grayImage.rows;
    int nCols = grayImage.cols * 3;
    
    dstImage = cv::Mat(nRows, nCols/3, CV_8UC3);
    
    if (grayImage.isContinuous()) {
        nCols *= nRows;
        nRows = 1;
    }
    
    int i, j;
    double gray, R, G, B;
    uchar* dstP;
    uchar* srcP;
    
    for (i = 0; i < nRows; ++i) {
        srcP = grayImage.ptr<uchar>(i);
        dstP = dstImage.ptr<uchar>(i);
        
        for (j = 0; j < nCols; j+=3) {
            gray = (double) srcP[(int)ceil((double)j/3)] / 255;
            R = [self sinFunction:gray :freq :shiftR];
            G = [self sinFunction:gray :freq :shiftG];
            B = [self sinFunction:gray :freq :shiftB];
            
            dstP[j]   = (uchar) (R * 255);
            dstP[j+1] = (uchar) (G * 255);
            dstP[j+2] = (uchar) (B * 255);
        }
    }
    return MatToUIImage(dstImage);
}

+(UIImage *) colorBalance: (UIImage *) image percent:(int)percent {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    else if (srcImage.channels() == 1) {
        return image;
    }
    
    float half_percent = (float) percent / 200.0f;
    
    std::vector<cv::Mat> tmpsplit;
    cv::split(srcImage, tmpsplit);
    
    for (int i = 0; i < 3; ++i) {
        cv::Mat flat;
        tmpsplit[i].reshape(1,1).copyTo(flat);
        cv::sort(flat, flat, cv::SORT_EVERY_ROW + cv::SORT_ASCENDING);
        int lowval = flat.at<uchar>(cvFloor(((float)flat.cols) * half_percent));
        int highval = flat.at<uchar>(cvCeil(((float)flat.cols) * (1.0 - half_percent)));
        
        tmpsplit[i].setTo(lowval, tmpsplit[i] < lowval);
        tmpsplit[i].setTo(highval, tmpsplit[i] > highval);
        
        cv::normalize(tmpsplit[i], tmpsplit[i], 0, 255, cv::NORM_MINMAX);
        cv::merge(tmpsplit, dstImage);
    }

    return MatToUIImage(dstImage);
}

//  ============================================  W A V E L E T S  =======================================

+(cv::Mat) haarLevel1Filter: (cv::Mat) srcImage isLowpass:(bool)isLowpass {
    cv::Mat grayImage;
    cv::Mat filter;
    srcImage.convertTo(grayImage, CV_64F);
    cv::divide(grayImage, cv::Scalar(255.0), grayImage);
    
    const int rows = (srcImage.rows % 2 == 1) ? srcImage.rows - 1 : srcImage.rows;
    const int cols = (srcImage.cols % 2 == 1) ? srcImage.cols - 1 : srcImage.cols;
    filter = cv::Mat(rows, cols/2, CV_64F);
    
    // Decomposition
    // Level 1
    double a, b, c;
    
    for (int row = 0; row < rows; row++) {
        for (int col = 0; col < cols; col += 2) {
            
            a = grayImage.at<double>(row, col);
            b = grayImage.at<double>(row, col+1);
            if (isLowpass) b = -b;
            c = (a-b) * 0.707;
            
            filter.at<double>(row, col/2) = c;
        }
    }
    
    return filter;
}

+(UIImage *) haarLevel1: (cv::Mat) srcImage isLowpassL1:(bool)isLowpassL1 isLowpassL2:(bool)isLowpassL2 {
    cv::Mat filterL1 = [self haarLevel1Filter:srcImage isLowpass:isLowpassL1];
    const int rows = filterL1.rows;
    const int cols = filterL1.cols;
    
    cv::Mat compressionL1 = cv::Mat(rows/2, cols, CV_64F);
    
    double a, b, c;
    for (int row = 0; row < rows; row += 2) {
        for (int col = 0; col < cols; col++) {
            
            a = filterL1.at<double>(row, col);
            b = filterL1.at<double>(row+1, col);
            if (isLowpassL2) b = -b;
            c = (a-b) * 0.707;
            
            compressionL1.at<double>(row/2, col) = c;
        }
    }
    
    cv::normalize(compressionL1, compressionL1, 0, 255, cv::NORM_MINMAX, CV_8UC1);
    return MatToUIImage(compressionL1);
}

+(cv::Mat) haarLevel2Filter: (cv::Mat) srcImage isLowpass:(bool)isLowpass {
    cv::Mat wA_1;
    UIImageToMat([self haarLevel1:srcImage isLowpassL1:true isLowpassL2:true], wA_1);
    wA_1.convertTo(wA_1, CV_64F);
    
    const int rows = (srcImage.rows % 2 == 1) ? srcImage.rows - 1 : srcImage.rows;
    const int cols = (srcImage.cols % 2 == 1) ? srcImage.cols - 1 : srcImage.cols;
    const int rows_2 = (rows/2 % 2 == 1) ? rows - 1 : rows;
    const int cols_2 = (cols/2 % 2 == 1) ? cols - 1 : cols;
    
    cv::Mat filter = cv::Mat(rows_2/2, cols_2/4, CV_64F);
    
    // Level 2
    double a, b, c;
    for (int row = 0; row < rows_2/2; row++) {
        for (int col = 0; col < cols_2/2; col+=2) {
            
            a = wA_1.at<double>(row, col);
            b = wA_1.at<double>(row, col+1);
            if (isLowpass) b = -b;
            c = (a-b) * 0.707;
            
            filter.at<double>(row, col/2) = c;
        }
    }
    
    return filter;
}

+(UIImage *) haarLevel2: (cv::Mat) srcImage isLowpassL1:(bool)isLowpassL1 isLowpassL2:(bool)isLowpassL2 {
    cv::Mat filterL2 = [self haarLevel2Filter:srcImage isLowpass:isLowpassL1];
    const int rows = filterL2.rows;
    const int cols = filterL2.cols;
    
    cv::Mat compressionL2 = cv::Mat(rows/2, cols, CV_64F);
    
    double a, b, c;
    for (int row = 0; row < rows; row += 2) {
        for (int col = 0; col < cols; col++) {
            
            a = filterL2.at<double>(row, col);
            b = filterL2.at<double>(row+1, col);
            if (isLowpassL2) b = -b;
            c = (a-b) * 0.707;
            
            compressionL2.at<double>(row/2, col) = c;
        }
    }
    
    cv::normalize(compressionL2, compressionL2, 0, 255, cv::NORM_MINMAX, CV_8UC1);
    return MatToUIImage(compressionL2);
}

+(UIImage *) haarForward: (UIImage *) image index:(int)index {
    cv::Mat srcImage;
    UIImageToMat(image, srcImage);
    
    switch (index) {
        // Level 1 - Approximation
        case 0: return [self haarLevel1:srcImage isLowpassL1:true isLowpassL2:true];  break;
            
        // Level 1 - Horizontal
        case 1: return [self haarLevel1:srcImage isLowpassL1:true isLowpassL2:false];  break;
            
        // Level 1 - Vertical
        case 2: return [self haarLevel1:srcImage isLowpassL1:false isLowpassL2:true];  break;
            
        // Level 1 - Diagonal
        case 3: return [self haarLevel1:srcImage isLowpassL1:false isLowpassL2:false];  break;
            
        // Level 2 - Approximation
        case 4: return [self haarLevel2:srcImage isLowpassL1:true isLowpassL2:true];  break;
            
        // Level 2 - Horizontal
        case 5: return [self haarLevel2:srcImage isLowpassL1:true isLowpassL2:false];  break;
            
        // Level 2 - Vertical
        case 6: return [self haarLevel2:srcImage isLowpassL1:false isLowpassL2:true];  break;
            
        // Level 2 - Diagonal
        case 7: return [self haarLevel2:srcImage isLowpassL1:false isLowpassL2:false];  break;
            
        default: return [self haarForwardFull:image]; break;
    }
}

+(UIImage *) haarForwardFull: (UIImage *) image {
    cv::Mat grayImage;
    cv::Mat dstImage;
    UIImageToMat(image, grayImage);

    dstImage = cv::Mat(grayImage.rows, grayImage.cols, CV_8UC1);
    grayImage.convertTo(grayImage, CV_64F);
    cv::divide(grayImage, cv::Scalar(255.0), grayImage);

    const int rows = (grayImage.rows % 2 == 1) ? grayImage.rows - 1 : grayImage.rows;
    const int cols = (grayImage.cols % 2 == 1) ? grayImage.cols - 1 : grayImage.cols;
    cv::Mat wLP_1 = cv::Mat(rows, cols/2, CV_64F);
    cv::Mat wHP_1 = cv::Mat(rows, cols/2, CV_64F);
    cv::Mat wA_1 = cv::Mat(rows/2, cols/2, CV_64F);
    cv::Mat wH_1 = cv::Mat(rows/2, cols/2, CV_64F);
    cv::Mat wV_1 = cv::Mat(rows/2, cols/2, CV_64F);
    cv::Mat wD_1 = cv::Mat(rows/2, cols/2, CV_64F);

    const int rows_2 = (rows/2 % 2 == 1) ? rows - 1 : rows;
    const int cols_2 = (cols/2 % 2 == 1) ? cols - 1 : cols;
    cv::Mat wLP_2 = cv::Mat(rows_2/2, cols_2/4, CV_64F);
    cv::Mat wHP_2 = cv::Mat(rows_2/2, cols_2/4, CV_64F);
    cv::Mat wA_2 = cv::Mat(rows_2/4, cols_2/4, CV_64F);
    cv::Mat wH_2 = cv::Mat(rows_2/4, cols_2/4, CV_64F);
    cv::Mat wV_2 = cv::Mat(rows_2/4, cols_2/4, CV_64F);
    cv::Mat wD_2 = cv::Mat(rows_2/4, cols_2/4, CV_64F);
    int row, col;

    // Decomposition
    // Level 1
    double a, b, c, d;

    for (row = 0; row < rows; ++row) {
        for (col = 0; col < cols; col += 2) {
            a = grayImage.at<double>(row, col);
            b = grayImage.at<double>(row, col+1);
            c = (a+b) * 0.7071;
            d = (a-b) * 0.7071;

            int _col = col / 2;
            wLP_1.at<double>(row, _col) = c;
            wHP_1.at<double>(row, _col) = d;
            
        }
    }

    for (row = 0; row < rows; row += 2) {
        for (col = 0; col < cols / 2; ++col) {
            a = wLP_1.at<double>(row, col);
            b = wLP_1.at<double>(row+1, col);
            c = (a+b) * 0.7071;
            d = (a-b) * 0.7071;

            int _row = row / 2;
            wA_1.at<double>(_row, col) = c;
            wH_1.at<double>(_row, col) = d;
        }
    }

    for (row = 0; row < rows; row += 2) {
        for (col = 0; col < cols / 2; ++col) {
            a = wHP_1.at<double>(row, col);
            b = wHP_1.at<double>(row+1, col);
            c = (a+b) * 0.7071;
            d = (a-b) * 0.7071;

            int _row = row / 2;
            wV_1.at<double>(_row, col) = c;
            wD_1.at<double>(_row, col) = d;
        }
    }

//     Level 2
    for (row = 0; row < rows_2 / 2; ++row) {
        for (col = 0; col < cols_2 / 2; col += 2) {
            a = wA_1.at<double>(row, col);
            b = wA_1.at<double>(row, col+1);
            c = (a+b) * 0.7071;
            d = (a-b) * 0.7071;

            int _col = col / 2;
            wLP_2.at<double>(row, _col) = c;
            wHP_2.at<double>(row, _col) = d;
        }
    }

    for (row = 0; row < rows_2 / 2; row += 2) {
        for (col = 0; col < cols_2 / 4; ++col) {
            a = wLP_2.at<double>(row, col);
            b = wLP_2.at<double>(row+1, col);
            c = (a+b) * 0.7071;
            d = (a-b) * 0.7071;

            int _row = row / 2;
            wA_2.at<double>(_row, col) = c;
            wH_2.at<double>(_row, col) = d;
        }
    }

    for (row = 0; row < rows_2 / 2; row += 2) {
        for (col = 0; col < cols_2 / 4; ++col) {
            a = wHP_2.at<double>(row, col);
            b = wHP_2.at<double>(row+1, col);
            c = (a+b) * 0.7071;
            d = (a-b) * 0.7071;

            int _row = row / 2;
            wV_2.at<double>(_row, col) = c;
            wD_2.at<double>(_row, col) = d;
        }
    }

    cv::normalize(wA_1, wA_1, 0, 255, cv::NORM_MINMAX, CV_8UC1);
    cv::normalize(wH_1, wH_1, 0, 255, cv::NORM_MINMAX, CV_8UC1);
    cv::normalize(wV_1, wV_1, 0, 255, cv::NORM_MINMAX, CV_8UC1);
    cv::normalize(wD_1, wD_1, 0, 255, cv::NORM_MINMAX, CV_8UC1);

    cv::normalize(wA_2, wA_2, 0, 255, cv::NORM_MINMAX, CV_8UC1);
    cv::normalize(wH_2, wH_2, 0, 255, cv::NORM_MINMAX, CV_8UC1);
    cv::normalize(wV_2, wV_2, 0, 255, cv::NORM_MINMAX, CV_8UC1);
    cv::normalize(wD_2, wD_2, 0, 255, cv::NORM_MINMAX, CV_8UC1);
    
    cv::Mat q0 = cv::Mat(dstImage, cv::Rect(0, 0,           cols/2, rows/2));
    cv::Mat q1 = cv::Mat(dstImage, cv::Rect(cols/2, 0,      cols/2, rows/2));
    cv::Mat q2 = cv::Mat(dstImage, cv::Rect(0, rows/2,      cols/2, rows/2));
    cv::Mat q3 = cv::Mat(dstImage, cv::Rect(cols/2, rows/2, cols/2, rows/2));

    cv::Mat q4 = cv::Mat(dstImage, cv::Rect(0, 0,                 cols/4, rows/4));
    cv::Mat q5 = cv::Mat(dstImage, cv::Rect(cols/4, 0,         cols/4, rows/4));
    cv::Mat q6 = cv::Mat(dstImage, cv::Rect(0, rows/4,         cols/4, rows/4));
    cv::Mat q7 = cv::Mat(dstImage, cv::Rect(cols/4, rows/4, cols/4, rows/4));

//    cv::Mat q0 = cv::Mat(dstImage, cv::Rect(0, 0, cols/2, rows/2));
//    cv::Mat q1 = cv::Mat(dstImage, cv::Rect((cols/2)-1, 0, cols/2, rows/2));
//    cv::Mat q2 = cv::Mat(dstImage, cv::Rect(0, (rows/2)-1, cols/2, rows/2));
//    cv::Mat q3 = cv::Mat(dstImage, cv::Rect((cols/2)-1, (rows/2)-1, cols/2, rows/2));
//
//    cv::Mat q4 = cv::Mat(dstImage, cv::Rect(0, 0, q0.cols/2, q0.rows/2));
//    cv::Mat q5 = cv::Mat(dstImage, cv::Rect((q0.cols/2)-1, 0, q0.cols/2, q0.rows/2));
//    cv::Mat q6 = cv::Mat(dstImage, cv::Rect(0, (q0.rows/2)-1, q0.cols/2, q0.rows/2));
//    cv::Mat q7 = cv::Mat(dstImage, cv::Rect((q0.cols/2)-1, (q0.rows/2)-1, q0.cols/2, q0.rows/2));

    wA_2.copyTo(q4);
    wH_2.copyTo(q5);
    wV_2.copyTo(q6);
    wD_2.copyTo(q7);
    wH_1.copyTo(q1);
    wV_1.copyTo(q2);
    wD_1.copyTo(q3);

    return MatToUIImage(dstImage);
}

//  ============================================  M O R P H O L O G Y  =======================================
+(cv::Mat) strelDisk: (int)radius {
    cv::Mat kernel;
    int i;
   
    switch (radius) {
        case 1:
            kernel = cv::getStructuringElement(cv::MORPH_CROSS, cv::Size(3,3));
            break;
        case 2: {
            uint8_t data_5x5[5][5] = {
                {0, 0, 1, 0, 0},
                {0, 1, 1, 1, 0},
                {1, 1, 1, 1, 1},
                {0, 1, 1, 1, 0},
                {0, 0, 1, 0, 0},
            };
            kernel = cv::Mat(5, 5, CV_8UC1, &data_5x5);
            break;
        }
        case 3:
            kernel = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(5, 5));
            break;
        case 4: {
            uint8_t data_7x7[7][7] = {
                {0, 0, 1, 1, 1, 0, 0},
                {0, 1, 1, 1, 1, 1, 0},
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1},
                {0, 1, 1, 1, 1, 1, 0},
                {0, 0, 1, 1, 1, 0, 0},
            };
            
            kernel = cv::Mat(7, 7, CV_8UC1, &data_7x7);
            break;
        }
        case 5:
            kernel = cv::Mat(9, 9, CV_8UC1);
            
            // row 0
            kernel.at<uchar>(0, 0) = 0;
            kernel.at<uchar>(0, 1) = 0;
            for (i = 2; i < 7; ++i) {
                kernel.at<uchar>(0, i) = 1;
            }
            kernel.at<uchar>(0, 7) = 0;
            kernel.at<uchar>(0, 8) = 0;
            
            // row 1
            kernel.at<uchar>(1, 0) = 0;
            for (i = 1; i < 8; ++i) {
                kernel.at<uchar>(1, i) = 1;
            }
            kernel.at<uchar>(1, 8) = 0;
            
            // rows 2 to 6
            for (i = 2; i < 7; ++i) {
                for (int j = 0; j < 9; j++) {
                    kernel.at<uchar>(i, j) = 1;
                }
            }
            
            // row 7
            kernel.at<uchar>(7, 0) = 0;
            for (i = 1; i < 8; ++i) {
                kernel.at<uchar>(7, i) = 1;
            }
            kernel.at<uchar>(7, 8) = 0;
            
            // row 8
            kernel.at<uchar>(8, 0) = 0;
            kernel.at<uchar>(8, 1) = 0;
            for (i = 2; i < 7; ++i) {
                kernel.at<uchar>(8, i) = 1;
            }
            kernel.at<uchar>(8, 7) = 0;
            kernel.at<uchar>(8, 8) = 0;
            
            break;
        default:
            kernel = cv::getStructuringElement(cv::MORPH_CROSS, cv::Size(3,3));
            break;
    }
    
    return kernel;
}

+(cv::Mat) getStrel: (int)strelShape :(int)width :(int)height {
    switch (strelShape) {
        case 0: // rectangular
            return cv::getStructuringElement(cv::MORPH_RECT, cv::Size(width, height));
        case 1: // cross
            return cv::getStructuringElement(cv::MORPH_CROSS, cv::Size(width, height));
        case 2: // ellipse
            return cv::getStructuringElement(cv::MORPH_ELLIPSE, cv::Size(width, height));
        case 3: // disk
        default:
            return [self strelDisk:width]; // radius is saved in width
    }
}

+(UIImage *) morphMethod: (UIImage *) image method:(int)method strelShape:(int)strelShape width:(int)width height:(int)height {
    cv::Mat srcImage;
    cv::Mat dstImage;
    cv::Mat strel;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4){
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    }
    
    strel = [self getStrel:strelShape :width :height];
    
    switch (method) {
        case 0: // erosion
            cv::morphologyEx(srcImage, dstImage, cv::MORPH_ERODE, strel); break;
        case 1: // dilation
            cv::morphologyEx(srcImage, dstImage, cv::MORPH_DILATE, strel); break;
        case 2: // opening
            cv::morphologyEx(srcImage, dstImage, cv::MORPH_OPEN, strel); break;
        case 3: // closing
            cv::morphologyEx(srcImage, dstImage, cv::MORPH_CLOSE, strel); break;
        case 4: // smoothing
            cv::morphologyEx(srcImage, dstImage, cv::MORPH_OPEN, strel);
            cv::morphologyEx(dstImage, dstImage, cv::MORPH_CLOSE, strel);
            break;
        case 5: // gradient
            cv::morphologyEx(srcImage, dstImage, cv::MORPH_GRADIENT, strel); break;
        default:
            srcImage.copyTo(dstImage);
    }
    
    return MatToUIImage(dstImage);
}

//  ============================================  S E G M E N T A T I O N  =======================================

int otsuThresholdValue = 0;

+(UIImage *) binaryThreshold: (UIImage *) image thresh:(int)thresh {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, dstImage, cv::COLOR_RGBA2GRAY);
    else if (srcImage.channels() == 3)
        cv::cvtColor(srcImage, dstImage, cv::COLOR_RGB2GRAY);
    else
        srcImage.copyTo(dstImage);
    
    cv::threshold(dstImage, dstImage, thresh, 255, cv::THRESH_BINARY);
    return MatToUIImage(dstImage);
}

+(UIImage *) otsuThreshold: (UIImage *) image {
    cv::Mat srcImage;
    cv::Mat dstImage;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, dstImage, cv::COLOR_RGBA2GRAY);
    else if (srcImage.channels() == 3)
        cv::cvtColor(srcImage, dstImage, cv::COLOR_RGB2GRAY);
    else
        srcImage.copyTo(dstImage);
    
    otsuThresholdValue = (int) cv::threshold(dstImage, dstImage, 0, 255, cv::THRESH_BINARY + cv::THRESH_OTSU);
    return MatToUIImage(dstImage);
}

+(int) getOtsuValue {
    return otsuThresholdValue;
}

+(UIImage *) houghCircle: (UIImage *) image minDistFactor:(int)minDistFactor cannyParam:(int)cannyParam minRadius:(int)minRadius maxRadius:(int)maxRadius {
    cv::Mat srcImage;
    cv::Mat dstImage;
    cv::Mat grayImage;
    std::vector<cv::Vec3f> circles;
    UIImageToMat(image, srcImage);
    
    if (srcImage.channels() == 4)
        cv::cvtColor(srcImage, srcImage, cv::COLOR_RGBA2RGB);
    srcImage.copyTo(dstImage);
    
    if (srcImage.channels() == 3)
        cv::cvtColor(srcImage, grayImage, cv::COLOR_RGB2GRAY);
    else
        srcImage.copyTo(grayImage);
    
    cv::medianBlur(grayImage, grayImage, 3);
    
    cv::HoughCircles(grayImage, circles, cv::HOUGH_GRADIENT, 1.0, (double)grayImage.rows / minDistFactor, cannyParam, 20, minRadius, maxRadius);
    for (size_t i = 0; i < circles.size(); i++) {
        cv::Vec3f c = circles[i];
        cv::Point center = cv::Point(c[0], c[1]);
        
        cv::circle(dstImage, center, 1, cv::Scalar(255, 100, 0), 4, 8, 0);
        int radius = (int) round(c[2]);
        cv::circle(dstImage, center, radius, cv::Scalar(255, 0, 0), 4, 8, 0);
    }
    
    return MatToUIImage(dstImage);
}

@end
