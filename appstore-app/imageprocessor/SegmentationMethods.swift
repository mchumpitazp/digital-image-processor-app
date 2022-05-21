//
//  SegmentationMethods.swift
//  imageprocessor
//
//  Created by Mauro Chumpitaz Polino on 17/12/21.
//

import Foundation

class SegmentationMethods {
    
    static func binaryThreshold (_ image : UIImage, _ thresh : Int32) -> UIImage {
        return Wrapper.binaryThreshold(image, thresh: thresh)
    }
    
    static func otsuThreshold (_ image : UIImage) -> UIImage {
        return Wrapper.otsuThreshold(image)
    }
    
    static func getOtsuValue () -> Int32 {
        return Wrapper.getOtsuValue()
    }
    
    static func houghCircle (_ image:UIImage, _ minDistFactor:Int32, _ cannyParam:Int32, _ minRad:Int32, _ maxRad:Int32) -> UIImage {
        return Wrapper.houghCircle(image, minDistFactor: minDistFactor, cannyParam: cannyParam, minRadius: minRad, maxRadius: maxRad)
    }
}

