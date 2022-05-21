//
//  ViewController.swift
//  imageprocessor
//
//  Created by Mauro Chumpitaz Polino on 7/11/21.
//

import UIKit

class ViewController: UIViewController {
    
    @IBOutlet var galleryView: UIImageView!
    @IBOutlet var realTimeView: UIImageView!
    

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        galleryView.layer.masksToBounds = true
        galleryView.layer.borderWidth = 2
        galleryView.layer.borderColor = UIColor.black.cgColor
        galleryView.layer.cornerRadius = 20
        
        realTimeView.layer.masksToBounds = true
        realTimeView.layer.borderWidth = 2
        realTimeView.layer.borderColor = UIColor.black.cgColor
        realTimeView.layer.cornerRadius = 20
        
        let tapGalleryRecognizer = UITapGestureRecognizer(target: self, action: #selector(didTapGalleryView(_:)))
        
        galleryView.addGestureRecognizer(tapGalleryRecognizer)
        
        let tapRealTimeRecognizer = UITapGestureRecognizer(target: self, action: #selector(didTapRealTimeView(_:)))
        
        realTimeView.addGestureRecognizer(tapRealTimeRecognizer)
        
    }
    
    @objc func didTapGalleryView (_ sender: UITapGestureRecognizer) {
        self.performSegue(withIdentifier: "HomeToProcessingSegue", sender: true)
    }
    
    @objc func didTapRealTimeView (_ sender: UITapGestureRecognizer) {
        self.performSegue(withIdentifier: "HomeToProcessingSegue", sender: false)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let vc = segue.destination as? ProcessingController {
            vc.isGalleryMode = sender as? Bool
        }
    }


}

