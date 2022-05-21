//
//  Wrapper.h
//  imageprocessor
//
//  Created by Mauro Chumpitaz Polino on 29/11/21.
//

#ifndef Wrapper_h
#define Wrapper_h

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface Wrapper : NSObject
+ (NSString *)openCVVsersionString;

+(UIImage *) toGrayscale : (UIImage *) image;
+(UIImage *) rotate : (UIImage *) image;
+(int) getImageInfo : (UIImage *) image;

+(Boolean) isColor : (UIImage *) image;

//    B A S I C     T R A N S F O R M S
+(UIImage *) negate : (UIImage *) image;
+(UIImage *) log: (UIImage *) image;
+(UIImage *) inverseLog: (UIImage *) image;
+(UIImage *) powerLaw: (UIImage *) image singleParam:(double) gamma;
+(UIImage *) contrastStretching: (UIImage *) image r1:(int)lowX s1:(int)highX r2:(int)lowY s2:(int)highY;
+(UIImage *) bitPlaneSlicing: (UIImage *) image bitPlane:(int)plane;
+(UIImage *) equalizeHist: (UIImage *) image;

//   S P A T I A L    D O M A I N
+(UIImage *) meanBlur: (UIImage *) image width:(int)width height:(int)height;
+(UIImage *) gaussianBlur: (UIImage *) image width:(int)width height:(int)height sigma:(double)sigma;
+(UIImage *) spatialLaplacian: (UIImage *) image kernelSize:(int)size;
+(UIImage *) firstDerivative: (UIImage *) image kernelType:(NSString *)type;

//   F R E Q U E N C Y   D O M A I N
+(UIImage *) freqIdeal: (UIImage *) image isLowpass:(bool)isLowpass cutOffFrequency:(int)D_o;
+(UIImage *) freqButterworth: (UIImage *) image isLowpass:(bool)isLowpass cutOffFrequency:(int)D_o n:(int)n;
+(UIImage *) freqGaussian: (UIImage *) image isLowpass:(bool)isLowpass cutOffFrequency:(int)D_o;
+(UIImage *) freqLaplacian: (UIImage *) image;

//   N O I S E    F I L T E R I N G
+(UIImage *) geometricMean: (UIImage *) image width:(int)width height:(int)height;
+(UIImage *) contraharmonicMean: (UIImage *) image powerQ:(double)Q width:(int)width height:(int)height;
+(UIImage *) medianFilter: (UIImage *) image kernelSize:(int)size;
+(UIImage *) minFilter: (UIImage *) image width:(int)width height:(int)height;
+(UIImage *) maxFilter: (UIImage *) image width:(int)width height:(int)height;
+(UIImage *) midpointFilter: (UIImage *) image width:(int)width height:(int)height;
+(UIImage *) alphaTrimmedMean: (UIImage *) image width:(int)width height:(int)height d:(int)d;

//   C O L O R    P R O C E S S I N G
+(UIImage *) rgb2planes: (UIImage *) image planeRGB:(int)plane;
+(UIImage *) rgb2hsi: (UIImage *) image planeHSI:(int)plane;
+(UIImage *) graySlicing: (UIImage *) image intensity1:(int)i1 intensity2:(int)i2 intensity3:(int)i3;
+(UIImage *) gray2color: (UIImage *) image frequency:(double)freq shiftRed:(double)shiftR shiftGreen:(double)shiftG shiftBlue:(double)shiftB;
+(UIImage *) colorBalance: (UIImage *) image percent:(int)percent;

//   W A V E L E T S
+(UIImage *) haarForward: (UIImage *) image index:(int)index;
/*
+(cv::Mat) haarLevel1Filter: (cv::Mat) srcImage isLowpass:(bool)isLowpass;
+(UIImage *) haarLevel1: (cv::Mat) srcImage isLowpassL1:(bool)isLowpassL1 isLowpassL2:(bool)isLowpassL2;
+(cv::Mat) haarLevel2Filter: (cv::Mat) srcImage isLowpass:(bool)isLowpass;
+(UIImage *) haarLevel2: (cv::Mat) srcImage isLowpassL1:(bool)isLowpassL1 isLowpassL2:(bool)isLowpassL2;
+(UIImage *) haarForwardFull: (UIImage *) image;
 */

//   M O R P H O L O G Y
+(UIImage *) morphMethod: (UIImage *) image method:(int)method strelShape:(int)strelShape width:(int)width height:(int)height;

//   S E G M E N T A T I O N
+(UIImage *) binaryThreshold: (UIImage *) image thresh:(int)thresh;
+(UIImage *) otsuThreshold: (UIImage *) image;
+(int) getOtsuValue;
+(UIImage *) houghCircle: (UIImage *) image minDistFactor:(int)minDistFactor cannyParam:(int)cannyParam minRadius:(int)minRadius maxRadius:(int)maxRadius;

@end

#endif /* Wrapper_h */


