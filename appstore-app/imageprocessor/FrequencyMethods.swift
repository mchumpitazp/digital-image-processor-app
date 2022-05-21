//
//  FrequencyMethods.swift
//  imageprocessor
//
//  Created by Mauro Chumpitaz Polino on 17/12/21.
//

import Foundation

class FrequencyMethods {
    
    static func freqIdeal (_ image: UIImage, _ isLowpass: Bool, _ cutOffFrequency : Int32) -> UIImage {
        let outputImage = Wrapper.freqIdeal(image, isLowpass: isLowpass, cutOffFrequency: cutOffFrequency)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func freqButterworth (_ image: UIImage, _ isLowpass: Bool, _ cutOffFrequency : Int32, _ n : Int32) -> UIImage {
        let outputImage = Wrapper.freqButterworth(image, isLowpass: isLowpass, cutOffFrequency: cutOffFrequency, n: n)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func freqGaussian (_ image: UIImage, _ isLowpass: Bool, _ cutOffFrequency : Int32) -> UIImage {
        let outputImage = Wrapper.freqGaussian(image, isLowpass: isLowpass, cutOffFrequency: cutOffFrequency)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
    
    static func freqLaplacian (_ image: UIImage) -> UIImage {
        let outputImage = Wrapper.freqLaplacian(image)
        return UIImage(cgImage: (outputImage?.cgImage)!, scale: image.scale, orientation: image.imageOrientation)
    }
}

