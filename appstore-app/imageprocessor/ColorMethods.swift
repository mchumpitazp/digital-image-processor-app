//
//  ColorMethods.swift
//  imageprocessor
//
//  Created by Mauro Chumpitaz Polino on 17/12/21.
//

import Foundation

class ColorMethods {
    
    static func rgb2planes (_ image : UIImage, _ plane : String) -> UIImage {
        let numPlane : Int32
        
        if (plane == "Red") {
            numPlane = 0
        }
        else if (plane == "Green") {
            numPlane = 1
        }
        else {
            numPlane = 2
        }
        
        let outputImage = Wrapper.rgb2planes(image, planeRGB: numPlane)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func rgb2hsi (_ image : UIImage, _ plane : String) -> UIImage {
        let numPlane : Int32
        
        if (plane == "Hue") {
            numPlane = 0
        }
        else if (plane == "Saturation") {
            numPlane = 1
        }
        else {
            numPlane = 2
        }
        
        let outputImage = Wrapper.rgb2hsi(image, planeHSI: numPlane)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func graySlicing (_ image : UIImage, _ i1 : Int32, _ i2 : Int32, _ i3 : Int32) -> UIImage {
        let outputImage = Wrapper.graySlicing(image, intensity1: i1, intensity2: i2, intensity3: i3)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func gray2color (_ image : UIImage, _ freq : Double, _ shiftR : Double, _ shiftG : Double, _ shiftB : Double) -> UIImage {
        let outputImage = Wrapper.gray2color(image, frequency: freq, shiftRed: shiftR, shiftGreen: shiftG, shiftBlue: shiftB)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func colorBalance (_ image : UIImage, _ percent : Int32) -> UIImage {
        let outputImage = Wrapper.colorBalance(image, percent: percent)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
}

