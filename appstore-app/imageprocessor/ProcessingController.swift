//
//  ProcessingController.swift
//  imageprocessor
//
//  Created by Mauro Chumpitaz Polino on 7/11/21.
//

import UIKit
import AVFoundation

class ProcessingController: UIViewController, AVCaptureVideoDataOutputSampleBufferDelegate {
    
    //==============================================================================================
    //====================================== I N T E R F A C E =====================================
    //==============================================================================================
    
    @IBOutlet var userView : UIImageView!
    
    // Header
    @IBOutlet var btnBackHome:     UILabel!
    @IBOutlet var imageInfoHeader: UILabel!
    var resolutionTextField : UITextField!
    var resolutionChanging : Bool = false
    @IBOutlet var btnToGray:       UILabel!
    
    // Toolbar
    @IBOutlet var btnTracking:  UIButton!
    @IBOutlet var btnAddMethod: UIButton!
    @IBOutlet var btnSaveImage: UIButton!
    
    // Tracking
    @IBOutlet var SVtracking: UIStackView!
    @IBOutlet var btnRemoveAll  : UILabel!
    @IBOutlet var btnRemoveAll2 : UILabel!
    @IBOutlet var TVtracking: UITableView!
    @IBOutlet var trackingEmpty : UILabel!
    
    // Set of categories
    @IBOutlet var SVsetOfCategories     : UIStackView!
    @IBOutlet var basicMethods          : UIStackView!
    @IBOutlet var spatialMethods        : UIStackView!
    @IBOutlet var frequencyMethods      : UIStackView!
    @IBOutlet var noiseMethods          : UIStackView!
    @IBOutlet var colorMethods          : UIStackView!
    @IBOutlet var waveletsMethods       : UIStackView!
    @IBOutlet var morphologyMethods     : UIStackView!
    @IBOutlet var segmentationMethods   : UIStackView!
    
    // Set of methods
    @IBOutlet var SVsetOfMethods       : UIStackView!
    @IBOutlet var SVsetOfMethodsHeader : UIStackView!
    @IBOutlet var TVsetOfMethods       : UITableView!
    @IBOutlet var selectedCategoryName : UILabel!
    
    // Mathematical parameters
    @IBOutlet var SVmathParams : UIStackView!
    
    // - header
    @IBOutlet var btnCancelMethod : UILabel!
    @IBOutlet var MathParams_methodName : UILabel!
    @IBOutlet var btnApplyMethod  : UILabel!
    
    // - extra comment
    @IBOutlet var extraComment : UILabel!
    
    // - first spinner
    @IBOutlet var firstSpinner: UIStackView!
    @IBOutlet var firstSpinnerText: UILabel!
    @IBOutlet var firstSpinnerImage : UIButton!
    @IBOutlet var firstSpinnerField: UITextField!
    
    // - kernel bars
    @IBOutlet var kernelBars      : UIStackView!
    @IBOutlet var kernelProgress  : UILabel!
    @IBOutlet var kernelHeightBar : UISlider!
    @IBOutlet var kernelMinValue  : UILabel!
    @IBOutlet var kernelMaxValue  : UILabel!
    @IBOutlet var kernelWidthBar  : UISlider!
    
    // - first bar
    @IBOutlet var firstBar         : UIStackView!
    @IBOutlet var firstBarName     : UILabel!
    @IBOutlet var firstBarMinValue : UILabel!
    @IBOutlet var firstBarProgress : UILabel!
    @IBOutlet var firstBarMaxValue : UILabel!
    @IBOutlet var firstBarBar      : UISlider!
    
    // - second bar
    @IBOutlet var secondBar         : UIStackView!
    @IBOutlet var secondBarName     : UILabel!
    @IBOutlet var secondBarMinValue : UILabel!
    @IBOutlet var secondBarProgress : UILabel!
    @IBOutlet var secondBarMaxValue : UILabel!
    @IBOutlet var secondBarBar      : UISlider!
    
    // - third bar
    @IBOutlet var thirdBar         : UIStackView!
    @IBOutlet var thirdBarName     : UILabel!
    @IBOutlet var thirdBarMinValue : UILabel!
    @IBOutlet var thirdBarProgress : UILabel!
    @IBOutlet var thirdBarMaxValue : UILabel!
    @IBOutlet var thirdBarBar      : UISlider!
    
    // - fourth bar
    @IBOutlet var fourthBar         : UIStackView!
    @IBOutlet var fourthBarName     : UILabel!
    @IBOutlet var fourthBarMinValue : UILabel!
    @IBOutlet var fourthBarProgress : UILabel!
    @IBOutlet var fourthBarMaxValue : UILabel!
    @IBOutlet var fourthBarBar      : UISlider!
    
    
    //==============================================================================================
    //========================== I N T E R F A C E    V A R I A B L E S ============================
    //==============================================================================================
    
    // Tracking
    
    // Set of methods
    
    var selectedCategoryArray = [String]()
    let basicMethodsArray = ["Negative", "Log", "Inverse log", "Power-law", "Contrast stretching", "Bit-plane slicing", "Histogram equalization"]
    let spatialMethodsArray = ["Mean blur", "Gaussian blur", "Laplacian sharp", "Gradient sharp"]
    let frequencyMethodsArray = ["Ideal lowpass", "Ideal highpass", "Butterworth lowpass", "Butterworth highpass", "Gaussian lowpass", "Gaussian highpass", "Laplacian"]
    let noiseMethodsArray = ["Geometric mean", "Contraharmonic mean", "Median", "Min", "Max", "Midpoint"]
    let colorMethodsArray = ["RGB planes", "RGB to HSI", "Intensity slicing", "Intensity to color", "Color balance"]
    let waveletMethodsArray = ["Haar transform"]
    let morphologyMethodsArray = ["Erosion", "Dilation", "Opening", "Closing", "Smoothing", "Gradient"]
    let segmentationMethodsArray = ["Binary threshold", "Otsu's threshold", "Hough circle"]
    
    // Mathematical parameters
    
    // - first spinner
    var spinnerPickedText: String = ""
    var pickerView = UIPickerView()
    
    var firstSpinnerArray = [String]()
        
    
    //==============================================================================================
    //=============================== G L O B A L    V A R I A B L E S =============================
    //==============================================================================================
    
    // Workspace variables
    var isGalleryMode : Bool!
    
    // User Image variables
    var srcUserImage : UIImage!
    var currentImage : UIImage!
    var processImage : UIImage!
    
    // Tracking variables
    
    // Streaming variables
    var captureSession  = AVCaptureSession()
    let videoDataOutput = AVCaptureVideoDataOutput()
    
    // Image processing variables
    var resolutionForChange : Bool = true
    var trackingDictionary = [String : [Int32]]()
    var trackingArray = [String]()
    
    
    //==============================================================================================
    //========================================== M A I N ===========================================
    //==============================================================================================
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        btnTracking.layer.cornerRadius = btnTracking.frame.height / 2
        btnAddMethod.layer.cornerRadius = btnAddMethod.frame.height / 2
        btnSaveImage.layer.cornerRadius = btnSaveImage.frame.height / 2
        
