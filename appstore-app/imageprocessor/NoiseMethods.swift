//
//  NoiseMethods.swift
//  imageprocessor
//
//  Created by Mauro Chumpitaz Polino on 17/12/21.
//

import Foundation

class NoiseMethods {
    
    static func geometricMean (_ image : UIImage, _ width : Int32, _ height : Int32) -> UIImage {
        let outputImage = Wrapper.geometricMean(image, width: width, height: height)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func contraharmonicMean (_ image : UIImage, _ Q : Double, _ width : Int32, _ height : Int32) -> UIImage {
        let outputImage = Wrapper.contraharmonicMean(image, powerQ: Q, width: width, height: height)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func medianFilter (_ image : UIImage, _ kernelSize : Int32) -> UIImage {
        let outputImage = Wrapper.medianFilter(image, kernelSize: kernelSize)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func minFilter (_ image : UIImage, _ width : Int32, _ height : Int32) -> UIImage {
        let outputImage = Wrapper.minFilter(image, width: width, height: height)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func maxFilter (_ image : UIImage, _ width : Int32, _ height : Int32) -> UIImage {
        let outputImage = Wrapper.maxFilter(image, width: width, height: height)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func midpointFilter (_ image : UIImage, _ width : Int32, _ height : Int32) -> UIImage {
        let outputImage = Wrapper.midpointFilter(image, width: width, height: height)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func alphaTrimmedFilter (_ image : UIImage, _ width : Int32, _ height : Int32, _ d : Int32) -> UIImage {
        let outputImage = Wrapper.alphaTrimmedMean(image, width: width, height: height, d: d)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
}

