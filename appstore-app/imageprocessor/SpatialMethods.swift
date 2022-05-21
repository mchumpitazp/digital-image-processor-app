//
//  SpatialMethods.swift
//  imageprocessor
//
//  Created by Mauro Chumpitaz Polino on 14/12/21.
//

import Foundation

class SpatialMethods {
    
    static func meanBlur (_ image : UIImage, _ width : Int32, _ height : Int32) -> UIImage {
        let outputImage = Wrapper.meanBlur(image, width: width, height: height)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func gaussianBlur (_ image : UIImage, _ width : Int32, _ height : Int32, _ sigma : Double) -> UIImage {
        let outputImage = Wrapper.gaussianBlur(image, width: width, height: height, sigma: sigma)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func spatialLaplacian (_ image : UIImage, _ kernelSize : Int32) -> UIImage {
        let outputImage = Wrapper.spatialLaplacian(image, kernelSize: kernelSize)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func firstDerivative (_ image : UIImage, _ kernelType : String) -> UIImage {
        let outputImage = Wrapper.firstDerivative(image, kernelType: kernelType)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
}