        setGlobalOnClickListeners()
        
        if (isGalleryMode) {
            pickImage()
        }
        else {
            self.addCameraInput(resolution: 2)
            self.getFrames()
            self.captureSession.startRunning()
        }
    }
    
    //==============================================================================================
    //======================== S E T    G L O B A L    O N    C L I C K ============================
    //==============================================================================================
    
    //---------------------------- S U P P O R T    F U N C T I O N S ------------------------------
    
    private func getToolbarVisibilityCode () -> Int {
        if (!SVtracking.isHidden)               {return 1}
        else if (!SVsetOfCategories.isHidden
                 || !SVsetOfMethods.isHidden)   {return 2}
        else if (!SVmathParams.isHidden)        {return 3}
        else                                    {return 0}
    }
    
    public func toast (message : String) {
        DispatchQueue.main.async {
            let toastLabel = UILabel(frame: CGRect(x: self.view.frame.size.width/2 - 165, y: self.view.frame.size.height/2, width: 330, height: 35))
            toastLabel.backgroundColor = UIColor.black.withAlphaComponent(0.6)
            toastLabel.textColor = UIColor.white
            toastLabel.textAlignment = .center;
            toastLabel.font = UIFont(name: "Montserrat-Light", size: 12.0)
            toastLabel.text = message
            toastLabel.alpha = 1.0
            toastLabel.layer.cornerRadius = 10;
            toastLabel.clipsToBounds  =  true
            self.view.addSubview(toastLabel)
            UIView.animate(withDuration: 1.0, delay: 2.0, options: .curveEaseOut, animations: {
                toastLabel.alpha = 0.0
            }, completion: {(isCompleted) in
                toastLabel.removeFromSuperview()
            })
        }
    }
    
    private func hideMathParameters () {
        baseInterface()
        DispatchQueue.main.async {
            self.firstSpinner.isHidden = true
            self.kernelBars.isHidden   = true
            self.firstBar.isHidden     = true
            self.secondBar.isHidden    = true
            self.thirdBar.isHidden     = true
            self.fourthBar.isHidden    = true
            self.extraComment.isHidden = true
        }
    }
    
    private func setNullParametersListeners () {
        DispatchQueue.main.async {
            self.firstSpinnerField.removeTarget(nil, action: nil, for: .valueChanged)
            self.kernelWidthBar.removeTarget(nil, action: nil, for: .valueChanged)
            self.kernelHeightBar.removeTarget(nil, action: nil, for: .valueChanged)
            self.firstBarBar.removeTarget(nil, action: nil, for: .valueChanged)
            self.secondBarBar.removeTarget(nil, action: nil, for: .valueChanged)
            self.thirdBarBar.removeTarget(nil, action: nil, for: .valueChanged)
            self.fourthBarBar.removeTarget(nil, action: nil, for: .valueChanged)
        }
    }
    
    private func attachIconToLabel (_ label : UILabel!, _ systemIcon : String, _ attachLeft : Bool, _ offSet : Float) {
        let imageAttachment = NSTextAttachment()
        imageAttachment.image = UIImage(systemName: systemIcon)
        
        let imageOffsetY: CGFloat = CGFloat(offSet)
        imageAttachment.bounds = CGRect(x: 0, y: imageOffsetY, width: imageAttachment.image!.size.width, height: imageAttachment.image!.size.height)
        
        let attachmentString = NSAttributedString(attachment: imageAttachment)
        
        let completeText = NSMutableAttributedString(string: "")
        let textwithIcon = NSAttributedString(string: label.text!)
        
        if attachLeft {
            completeText.append(attachmentString)
            completeText.append(textwithIcon)
        }
        else {
            completeText.append(textwithIcon)
            completeText.append(attachmentString)
        }
        
        label.textAlignment = .center
        label.attributedText = completeText
    }
    
    private func setImageInfo (_ image : UIImage!) {
        let heightInPoints = image.size.height
        let heightInPixels = Int(heightInPoints * image.scale)
        let widthInPoints  = image.size.width
        let widthInPixels  = Int(widthInPoints * image.scale)
        let imageResolution = String(heightInPixels) + " x " + String(widthInPixels)
        let color = (Wrapper.isColor(image)) ? "RGB" : "Gray"
        self.imageInfoHeader.text = imageResolution + " | " + color
        self.attachIconToLabel(self.imageInfoHeader, "gearshape.fill", false, -3.0)
    }
    
    //-------------------------------- M A I N    F U N C T I O N ----------------------------------
    
    private func setGlobalOnClickListeners () {
        
        // Header
        
        attachIconToLabel(self.btnBackHome, "arrow.backward", true, -2.0)
        attachIconToLabel(self.btnToGray, "paintbrush.pointed.fill", false, -5.0)
        let tapBackHome = UITapGestureRecognizer(target: self, action: #selector(OnClickBackHome))
        btnBackHome.addGestureRecognizer(tapBackHome)
        let tapResolution = UITapGestureRecognizer(target: self, action: #selector(OnClickResolution))
        imageInfoHeader.addGestureRecognizer(tapResolution)
        let tapToGray = UITapGestureRecognizer(target: self, action: #selector(OnClickToGray))
        btnToGray.addGestureRecognizer(tapToGray)
        
        // Tracking
        
        attachIconToLabel(self.btnRemoveAll, "trash", true, -4.0)
        TVtracking.delegate = self
        TVtracking.dataSource = self
        TVtracking.backgroundColor = UIColor.clear
        let tapRemoveTracking = UITapGestureRecognizer(target: self, action: #selector(OnClickRemoveTracking))
        btnRemoveAll.addGestureRecognizer(tapRemoveTracking)
        
        // Set of methods
        
        setTapOnMethodCategory(basicMethods, "Basic Transforms", basicMethodsArray)
        setTapOnMethodCategory(spatialMethods, "Filtering in spatial domain", spatialMethodsArray)
        setTapOnMethodCategory(frequencyMethods, "Filtering in frequency domain", frequencyMethodsArray)
        setTapOnMethodCategory(noiseMethods, "Mean and order-statistic filters", noiseMethodsArray)
        setTapOnMethodCategory(colorMethods, "Color processing", colorMethodsArray)
        setTapOnMethodCategory(waveletsMethods, "Wavelets", waveletMethodsArray)
        setTapOnMethodCategory(morphologyMethods, "Morphology", morphologyMethodsArray)
        setTapOnMethodCategory(segmentationMethods, "Segmentation", segmentationMethodsArray)
        
        let tapMethodCategory = UITapGestureRecognizer(target: self, action: #selector(OnClickMethodCategoryTop))
        tapMethodCategory.cancelsTouchesInView = false
        SVsetOfMethodsHeader.addGestureRecognizer(tapMethodCategory)
        SVsetOfMethodsHeader.isUserInteractionEnabled = true
        
        TVsetOfMethods.delegate = self
        TVsetOfMethods.dataSource = self
        TVsetOfMethods.backgroundColor = UIColor.clear
        
        // Processing buttons
        
        attachIconToLabel(self.btnCancelMethod, "xmark", true, -4.0)
        attachIconToLabel(self.btnApplyMethod, "checkmark", false, -4.0)
        
        let tapCancelMethod = UITapGestureRecognizer(target: self, action: #selector(OnClickCancelMethod))
        btnCancelMethod.addGestureRecognizer(tapCancelMethod)
        
        let tapApplyMethod = UITapGestureRecognizer(target: self, action: #selector(OnClickApplyMethod))
        btnApplyMethod.addGestureRecognizer(tapApplyMethod)
        
        // First Spinner
        pickerView.delegate = self
        pickerView.dataSource = self
        firstSpinnerField.inputView = pickerView
    }
    
    // Header - Selectors
    
    @objc func OnClickBackHome () {
        self.performSegue(withIdentifier: "ProcessingToHomeSegue", sender: self)
    }
    
    @objc func OnClickResolution () {
        
        if (!isGalleryMode) {
            if (SVmathParams.isHidden) {
                let resolutionsArray = ["3840 x 2160", "1920 x 1080", "1280 x 720", "640 x 480"]
                firstSpinnerArray = resolutionsArray
                resolutionChanging = true
                
                if resolutionTextField == nil {
                    self.resolutionTextField = UITextField(frame:.zero)
                    resolutionTextField.inputView = self.pickerView
                    self.view.addSubview(resolutionTextField)
                }
                resolutionTextField.becomeFirstResponder()
            }
            else {
                toast(message: "Finish the method in progress")
            }
        }
        else {
            toast(message: "Resolution only changes in streaming")
        }
    }
    
    @objc func OnClickToGray () {
        if (getToolbarVisibilityCode() == 3) {
            toast(message: "Finish the method in progress")
        }
        else {
            if (isGalleryMode) {
                if (!Wrapper.isColor(currentImage)) {
                    toast(message: "Image already in gray")
                }
                else {
                    trackingArray.append("To grayscale")
                    self.TVtracking.reloadData()
                    self.trackingEmpty.isHidden = true
                    self.TVtracking.isHidden = false
                    
                    currentImage = Wrapper.toGrayscale(currentImage)
                    self.setImageInfo(currentImage)
                    self.userView.image = currentImage
                }
            }
            else {
                if (trackingArray.contains("To grayscale")) {
                    toast(message: "Image already in gray")
                }
                else {
                    trackingArray.append("To grayscale")
                    self.TVtracking.reloadData()
                    self.trackingEmpty.isHidden = true
                    self.TVtracking.isHidden = false
                }
            }
        }
        
    }
    
    // Tracking - Selectors
    
    @objc func OnClickRemoveTracking () {
        self.TVtracking.isHidden = true
        self.trackingEmpty.isHidden = false
        trackingArray.removeAll()
        trackingDictionary.removeAll()
        TVtracking.reloadData()
        
        if (isGalleryMode) {
            setImageInfo(srcUserImage)
            userView.image = srcUserImage
            currentImage   = srcUserImage
            processImage   = srcUserImage
        }
    }
 
    class MyTapGestureRecognizer: UITapGestureRecognizer {
        var methodsArray = [String]()
        var categoryName = String()
    }
    
    func setTapOnMethodCategory (_ categorySV: UIStackView, _ categoryName: String, _ methodsArray: [String]) {
        
        let tap = MyTapGestureRecognizer(target: self, action: #selector(OnClickMethodCategory(sender:)))
        tap.cancelsTouchesInView = false
        tap.categoryName = categoryName
        tap.methodsArray = methodsArray
        categorySV.addGestureRecognizer(tap)
        categorySV.isUserInteractionEnabled = true
    }
    
    @objc func OnClickMethodCategory (sender : MyTapGestureRecognizer) {
        selectedCategoryName.text = "   " + sender.categoryName
        selectedCategoryArray = sender.methodsArray
        SVsetOfCategories.isHidden = true
        SVsetOfMethods.isHidden = false
        TVsetOfMethods?.reloadData()
    }
    
    @objc func OnClickMethodCategoryTop () {
        SVsetOfCategories.isHidden = false
        SVsetOfMethods.isHidden = true
    }
    
    @objc func OnClickCancelMethod () {
        hideMathParameters()
        setNullParametersListeners()
        
        let method = trackingArray.removeLast()
        trackingDictionary.removeValue(forKey: method)
        
        if (isGalleryMode) {
            setImageInfo(currentImage)
            userView.image = currentImage
        }
    }
    
    @objc func OnClickApplyMethod () {
        hideMathParameters()
        setNullParametersListeners()
        
        SVsetOfMethods.isHidden = true
        btnAddMethod.setImage(UIImage(systemName: "plus"), for: .normal)
        
        if (isGalleryMode) {
            currentImage = processImage
        }
    }
    
    // B O T T O M    T O O L B A R
    
    @IBAction func OnClickBtnTracking(_ sender: UIButton) {
        TVtracking.reloadData()
        
        switch (getToolbarVisibilityCode()) {
            
        case 0: // Everything is closed
            if (trackingArray.isEmpty) {
                self.trackingEmpty.isHidden = false
                self.TVtracking.isHidden = true
            }
            else {
                self.trackingEmpty.isHidden = true
                self.TVtracking.isHidden = false
            }
            self.SVtracking.isHidden = false
        case 1: // Tracking is open
            self.SVtracking.isHidden = true
        case 2: // Set of methods is open
            if (trackingArray.isEmpty) {
                self.trackingEmpty.isHidden = false
                self.TVtracking.isHidden = true
            }
            else {
                self.trackingEmpty.isHidden = true
                self.TVtracking.isHidden = false
            }
            self.SVsetOfCategories.isHidden  = true
            self.SVsetOfMethods.isHidden = true
            btnAddMethod.setImage(UIImage(systemName: "plus"), for: .normal)
            self.SVtracking.isHidden = false
        case 3: // Parameters are open
            toast(message: "Apply or cancel the method in progress")
        default:
            toast(message: "Error: Tracking case not found")
        }
    }
    
    @IBAction func OnClickBtnAddMethod(_ sender: UIButton) {
        switch (getToolbarVisibilityCode()) {
            
        case 0: // Everything is closed
            SVsetOfCategories.isHidden = false
            btnAddMethod.setImage(UIImage(systemName: "minus"), for: .normal)
            break
        case 1: // Tracking is open
            SVtracking.isHidden = true
            SVsetOfCategories.isHidden = false
            btnAddMethod.setImage(UIImage(systemName: "minus"), for: .normal)
            break
        case 2: // Set of categories or methods is open
            SVsetOfCategories.isHidden = true
            SVsetOfMethods.isHidden = true
            btnAddMethod.setImage(UIImage(systemName: "plus"), for: .normal)
            break
        case 3: // Mathematical parameters are open
            toast(message: "Finish the method in progress")
            break
        default:
            toast(message: "BtnAddMethod Error")
        }
    }
    
    
    @IBAction func OnClickBtnSave(_ sender: UIButton) {
        UIImageWriteToSavedPhotosAlbum(self.userView.image!, self, #selector(imageSaved(_:didFinishSavingWithError:contextInfo:)), nil)
    }
    
    @objc func imageSaved(_ image: UIImage,
            didFinishSavingWithError error: Error?, contextInfo: UnsafeRawPointer) {
        if let error = error {
            print("ERROR: \(error)")
        }
        else {
            toast(message: "Image saved")
        }
    }
    
    
    //==============================================================================================
    //================================= M A T H     I N T E R F A C E ==============================
    //==============================================================================================

    private func baseInterface () {
        DispatchQueue.main.async {
            if (self.SVmathParams.isHidden) {
                self.SVsetOfMethods.isHidden = true
                self.SVmathParams.isHidden = false
            }
            else {
                self.SVsetOfMethods.isHidden = false
                self.SVmathParams.isHidden = true
            }
        }
    }
    
    private func displayBar (_ ll : UIStackView!, barName : UILabel!, bar : UISlider!,
                             _ name : String, _ min : Int, _ progress : Int, _ max : Int) {
        ll.isHidden = false
        barName.text = name
        bar.minimumValue = Float(min)
        bar.maximumValue = Float(max)
        bar.setValue(Float(progress), animated: true)
    }
    
    private func setBarLimits (_ minValue : UILabel!, _ progressValue : UILabel!, _ maxValue : UILabel!,
                               _ type : String, _ min : Int, _ progress : Int, _ max : Int) {
        var minText      : String
        var progressText : String
        var maxText      : String
        
        switch type {
        case "direct":
            minText      = String(min)
            progressText = String(progress)
            maxText      = String(max)
            break
        case "double":
            minText      = String(Float(min) / 10)
            progressText = String(Float(progress) / 10)
            maxText      = String(Float(max) / 10)
            break
        case "kernel":
            minText      = String(min * 2 + 1)
            progressText = String(progress * 2 + 1)
            maxText      = String(max * 2 + 1)
            break
        case "norm_freq":
            minText      = "0"
            progressText = "0.500"
            maxText      = "1"
            break
        default:
            minText      = "No init"
            progressText = "No init"
            maxText      = "No init"
        }
        
        minValue.text      = minText
        progressValue.text = progressText
        maxValue.text      = maxText
    }
    
    private func intFirstBar (_ type : String, _ name : String, _ min : Int, _ max : Int, _ progress : Int) {
        displayBar(firstBar, barName: firstBarName, bar: firstBarBar, name, min, progress, max)
        setBarLimits(firstBarMinValue, firstBarProgress, firstBarMaxValue, type, min, progress, max)
    }
    
    private func intSecondBar (_ type : String, _ name : String, _ min : Int, _ max : Int, _ progress : Int) {
        displayBar(secondBar, barName: secondBarName, bar: secondBarBar, name, min, progress, max)
        setBarLimits(secondBarMinValue, secondBarProgress, secondBarMaxValue, type, min, progress, max)
    }
    
    private func intThirdBar (_ type : String, _ name : String, _ min : Int, _ max : Int, _ progress : Int) {
        displayBar(thirdBar, barName: thirdBarName, bar: thirdBarBar, name, min, progress, max)
        setBarLimits(thirdBarMinValue, thirdBarProgress, thirdBarMaxValue, type, min, progress, max)
    }
    
    private func intFourthBar (_ type : String, _ name : String, _ min : Int, _ max : Int, _ progress : Int) {
        displayBar(fourthBar, barName: fourthBarName, bar: fourthBarBar, name, min, progress, max)
        setBarLimits(fourthBarMinValue, fourthBarProgress, fourthBarMaxValue, type, min, progress, max)
    }
    
    private func intKernelBar (_ min : Int, _ max : Int, _ progress : Int) {
        let minText  = String(min * 2 + 1)
        let maxText  = String(max * 2 + 1)
        var progText = String(progress * 2 + 1)
        progText += " x " + progText
        
        kernelBars.isHidden = false
        kernelHeightBar.minimumValue = Float(min)
        kernelWidthBar.minimumValue  = Float(min)
        kernelHeightBar.maximumValue = Float(max)
        kernelWidthBar.maximumValue  = Float(max)
        kernelHeightBar.value = Float(progress)
        kernelWidthBar.value  = Float(progress)
        
        kernelMinValue.text = minText
        kernelMaxValue.text = maxText
        kernelProgress.text = progText
    }
    
    private func intFirstSpinner (_ spinnerText : String) {
        firstSpinner.isHidden = false
        firstSpinnerText.text = spinnerText
        firstSpinnerImage.isHidden = true
    }
      
    
    //==============================================================================================
    //============================= M E T H O D S    O N    C L I C K ==============================
    //==============================================================================================
    
    
    //---------------------------- S U P P O R T    F U N C T I O N S ------------------------------

    private func processInGalleryMode () {
        if (isGalleryMode) {
            processImage = imageProcessing(inputImage: currentImage)
            userView.image = processImage
            setImageInfo(processImage)
        }
    }
    
    private func mapMethodValues (_ i1 : Int = -1, _ i2 : Int = -1, _ i3 : Int = -1, _ i4 : Int = -1) {
        var valuesArray = [Int32]()
        let method = MathParams_methodName.text!
        
        if (i1 != -1) { valuesArray.append(Int32(i1))}
        if (i2 != -1) { valuesArray.append(Int32(i2))}
        if (i3 != -1) { valuesArray.append(Int32(i3))}
        if (i4 != -1) { valuesArray.append(Int32(i4))}
        
        trackingDictionary[method] = valuesArray
    }
    
    private func setSliderChangeListener (_ sliderNumber : Int, _ dataType : String, _ arrayIndex : Int) {
        let sliderBar : UISlider!
        
        switch (sliderNumber) {
        case 1:  sliderBar = firstBarBar
        case 2:  sliderBar = secondBarBar
        case 3:  sliderBar = thirdBarBar
        default: sliderBar = fourthBarBar
        }
        
        sliderBar.tag = sliderNumber
        sliderBar.accessibilityHint = dataType
        sliderBar.accessibilityValue = String(arrayIndex)
        sliderBar.addTarget(self, action: #selector(sliderBarChanged(sender:)), for: .valueChanged)
    }
    
    @objc func sliderBarChanged (sender : UISlider!) {
        // Passed variables
        let sliderNumber = sender.tag
        let dataType     = sender.accessibilityHint
        let arrayIndex   = Int(sender.accessibilityValue!)!
        
        // Working variables
        let method = MathParams_methodName.text!
        var newValue : Int = Int(sender.value)
        var strValue : String = "_"
        let sliderProgress : UILabel!
        
        switch dataType {
        case "int":    strValue = String(newValue)
        case "double": strValue = String(Double(newValue) / 10)
        case "kernel":
            newValue = newValue * 2 + 1
            strValue = String(newValue)
        case "norm_freq":
            var normalizedValue = Float(newValue) / sender.maximumValue
            normalizedValue = round(normalizedValue * 1000) / 1000
            strValue = String(normalizedValue)
        default: toast(message: "Error: Slider data type not found")
        }
        
        switch sliderNumber {
        case 1:  sliderProgress = firstBarProgress
        case 2:  sliderProgress = secondBarProgress
        case 3:  sliderProgress = thirdBarProgress
        default: sliderProgress = fourthBarProgress
        }
        
        sliderProgress.text = strValue
        trackingDictionary[method]![arrayIndex] = Int32(newValue)
        
        processInGalleryMode()
    }
    
    private func setKernelBarsChangeListener (_ methodName : String, _ min : Int, _ max : Int, _ skip : Bool = false) {
        if (!skip) {
            let value = min * 2 + 1
            mapMethodValues(value, value)
            intKernelBar(min, max, min)
        }
        
        kernelHeightBar.tag = 0
        kernelHeightBar.addTarget(self, action: #selector(kernelBarsChanged(sender:)), for: .valueChanged)
        
        kernelWidthBar.tag = 1
        kernelWidthBar.addTarget(self, action: #selector(kernelBarsChanged(sender:)), for: .valueChanged)
    }
    
    @objc func kernelBarsChanged (sender : UISlider!) {
        let methodName = MathParams_methodName.text!
        let index = sender.tag
        
        var height = Int32(sender.value) * 2 + 1
        var width  = trackingDictionary[methodName]![index]
        
        if (index == 1) {
            (height, width) = (width, height)
            trackingDictionary[methodName]![0] = width
        }
        else {
            trackingDictionary[methodName]![1] = height
        }
        
        kernelProgress.text = String(height) + " x " + String(width)
        
        processInGalleryMode()
    }
    
    @objc func contraHarmonicChanged (sender : UISlider!) {
        let value = Int32(sender.value)
        
        switch value {
        case -10: MathParams_methodName.text = "Harmonic mean"
        case 0:   MathParams_methodName.text = "Arithmetic mean"
        default:  MathParams_methodName.text = "Contraharmonic mean"
        }
        
        let d1 = Float(value) / 10
        firstBarProgress.text = String(d1)
        trackingDictionary["Contraharmonic mean"]![2] = value
        
        processInGalleryMode()
    }
    
    @objc func medianChanged (sender : UISlider!) {
        let value = Int32(sender.value) * 2 + 1
        firstBarProgress.text = String(value)
        trackingDictionary["Median"]![0] = value
        
        processInGalleryMode()
    }
    
    //-------------------------------- M A I N    F U N C T I O N ----------------------------------
    
    private func showMethodInterface (_ methodName : String) {
        MathParams_methodName.text = methodName
        trackingArray.append(MathParams_methodName.text!)
        baseInterface()
        
        switch methodName {
            
        case "To grayscale", "Negative", "Log", "Inverse log", "Histogram equalization":
            trackingDictionary[methodName] = nil
            
        case "Power-law":
            intFirstBar("double", "Gamma", 1, 70, 10)
            mapMethodValues(10)
            setSliderChangeListener(1, "double", 0)
            
        case "Contrast stretching":
            intFirstBar ("direct", "r1", 1, 200, 1)
            intSecondBar("direct", "s1", 1, 200, 1)
            intThirdBar ("direct", "r2", 50, 255, 200)
            intFourthBar("direct", "s2", 50, 255, 255)
            mapMethodValues(1, 1, 200, 255)
            
            setSliderChangeListener(1, "int", 0)
            setSliderChangeListener(2, "int", 1)
            setSliderChangeListener(3, "int", 2)
            setSliderChangeListener(4, "int", 3)
            
        case "Bit-plane slicing":
            intFirstBar("direct", "Bit-plane", 1, 8, 1)
            mapMethodValues(1)
            setSliderChangeListener(1, "int", 0)
            
        case "Mean blur":
            setKernelBarsChangeListener(methodName, 1, 10)
            
        case "Gaussian blur":
            setKernelBarsChangeListener(methodName, 1, 10)
            trackingDictionary[methodName]!.append(30)
            
            intFirstBar("double", "Sigma", 10, 50, 30)
            setSliderChangeListener(1, "double", 2)
            
        case "Laplacian sharp":
            mapMethodValues(1)
            intFirstBar("kernel", "Kernel size", 0, 3, 0)
            setSliderChangeListener(1, "kernel", 0)
            
        case "Gradient sharp":
            intFirstSpinner("Kernel type:")
            let kernelTypes = ["Sobel", "SobelX", "SobelY", "Prewitt", "PrewittX", "PrewittY", "Roberts"]
            firstSpinnerArray = kernelTypes
            firstSpinnerField.text = kernelTypes[0]
            spinnerPickedText = kernelTypes[0]
            
            // FREQUENCY DOMAIN
        case "Ideal lowpass", "Ideal highpass":
            let imageHeight : Int!
            let imageWidth  : Int!
            if (isGalleryMode) {
                imageHeight = Int(self.userView.image!.size.height) // currentImage
                imageWidth  = Int(self.userView.image!.size.width)  // currentImage
            }
            else {
                imageHeight = Int(self.userView.image!.size.height)
                imageWidth  = Int(self.userView.image!.size.width)
            }
            let isLowpass = (methodName == "Ideal lowpass") ? 1 : 0
            let maxCutOffFrequency = min(imageHeight, imageWidth)
            
            mapMethodValues(maxCutOffFrequency / 2, isLowpass)
            intFirstBar("norm_freq", "Cut-off frequency", 0, maxCutOffFrequency, maxCutOffFrequency / 2)
            setSliderChangeListener(1, "norm_freq", 0)
            
        case "Butterworth lowpass", "Butterworth highpass":
            let imageHeight : Int!
            let imageWidth  : Int!
            if (isGalleryMode) {
                imageHeight = Int(self.userView.image!.size.height) // currentImage
                imageWidth  = Int(self.userView.image!.size.width)  // currentImage
            }
            else {
                imageHeight = Int(self.userView.image!.size.height)
                imageWidth  = Int(self.userView.image!.size.width)
            }
            let isLowpass = (methodName == "Butterworth lowpass") ? 1 : 0
            let maxCutOffFrequency = min(imageHeight, imageWidth)
            
            mapMethodValues(maxCutOffFrequency / 2, isLowpass, 3)
            intFirstBar("norm_freq", "Cut-off frequency", 0, maxCutOffFrequency, maxCutOffFrequency / 2)
            intSecondBar("direct", "n", 1, 5, 3)
            setSliderChangeListener(1, "norm_freq", 0)
            setSliderChangeListener(2, "int", 2)
            
        case "Gaussian lowpass", "Gaussian highpass":
            let imageHeight : Int!
            let imageWidth  : Int!
            if (isGalleryMode) {
                imageHeight = Int(self.userView.image!.size.height) // currentImage
                imageWidth  = Int(self.userView.image!.size.width)  // currentImage
            }
            else {
                imageHeight = Int(self.userView.image!.size.height)
                imageWidth  = Int(self.userView.image!.size.width)
            }
            let isLowpass = (methodName == "Gaussian lowpass") ? 1 : 0
            let maxCutOffFrequency = min(imageHeight, imageWidth)
            
            mapMethodValues(maxCutOffFrequency / 2, isLowpass)
            intFirstBar("norm_freq", "Cut-off frequency", 0, maxCutOffFrequency, maxCutOffFrequency / 2)
            setSliderChangeListener(1, "norm_freq", 0)
            
        case "Laplacian":
            trackingDictionary[methodName] = nil
            
        case "Geometric mean":
            setKernelBarsChangeListener(methodName, 1, 10)
            
        case "Contraharmonic mean":
            setKernelBarsChangeListener(methodName, 1, 10)
            trackingDictionary[methodName]!.append(0)
            intFirstBar("double", "Order Q", -20, 20, 0)
            
            firstBarBar.addTarget(self, action: #selector(contraHarmonicChanged(sender:)), for: .valueChanged)
            
        case "Median":
            mapMethodValues(3)
            intFirstBar("kernel", "Kernel size", 1, 10, 1)
            
            firstBarBar.addTarget(self, action: #selector(medianChanged(sender:)), for: .valueChanged)
            
        case "Min", "Max", "Midpoint":
            setKernelBarsChangeListener(methodName, 1, 10)
            
        case "RGB planes":
            intFirstSpinner("Color plane:")
            let rgbPlanesArray = ["Red", "Green", "Blue"]
            firstSpinnerArray = rgbPlanesArray
            firstSpinnerField.text = rgbPlanesArray[0]
            spinnerPickedText = rgbPlanesArray[0]
            
        case "RGB to HSI":
            intFirstSpinner("Current plane:")
            let hsiPlanesArray = ["Hue", "Saturation", "Intensity"]
            firstSpinnerArray = hsiPlanesArray
            firstSpinnerField.text = hsiPlanesArray[0]
            spinnerPickedText = hsiPlanesArray[0]
            
        case "Intensity slicing":
            mapMethodValues(120, 150, 180)
            intFirstBar("direct", "Intensity (yellow)", 0, 255, 120)
            intSecondBar("direct", "Intensity (magenta)", 0, 255, 150)
            intThirdBar("direct", "Intensity (cyan)", 0, 255, 180)
            
            setSliderChangeListener(1, "int", 0)
            setSliderChangeListener(2, "int", 1)
            setSliderChangeListener(3, "int", 2)
            
        case "Intensity to color":
            extraComment.isHidden = false
            extraComment.text = "s = abs(sin(r x freq x pi + shift x pi))"
            mapMethodValues(10, 0, 2, 4)
            intFirstBar("double", "Sinusoid frequency", 10, 40, 10)
            intSecondBar("double", "Red phase shift"  , 0, 10, 0)
            intThirdBar("double", "Green phase shift" , 0, 10, 2)
            intFourthBar("double", "Blue phase shift" , 0, 10, 4)
            
            setSliderChangeListener(1, "double", 0)
            setSliderChangeListener(2, "double", 1)
            setSliderChangeListener(3, "double", 2)
            setSliderChangeListener(4, "double", 3)
            
        case "Color balance":
            if (isGalleryMode) {
                
            }
            else {
                toast(message: "Not available in real-time")
                baseInterface()
                trackingDictionary.removeValue(forKey: methodName)
            }
        case "Erosion", "Dilation", "Opening", "Closing", "Smoothing", "Gradient":
            intFirstSpinner("SE shape:")
            firstSpinnerImage.isHidden = false
            
            let strelShapesArray = ["Rect", "Cross", "Ellipse", "Disk"]
            firstSpinnerArray = strelShapesArray
            firstSpinnerField.text = strelShapesArray[0]
            spinnerPickedText = strelShapesArray[0]
            
            mapMethodValues(3, 3)
            intKernelBar(1, 10, 1)
            setKernelBarsChangeListener(methodName, 1, 10, true)
            
        case "Binary threshold":
            mapMethodValues(128)
            intFirstBar("direct", "Threshold value", 1, 254, 128)
            setSliderChangeListener(1, "int", 0)
            
        case "Otsu's threshold":
            extraComment.isHidden = false
            extraComment.text = "Threshold value: _"
            
        case "Hough circle":
            mapMethodValues(50, 100, 20, 50)
            intFirstBar ("direct", "Min distance factor", 1, 100, 50)
            intSecondBar("direct", "Canny top threshold", 2, 254, 100)
            intThirdBar ("direct", "Min radius", 1, 200, 20)
            intFourthBar("direct", "Max radius", 1, 200, 50)
            
            setSliderChangeListener(1, "int", 0)
            setSliderChangeListener(2, "int", 1)
            setSliderChangeListener(3, "int", 2)
            setSliderChangeListener(4, "int", 3)
            
        case "Haar transform":
            intFirstSpinner("Compression:")
            let compressionsArray = ["L1 Approximation", "L1 Horizontal", "L1 Vertical", "L1 Diagonal", "L2 Approximation", "L2 Horizontal", "L2 Vertical", "L2 Diagonal", "All"]
            firstSpinnerArray = compressionsArray
            firstSpinnerField.text = compressionsArray[0]
            spinnerPickedText = compressionsArray[0]
            
        default:
            toast(message: "Error: Method not found")
        }
        
        processInGalleryMode()
    }
    
    //==============================================================================================
    //============================== W O R K M O D E :  G A L L E R Y ==============================
    //==============================================================================================
    
    private func pickImage() {
        let picker = UIImagePickerController()
        picker.sourceType = .photoLibrary
        picker.delegate = self
        picker.allowsEditing = false
        present(picker, animated: true)
    }
    
        
    //==============================================================================================
    //============================ W O R K M O D E :  R E A L - T I M E ============================
    //==============================================================================================
    
    func captureOutput(
        _ output: AVCaptureOutput,
        didOutput sampleBuffer: CMSampleBuffer,
        from connection: AVCaptureConnection) {
        // here we can process the frame
        guard let  imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }
        CVPixelBufferLockBaseAddress(imageBuffer, CVPixelBufferLockFlags.readOnly)
        let baseAddress = CVPixelBufferGetBaseAddress(imageBuffer)
        let bytesPerRow = CVPixelBufferGetBytesPerRow(imageBuffer)
        let width = CVPixelBufferGetWidth(imageBuffer)
        let height = CVPixelBufferGetHeight(imageBuffer)
        let colorSpace = CGColorSpaceCreateDeviceRGB()
        var bitmapInfo: UInt32 = CGBitmapInfo.byteOrder32Little.rawValue
        bitmapInfo |= CGImageAlphaInfo.premultipliedFirst.rawValue & CGBitmapInfo.alphaInfoMask.rawValue
        let context = CGContext(data: baseAddress, width: width, height: height, bitsPerComponent: 8, bytesPerRow: bytesPerRow, space: colorSpace, bitmapInfo: bitmapInfo)
        guard let quartzImage = context?.makeImage() else { return }
        let image = UIImage(cgImage: quartzImage)
        let processedImage = imageProcessing(inputImage: image)
            
        DispatchQueue.main.async {
            self.userView.image = processedImage
            
            if self.resolutionForChange {
                let prevHeader = self.imageInfoHeader.text
                self.setImageInfo(processedImage)
                
                self.resolutionForChange = (prevHeader == self.imageInfoHeader.text) ? true : false
            }
            
        }
    }
    
    private func addCameraInput(resolution : Int) {
        guard let device = AVCaptureDevice.DiscoverySession(
            deviceTypes: [.builtInWideAngleCamera, .builtInDualCamera, .builtInTrueDepthCamera],
            mediaType: .video,
            position: .back).devices.first else {
                fatalError("No back camera device found, please make sure to run SimpleLaneDetection in an iOS device and not a simulator")
        }
        let cameraInput = try! AVCaptureDeviceInput(device: device)
        switch resolution {
        case 1:     self.captureSession.sessionPreset = AVCaptureSession.Preset.hd4K3840x2160
        case 2:     self.captureSession.sessionPreset = AVCaptureSession.Preset.hd1920x1080
        case 3:     self.captureSession.sessionPreset = AVCaptureSession.Preset.hd1280x720
        default:    self.captureSession.sessionPreset = AVCaptureSession.Preset.vga640x480
        }
        self.captureSession.addInput(cameraInput)
    }
    
    private func getFrames() {
        videoDataOutput.videoSettings = [(kCVPixelBufferPixelFormatTypeKey as NSString) : NSNumber(value: kCVPixelFormatType_32BGRA)] as [String : Any]
        videoDataOutput.alwaysDiscardsLateVideoFrames = true
        videoDataOutput.setSampleBufferDelegate(self, queue: DispatchQueue(label: "camera.frame.processing.queue"))
        self.captureSession.addOutput(videoDataOutput)
        guard let connection = self.videoDataOutput.connection(with: AVMediaType.video),
            connection.isVideoOrientationSupported else { return }
        connection.videoOrientation = .portrait
    }
    
    
    //==============================================================================================
    //=============================  I M A G E     P R O C E S S I N G  ============================
    //==============================================================================================
    
    private func imageProcessing (inputImage : UIImage) -> UIImage {
        if (trackingArray.count > 0) {
            let index = (isGalleryMode) ? (trackingArray.endIndex-1) : 0
            return imageProcessingLoop(inputImage, index)
        }
        return inputImage
    }
    
    private func imageProcessingLoop (_ inputImage : UIImage, _ index : Int) -> UIImage {
        var image = inputImage
        let method : String = trackingArray[index]
        
        func isGray () -> Bool {
            if (Wrapper.isColor(image)) {
                OnClickCancelMethod()
                toast(message: "Gray image is needed")
                return false
            }
            return true
        }
        
        func isRGB () -> Bool {
            if (!Wrapper.isColor(image)) {
                OnClickCancelMethod()
                toast(message: "RGB image is needed")
                return false
            }
            return true
        }
        
        switch method {
            
        case "To grayscale":
            image = Wrapper.toGrayscale(image)
            
        case "Negative":
            image = BasicMethods.negate(image)
            
        case "Log":
            image = BasicMethods.log(image)
            
        case "Inverse log":
            image = BasicMethods.inverseLog(image)
            
        case "Histogram equalization":
            if (isGray()) { image = BasicMethods.equilizeHist(image)}
            
        case "Power-law":
            let gamma = Double(trackingDictionary[method]![0]) / 10
            image = BasicMethods.powerLaw(image, gamma)
            
        case "Contrast stretching":
            let r1 = trackingDictionary[method]![0]
            let s1 = trackingDictionary[method]![1]
            let r2 = trackingDictionary[method]![2]
            let s2 = trackingDictionary[method]![3]
            image = BasicMethods.contrastStretching(image, r1, s1, r2, s2)
            
        case "Bit-plane slicing":
            if (isGray()) {
                let plane = trackingDictionary[method]![0]
                image = BasicMethods.bitPlaneSlicing(image, plane-1)
            }
            
        case "Mean blur":
            let width  = trackingDictionary[method]![0]
            let height = trackingDictionary[method]![1]
            image = SpatialMethods.meanBlur(image, width, height)
            
        case "Gaussian blur":
            let width  = trackingDictionary[method]![0]
            let height = trackingDictionary[method]![1]
            let sigma  = Double(trackingDictionary[method]![2]) / 10
            image = SpatialMethods.gaussianBlur(image, width, height, sigma)
            
        case "Laplacian sharp":
            image = SpatialMethods.spatialLaplacian(image, trackingDictionary[method]![0])
            
        case "Gradient sharp":
            if (isGray()) {
                image = SpatialMethods.firstDerivative(image, spinnerPickedText)
            }
            
        case "Ideal lowpass", "Ideal highpass":
            if (isGray()) {
                let cutOffFrequency = trackingDictionary[method]![0]
                let isLowpass = (trackingDictionary[method]![1] == 1)
                image = FrequencyMethods.freqIdeal(image, isLowpass, cutOffFrequency)
            }
            
        case "Butterworth lowpass", "Butterworth highpass":
            if (isGray()) {
                let cutOffFrequency = trackingDictionary[method]![0]
                let isLowpass = (trackingDictionary[method]![1] == 1)
                let n = trackingDictionary[method]![2]
                image = FrequencyMethods.freqButterworth(image, isLowpass, cutOffFrequency, n)
            }
            
        case "Gaussian lowpass", "Gaussian highpass":
            if (isGray()) {
                let cutOffFrequency = trackingDictionary[method]![0]
                let isLowpass = (trackingDictionary[method]![1] == 1)
                image = FrequencyMethods.freqGaussian(image, isLowpass, cutOffFrequency)
            }
            
        case "Laplacian":
            if (isGray()) {
                image = FrequencyMethods.freqLaplacian(image)
            }
            
        case "Geometric mean":
            let width  = trackingDictionary[method]![0]
            let height = trackingDictionary[method]![1]
            image = NoiseMethods.geometricMean(image, width, height)
            
        case "Contraharmonic mean":
            let width  = trackingDictionary[method]![0]
            let height = trackingDictionary[method]![1]
            let q      = Double(trackingDictionary[method]![2]) / 10
            image = NoiseMethods.contraharmonicMean(image, q, width, height)
            
        case "Median":
            image = NoiseMethods.medianFilter(image, trackingDictionary[method]![0])
            
        case "Min":
            let width  = trackingDictionary[method]![0]
            let height = trackingDictionary[method]![1]
            image = NoiseMethods.minFilter(image, width, height)
            
        case "Max":
            let width  = trackingDictionary[method]![0]
            let height = trackingDictionary[method]![1]
            image = NoiseMethods.maxFilter(image, width, height)
            
        case "Midpoint":
            let width  = trackingDictionary[method]![0]
            let height = trackingDictionary[method]![1]
            image = NoiseMethods.midpointFilter(image, width, height)
            
        case "RGB planes":
            if (isRGB()) {
                image = ColorMethods.rgb2planes(image, spinnerPickedText)
            }
            
        case "RGB to HSI":
            if (isRGB()) {
                image = ColorMethods.rgb2hsi(image, spinnerPickedText)
            }
            
        case "Intensity slicing":
            if (isGray()) {
                let intensity1 = trackingDictionary[method]![0]
                let intensity2 = trackingDictionary[method]![1]
                let intensity3 = trackingDictionary[method]![2]
                image = ColorMethods.graySlicing(image, intensity1, intensity2, intensity3)
            }
            
        case "Intensity to color":
            if (isGray()) {
                let frequency = Double(trackingDictionary[method]![0]) / 10
                let shiftR    = Double(trackingDictionary[method]![1]) / 10
                let shiftG    = Double(trackingDictionary[method]![2]) / 10
                let shiftB    = Double(trackingDictionary[method]![3]) / 10
                image = ColorMethods.gray2color(image, frequency, shiftR, shiftG, shiftB)
            }
            
        case "Color balance":
            if (isRGB()) {
                image = ColorMethods.colorBalance(image, 5)
            }
            
        case "Erosion", "Dilation", "Opening", "Closing", "Smoothing", "Gradient":
            let width  = trackingDictionary[method]![0] // or radius
            let height = trackingDictionary[method]![1]
            image = MorphologyMethods.morphMethod2(image, method, spinnerPickedText, width, height)
            
        case "Binary threshold":
            if (isGray()) {
                image = SegmentationMethods.binaryThreshold(image, trackingDictionary[method]![0])
            }
            
        case "Otsu's threshold":
            if (isGray()) {
                image = SegmentationMethods.otsuThreshold(image)
                var otsuText = String(SegmentationMethods.getOtsuValue())
                otsuText = "Threshold value: " + otsuText
                self.extraComment.text = otsuText
            }
            
        case "Hough circle":
            let minDistFactor = trackingDictionary[method]![0]
            let cannyParam    = trackingDictionary[method]![1]
            let minRadius     = trackingDictionary[method]![2]
            let maxRadius     = trackingDictionary[method]![3]
            image = SegmentationMethods.houghCircle(image, minDistFactor, cannyParam, minRadius, maxRadius)
            
        case "Haar transform":
            if (isGray()) {
                image = WaveletsMethods.haarForward(image, spinnerPickedText)
            }
            
        default:
            OnClickCancelMethod()
            toast(message: "Error: Method not found")
            return inputImage
        }
        
        if ((index+1) < trackingArray.count) {
            return imageProcessingLoop(image, index+1)
        }
        else {
            return image
        }
    }

}

    //==============================================================================================
    //======================================= E X T E N S I O N S ==================================
    //==============================================================================================

    // G A L L E R Y    M O D E

extension ProcessingController : UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        
        if let selectedImage = info[UIImagePickerController.InfoKey(rawValue: "UIImagePickerControllerOriginalImage")] as? UIImage {
            srcUserImage = selectedImage
            processImage = selectedImage
            currentImage = selectedImage
            userView.image = currentImage
            setImageInfo(currentImage)

        }
        picker.dismiss(animated: true, completion: nil)
    }
    
    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true, completion: nil)
    }
}

extension ProcessingController : UIPickerViewDelegate, UIPickerViewDataSource {
    
    public func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    public func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return firstSpinnerArray.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        firstSpinnerArray[row]
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        firstSpinnerField.text = firstSpinnerArray[row]
        spinnerPickedText = firstSpinnerArray[row]
        
        let methodName = MathParams_methodName.text!
        
        if (firstSpinnerArray[0] == "Rect") {
            
            if (firstSpinnerArray[row] != "Disk") {
                
                if (firstBar.isHidden == false) {
                    firstBar.isHidden = true
                    kernelBars.isHidden = false
                }
                setKernelBarsChangeListener(methodName, 1, 10, true)
            }
            else {
                if (kernelBars.isHidden == false) {
                    kernelBars.isHidden = true
                    firstBar.isHidden = false
                }
                trackingDictionary[methodName]![0] = 1
                
                intFirstBar("direct", "Radius", 1, 5, 1)
                setSliderChangeListener(1, "int", 0)
            }
        }
        
        if (resolutionChanging) {
            resolutionChanging = false
            resolutionTextField.resignFirstResponder()
            
            self.captureSession.stopRunning()
            self.captureSession = AVCaptureSession()
            
            switch spinnerPickedText {
                case "3840 x 2160" : self.addCameraInput(resolution: 1)
                case "1920 x 1080" : self.addCameraInput(resolution: 2)
                case "1280 x 720"  : self.addCameraInput(resolution: 3)
                default:             self.addCameraInput(resolution: 4)
            }
            self.getFrames()
            self.captureSession.startRunning()
            resolutionForChange = true
        }
        else {
            firstSpinnerField.resignFirstResponder()
            processInGalleryMode()
        }
       
    }
}

extension ProcessingController: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        if (tableView == TVtracking) {
            return
        }
                
        // Android: cambiar methodsAppliedArray por trackingArray
        let methodName = selectedCategoryArray[indexPath[1]]
        showMethodInterface(methodName)
         
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (tableView == TVsetOfMethods) {
            return selectedCategoryArray.count
        }
        else {
            return trackingArray.count
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        if (tableView == TVsetOfMethods) {
            let inner_methods_cell = TVsetOfMethods.dequeueReusableCell(withIdentifier: "inner_methods_cell", for: indexPath)
            
            inner_methods_cell.textLabel?.text = selectedCategoryArray[indexPath.row]
            inner_methods_cell.textLabel?.textColor = UIColor.black
            return inner_methods_cell
        }
        else {
            let methods_applied_cell = TVtracking.dequeueReusableCell(withIdentifier: "methods_applied_cell", for: indexPath)
            
            methods_applied_cell.textLabel?.text = trackingArray[indexPath.row]
            methods_applied_cell.textLabel?.textColor = UIColor.black
             
            return methods_applied_cell
             
        }
         
    }
}

