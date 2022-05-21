//
//  BasicMethods.swift
//  imageprocessor
//
//  Created by Mauro Chumpitaz Polino on 1/12/21.
//

import Foundation

class BasicMethods : ProcessingController {
    
    static func negate (_ image : UIImage) -> UIImage {
        let outputImage = Wrapper.negate(image)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func log (_ image : UIImage) -> UIImage {
        let outputImage = Wrapper.log(image)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func inverseLog (_ image : UIImage) -> UIImage {
        let outputImage = Wrapper.inverseLog(image)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func powerLaw (_ image : UIImage, _ gamma : Double) -> UIImage {
        let outputImage = Wrapper.powerLaw(image, singleParam: gamma)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func contrastStretching (_ image : UIImage, _ r1 : Int32, _ s1 : Int32, _ r2 : Int32, _ s2 : Int32) -> UIImage {
        let outputImage = Wrapper.contrastStretching(image, r1: r1, s1: s1, r2: r2, s2: s2)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func bitPlaneSlicing (_ image : UIImage, _ plane : Int32) -> UIImage {
        let outputImage = Wrapper.bitPlaneSlicing(image, bitPlane: plane)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func equilizeHist (_ image : UIImage) -> UIImage {
        let outputImage = Wrapper.equalizeHist(image)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
}
