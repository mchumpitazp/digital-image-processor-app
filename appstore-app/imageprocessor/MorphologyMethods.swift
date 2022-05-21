//
//  MorphologyMethods.swift
//  imageprocessor
//
//  Created by Mauro Chumpitaz Polino on 17/12/21.
//

import Foundation

class MorphologyMethods {
    
    
    static func morphMethod (_ image : UIImage, _ method : Int32, _ strelShape : String, _ width : Int32, _ height : Int32) -> UIImage {
        let shape : Int32;
        
        if (strelShape == "Rect") {
            shape = 0
        }
        else if (strelShape == "Cross") {
            shape = 1
        }
        else if (strelShape == "Ellipse") {
            shape = 2
        }
        else {
            shape = 3
        }
        
        let outputImage = Wrapper.morphMethod(image, method: method, strelShape: shape, width: width, height: height);
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func morphMethod2 (_ image : UIImage, _ methodStr : String, _ strelShape : String, _ width : Int32, _ height : Int32) -> UIImage {
        let shape : Int32
        let method : Int32
        
        if (strelShape == "Rect") {
            shape = 0
        }
        else if (strelShape == "Cross") {
            shape = 1
        }
        else if (strelShape == "Ellipse") {
            shape = 2
        }
        else {
            shape = 3
        }
        
        if (methodStr == "Erosion") {
            method = 0
        }
        else if (methodStr == "Dilation") {
            method = 1
        }
        else if (methodStr == "Opening") {
            method = 2
        }
        else if (methodStr == "Closing") {
            method = 3
        }
        else if (methodStr == "Smoothing") {
            method = 4
        }
        else { method = 5 } // Gradient
        
        let outputImage = Wrapper.morphMethod(image, method: method, strelShape: shape, width: width, height: height);
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
}

