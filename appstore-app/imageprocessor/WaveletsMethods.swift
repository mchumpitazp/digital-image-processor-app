//
//  WaveletsMethods.swift
//  imageprocessor
//
//  Created by Mauro Chumpitaz Polino on 19.01.2022.
//

import Foundation

import Foundation

class WaveletsMethods : ProcessingController {

    static func haarForward (_ image : UIImage, _ compression : String) -> UIImage {
        let index : Int32
        
        if (compression == "L1 Approximation")      {index = 0}
        else if (compression == "L1 Horizontal")    {index = 1}
        else if (compression == "L1 Vertical")      {index = 2}
        else if (compression == "L1 Diagonal")      {index = 3}
        else if (compression == "L2 Approximation") {index = 4}
        else if (compression == "L2 Horizontal")    {index = 5}
        else if (compression == "L2 Vertical")      {index = 6}
        else if (compression == "L2 Diagonal")      {index = 7}
        else {index = 8}
        
        return Wrapper.haarForward(image, index: index)
    }
}
