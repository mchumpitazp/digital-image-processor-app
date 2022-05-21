package com.example.imageprocessor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.imageprocessor.databinding.ActivityProcessingBinding

import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.io.File

import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import kotlin.math.min
import kotlin.math.round

class ProcessingActivity : AppCompatActivity() {

    //==============================================================================================
    //=============================== G L O B A L    V A R I A B L E S =============================
    //==============================================================================================

    // Workspace variables
    var isGalleryMode = true
    lateinit var binding  : ActivityProcessingBinding
    lateinit var MethodInterface : MethodInterface

    // User Image variables
    private lateinit var dstUserBitmap : Bitmap
    private lateinit var srcUserImage  : Mat
    private lateinit var currentImage  : Mat
    private lateinit var processImage  : Mat
    private lateinit var outputImage   : Mat

    // Tracking variables
    private var methodsAppliedArray : ArrayList<String> = arrayListOf()

    // CameraX variables
    private lateinit var cameraProvider : ProcessCameraProvider
    private lateinit var preview        : Preview
    private lateinit var imageCapture   : ImageCapture
    private lateinit var imageAnalysis  : ImageAnalysis
    private var executor = Executors.newSingleThreadExecutor()

    // Streaming variables
    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUIRED_PERMISSIONS = arrayOf("android.permission.CAMERA",
                                                "android.permission.WRITE_EXTERNAL_STORAGE",
                                                "android.permission.READ_EXTERNAL_STORAGE")
    private var resolutionSize = Size (720, 960)


    // Image Processing variables
    private var methodsAppliedMap : MutableMap<String, ArrayList<Int>?> = mutableMapOf()
    private var strelShape = "Rect"

    // Toast variables
    enum class Message {
        NOT_AVAILABLE,
        GRAY_NEEDED,
        RGB_NEEDED,
        IN_PROGRESS,
        SAVING
    }
    
    //==============================================================================================
    //============================================ M A I N =========================================
    //==============================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessingBinding.inflate(LayoutInflater.from(this@ProcessingActivity))
        setContentView(binding.root)

        // Initialize OpenCV related
        OpenCVLoader.initDebug()
        srcUserImage = Mat()
        currentImage = Mat()
        processImage = Mat()

        // Global initialization
        isGalleryMode = intent.getBooleanExtra("isGalleryMode", true)
        MethodInterface = MethodInterface(this.binding)
        setGlobalOnClickListeners()

        if (isGalleryMode) {
            pickImageGallery()
        }
        else {
            if (allPermissionsGranted())
                startStreaming()
            else
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    //==============================================================================================
    //======================== S E T    G L O B A L    O N    C L I C K ============================
    //==============================================================================================

    //---------------------------- S U P P O R T    F U N C T I O N S ------------------------------

    private fun getToolbarVisibilityCode () : Int {
        return  if (binding.llTracking.visibility == View.VISIBLE)               { 1 }
                else if (binding.llSetOfCategories.visibility == View.VISIBLE
                        || binding.llSetOfMethods.visibility == View.VISIBLE)   { 2 }
                else if (binding.llMathParams.visibility == View.VISIBLE)       { 3 }
                else                                                            { 0 }
    }

    private fun setImageInfo (image : Mat) {
        var imageInfo = "" + image.rows() + " x " + image.cols()
        imageInfo += if (image.channels() > 1)
                        " | RGB"
                    else
                        " | Grayscale"
        binding.imageInfoHeader.text = imageInfo
    }

    private fun setUserImage (image : Mat) {
        val userBitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(image, userBitmap)
        binding.userImageView.setImageBitmap(userBitmap)
    }

    private fun updateTracking () {
        binding.trackingListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, methodsAppliedArray)
    }

    private fun setCategoryListener (selectedCategory : String, categoryArrayCode : Int) : View.OnClickListener {
        return View.OnClickListener {
            binding.llSetOfCategories.visibility = View.GONE
            binding.llSetOfMethods.visibility = View.VISIBLE
            binding.llSetOfMethodsSelectedCategory.text = selectedCategory
            binding.llSetOfMethodsMethodsList.adapter = ArrayAdapter.createFromResource(this, categoryArrayCode, android.R.layout.simple_list_item_1)
            if (!isGalleryMode)
                if (selectedCategory == "Filtering in frequency domain" || selectedCategory == "Wavelets")
                    Toast.makeText(this, "Resolution may be modified by clicking on itself", Toast.LENGTH_LONG).show()
        }
    }

    private fun hideMathParameters () {
        MethodInterface.baseInterface()
        binding.FirstSpinner.visibility = View.GONE
        binding.KernelBars.visibility   = View.GONE
        binding.FirstBar.visibility     = View.GONE
        binding.SecondBar.visibility    = View.GONE
        binding.ThirdBar.visibility     = View.GONE
        binding.FourthBar.visibility    = View.GONE
        binding.ExtraComment.visibility = View.GONE
    }

    private fun setNullMathParametersListeners () {
        binding.FirstSpinnerSpinner.onItemSelectedListener = null
        binding.KernelHeightBar.setOnSeekBarChangeListener  (null)
        binding.KernelWidthBar.setOnSeekBarChangeListener   (null)
        binding.FirstBarBar.setOnSeekBarChangeListener      (null)
        binding.SecondBarBar.setOnSeekBarChangeListener     (null)
        binding.ThirdBarBar.setOnSeekBarChangeListener      (null)
        binding.FourthBarBar.setOnSeekBarChangeListener     (null)
    }


    //-------------------------------- M A I N    F U N C T I O N ----------------------------------

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setGlobalOnClickListeners () {

        // H E A D E R     B U T T O N S

        binding.btnBack.setOnClickListener {
            val backToMainActivity = Intent(this@ProcessingActivity, MainActivity::class.java)
            startActivity(backToMainActivity)
        }

        binding.imageInfoHeader.setOnClickListener {
            when {
                binding.llMathParams.visibility == View.VISIBLE -> toast(Message.IN_PROGRESS)
                binding.llCapture.visibility == View.VISIBLE    -> toast(Message.SAVING)
                else -> binding.imageInfoHeaderSpinner.performClick()
            }
        }

        if (!isGalleryMode) {
            val spinnerArray = arrayOf("960 x 720", "640 x 480", "320 x 240")
            val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerArray)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.imageInfoHeaderSpinner.adapter = spinnerAdapter

            binding.imageInfoHeaderSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    val size : Size = when (position) {
                        0    -> Size(720,960)
                        1    -> Size(480,640)
                        else -> Size(240,320)
                    }
                    resolutionSize = size
                    startStreaming()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        binding.btnToGray.setOnClickListener {
            if (isGalleryMode) {
                if (currentImage.channels() == 1)
                    Toast.makeText(this@ProcessingActivity, "Image already in grayscale", Toast.LENGTH_SHORT).show()
                else {
                    methodsAppliedArray.add("To grayscale")
                    Imgproc.cvtColor(currentImage, currentImage, Imgproc.COLOR_RGB2GRAY)
                    if (binding.llTracking.visibility == View.VISIBLE) {
                        updateTracking()
                        binding.btnTracking.performClick()
                        binding.btnTracking.performClick()
                    }

                    setImageInfo(currentImage)
                    setUserImage(currentImage)
                }
            }
            else {
                if (methodsAppliedArray.contains("To grayscale"))
                    Toast.makeText(this@ProcessingActivity, "Image already in grayscale", Toast.LENGTH_SHORT).show()
                else
                    methodsAppliedArray.add("To grayscale") // Falta que se actualice la lista de metodos aplicados en caso este abierto el tracking
            }

        }

        // T R A C K I N G     B U T T O N S

        binding.btnTracking.setOnClickListener {
            updateTracking()

            when (getToolbarVisibilityCode()) {
                // case 0: Everything is closed
                0 -> {
                    if (methodsAppliedArray.isEmpty()) {
                        binding.trackingEmptyText.visibility = View.VISIBLE
                        binding.trackingListView.visibility = View.GONE
                    }
                    else {
                        binding.trackingEmptyText.visibility = View.GONE
                        binding.trackingListView.visibility = View.VISIBLE
                    }
                    binding.llTracking.visibility = View.VISIBLE
                }
                // case 1: Tracking is open
                1 ->  binding.llTracking.visibility = View.GONE
                // case 2: Set of methods is open
                2 -> {
                    if (methodsAppliedArray.isEmpty()) {
                        binding.trackingEmptyText.visibility = View.VISIBLE
                        binding.trackingListView.visibility = View.GONE
                    }
                    else {
                        binding.trackingEmptyText.visibility = View.GONE
                        binding.trackingListView.visibility = View.VISIBLE
                    }
                    binding.llSetOfCategories.visibility = View.GONE
                    binding.llSetOfMethods.visibility = View.GONE
                    binding.btnAddMethod.setImageDrawable(getDrawable(R.drawable.ic_plus))
                    binding.llTracking.visibility = View.VISIBLE
                }
                // case 3: Parameters are open
                3 -> toast(Message.IN_PROGRESS)
            }
        }

        binding.btnRemoveTracking.setOnClickListener {
            binding.trackingListView.visibility = View.GONE
            binding.trackingEmptyText.visibility = View.VISIBLE
            methodsAppliedArray.clear()
            methodsAppliedMap.clear()
            updateTracking()

            if (isGalleryMode) {
                setImageInfo(srcUserImage)
                setUserImage(srcUserImage)
                srcUserImage.copyTo(currentImage)
                srcUserImage.copyTo(processImage)
            }
        }

        // M E T H O D    S E L E C T I O N    B U T T O N S

        binding.btnAddMethod.setOnClickListener {
            when (getToolbarVisibilityCode()) {
                // case 0: Everything is closed
                0 -> {
                    binding.llSetOfCategories.visibility = View.VISIBLE
                    binding.btnAddMethod.setImageDrawable(getDrawable(R.drawable.ic_minus))
                }
                // case 1: Tracking is open
                1 -> {
                    binding.llTracking.visibility = View.GONE
                    binding.llSetOfCategories.visibility = View.VISIBLE
                    binding.btnAddMethod.setImageDrawable(getDrawable(R.drawable.ic_minus))
                }
                // case 2: Set of categories or methods is open
                2 -> {
                    binding.llSetOfCategories.visibility = View.GONE
                    binding.llSetOfMethods.visibility = View.GONE
                    binding.btnAddMethod.setImageDrawable(getDrawable(R.drawable.ic_plus))
                }
                // case 3: Mathematical parameters are open
                3 -> toast(Message.IN_PROGRESS)
            }
        }

        binding.llSetOfCategoriesBasics.setOnClickListener  (
            setCategoryListener(getString(R.string.category__basics), R.array.basic_transforms) )
        binding.llSetOfCategoriesSpatial.setOnClickListener (
            setCategoryListener(getString(R.string.category__spatial), R.array.spatial_domain) )
        binding.llSetOfCategoriesFrequency.setOnClickListener (
            setCategoryListener(getString(R.string.category__frequency), R.array.frequency_domain) )
        binding.llSetOfCategoriesRestoration.setOnClickListener (
            setCategoryListener(getString(R.string.category__restoration), R.array.restoration_filters) )
        binding.llSetOfCategoriesColor.setOnClickListener (
            setCategoryListener(getString(R.string.category__color), R.array.color_processing) )
        binding.llSetOfCategoriesWavelets.setOnClickListener (
            setCategoryListener(getString(R.string.category__wavelets), R.array.wavelets) )
        binding.llSetOfCategoriesMorphology.setOnClickListener (
            setCategoryListener(getString(R.string.category__morphology), R.array.morphology_filters) )
        binding.llSetOfCategoriesSegmentation.setOnClickListener (
            setCategoryListener(getString(R.string.category__segmentation), R.array.segmentation) )

        binding.llSetOfMethodsSelectedCategory.setOnClickListener {
            binding.llSetOfMethods.visibility = View.GONE
            binding.llSetOfCategories.visibility = View.VISIBLE
        }

        binding.llSetOfMethodsMethodsList.setOnItemClickListener { parent, _, position, _ ->
            val methodName = parent.getItemAtPosition(position).toString()
            showMethodInterface(methodName)
        }

        // M E T H O D     I N T E R F A C E     B U T T O N S

        binding.btnCancelMethod.setOnClickListener {
            hideMathParameters()
            setNullMathParametersListeners()

            val methodName = binding.llMathParamsMethodName.text.toString()
            methodsAppliedArray.remove(methodName)
            methodsAppliedMap.remove(methodName)

            if (isGalleryMode) {
                setImageInfo(currentImage)
                setUserImage(currentImage)
            }
        }

        binding.btnApplyMethod.setOnClickListener {
            hideMathParameters()
            setNullMathParametersListeners()

            binding.llSetOfMethods.visibility = View.GONE
            binding.btnAddMethod.setImageDrawable(getDrawable(R.drawable.ic_plus))

            if (isGalleryMode) {
                currentImage = processImage
            }
        }

        // S A V E     B U T T O N S

        fun saveImage (image : Bitmap) {
            var uri : Uri? = null
            val resolver   = contentResolver
            val values     = ContentValues()

            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "image_" + System.currentTimeMillis())
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "ImageProcessor")

            try {
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                uri = resolver.insert(contentUri, values)

                if (uri == null)
                    throw IOException("Failed to create new MediaStore record.")

                val stream = resolver.openOutputStream(uri) ?: throw IOException("Failed to open output stream.")

                if (!image.compress(Bitmap.CompressFormat.PNG, 100, stream))
                    throw IOException("Failed to save bitmap.")
            } catch (e : IOException) {
                if (uri != null) resolver.delete(uri, null, null)
                e.printStackTrace()
            }
        }

        if (isGalleryMode) {
            binding.btnSaveImage.setOnClickListener {
                if (binding.llMathParams.visibility == View.GONE) {
                    val bitmapToSave = Bitmap.createBitmap(currentImage.cols(), currentImage.rows(), Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(currentImage, bitmapToSave)
                    saveImage(bitmapToSave)
                    Toast.makeText(this, "Image saved in ImageProcessor album", Toast.LENGTH_LONG).show()
                }
                else {
                    toast(Message.IN_PROGRESS)
                }
            }
        }
        else {
            fun savingListener (isAccepted : Boolean) : View.OnClickListener {
                return View.OnClickListener {
                    if (isAccepted) {
                        saveImage(dstUserBitmap)
                        Toast.makeText(this, "Image saved in ImageProcessor album", Toast.LENGTH_LONG).show()
                    }
                    displayCapturedImage(false)
                }
            }

            binding.btnRejectSave.setOnClickListener ( savingListener(false) )
            binding.btnAcceptSave.setOnClickListener ( savingListener(true) )

            binding.btnSaveImage.setOnClickListener {
                if (binding.llMathParams.visibility == View.GONE) {
                    if (binding.llSetOfCategories.visibility == View.VISIBLE ||
                        binding.llSetOfMethods.visibility == View.VISIBLE)
                        binding.btnAddMethod.performClick()
                    if (binding.llTracking.visibility == View.VISIBLE)
                        binding.btnTracking.performClick()

                    displayCapturedImage(true)
                    dstUserBitmap = Bitmap.createBitmap(outputImage.cols(), outputImage.rows(), Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(outputImage, dstUserBitmap)
                }
                else {
                    toast(Message.IN_PROGRESS)
                }
            }
        }

    }

    //==============================================================================================
    //============================= M E T H O D S    O N    C L I C K ==============================
    //==============================================================================================

    //---------------------------- S U P P O R T    F U N C T I O N S ------------------------------

    /**
     * Display a user message.
     *
     * @param messageCode Possible inputs are entries of enum Message
     */

    private fun toast (messageCode : Message) {
        val message : String = when (messageCode) {
            Message.NOT_AVAILABLE -> "Not available in real-time mode"
            Message.GRAY_NEEDED   -> "Gray image is needed"
            Message.RGB_NEEDED    -> "RGB image is needed"
            Message.IN_PROGRESS   -> "Apply or cancel the method in progress"
            Message.SAVING        -> "Image saving in progress"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun mapMethodValues (i1 : Int = -1, i2 : Int = -1, i3 : Int = -1, i4 : Int = -1) {
        val valuesArray : ArrayList<Int> = arrayListOf()
        val method = binding.llMathParamsMethodName.text

        if (i1 != -1) {valuesArray.add(i1)}
        if (i2 != -1) {valuesArray.add(i2)}
        if (i3 != -1) {valuesArray.add(i3)}
        if (i4 != -1) {valuesArray.add(i4)}

        methodsAppliedMap[method.toString()] = valuesArray
    }

    private fun processInGalleryMode () {
        if (isGalleryMode) {
            processImage = imageProcessing(currentImage)
            setUserImage(processImage)
            setImageInfo(processImage)
        }
    }

    private fun setSpinnerAdapter (spinnerArray : Array<String>) {
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerArray)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.FirstSpinnerSpinner.adapter = spinnerAdapter

        binding.FirstSpinnerSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                processInGalleryMode()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setKernelBarsChangeListeners (methodName: String, min : Int, max : Int, skip : Boolean = false) {
        if (!skip) {
            val value = min * 2 + 1
            mapMethodValues(value, value)
            MethodInterface.kernelBars(min, max, min)
        }

        binding.KernelWidthBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val width  = progress * 2 + 1
                methodsAppliedMap[methodName]!![0] = width

                val height = methodsAppliedMap[methodName]!![1]
                val kernelSize = "$height x $width"
                binding.KernelProgress.text = kernelSize

                processInGalleryMode()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.KernelHeightBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val height  = progress * 2 + 1
                methodsAppliedMap[methodName]!![1] = height

                val width = methodsAppliedMap[methodName]!![0]
                val kernelSize = "$height x $width"
                binding.KernelProgress.text = kernelSize

                processInGalleryMode()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setSeekBarChangeListener (seekBarNumber : Int, dataType : String, arrayIndex : Int) {
        val method = binding.llMathParamsMethodName.text.toString()
        var strValue = ""
        var newProgress: Int

        val bindBarBar: SeekBar
        val bindBarProgress: TextView

        when (seekBarNumber) {
            1 -> {
                bindBarBar = binding.FirstBarBar
                bindBarProgress = binding.FirstBarProgress
            }
            2 -> {
                bindBarBar = binding.SecondBarBar
                bindBarProgress = binding.SecondBarProgress
            }
            3 -> {
                bindBarBar = binding.ThirdBarBar
                bindBarProgress = binding.ThirdBarProgress
            }
            else -> {
                bindBarBar = binding.FourthBarBar
                bindBarProgress = binding.FourthBarProgress
            }
        }

        bindBarBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                newProgress = progress

                when (dataType) {
                    "int"       ->  strValue = progress.toString()
                    "double"    ->  strValue = (progress.toDouble() / 10).toString()
                    "kernel"    -> {
                        newProgress = progress * 2 + 1
                        strValue = newProgress.toString()
                    }
                    "norm_freq" -> {
                        var normalizedProgress = progress.toDouble() / bindBarBar.max
                        normalizedProgress = round(normalizedProgress * 1000) / 1000
                        strValue = normalizedProgress.toString()
                    }
                }
                bindBarProgress.text = strValue
                methodsAppliedMap[method]!![arrayIndex] = newProgress

                processInGalleryMode()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    //-------------------------------- M A I N    F U N C T I O N ----------------------------------

    @SuppressLint("SetTextI18n")
    private fun showMethodInterface (methodName : String) {
        binding.llMathParamsMethodName.text = methodName
        methodsAppliedArray.add(methodName)
        MethodInterface.baseInterface()

        when (methodName) {
            "Negative", "Log", "Inverse log", "Histogram equalization" -> methodsAppliedMap[methodName] = null

            "Power-law" -> {
                MethodInterface.firstBar("double", "Gamma", 1, 70, 10)
                mapMethodValues(10)
                setSeekBarChangeListener(1, "double", 0)
            }

            // REVISAR: PROBLEMAS AL PASAR VALORES ENTRE FUNCIONES
            "Contrast stretching" -> {
                MethodInterface.firstBar ("direct", "r1", 1 , 200, 1)
                MethodInterface.secondBar("direct", "s1", 1 , 200, 1)
                MethodInterface.thirdBar ("direct", "r2", 50, 255, 200)
                MethodInterface.fourthBar("direct", "s2", 50, 255, 255)
                mapMethodValues(1, 1, 200, 255)

                setSeekBarChangeListener(1, "int", 0)
                setSeekBarChangeListener(2, "int", 1)
                setSeekBarChangeListener(3, "int", 2)
                setSeekBarChangeListener(4, "int", 3)
            }

            "Bit-plane slicing" -> {
                MethodInterface.firstBar("direct", "Bit-plane", 1, 8, 1)
                mapMethodValues(1)
                setSeekBarChangeListener(1, "int", 0)
            }

            "Mean blur" -> setKernelBarsChangeListeners(methodName, 1, 10)

            "Gaussian blur" -> {
                setKernelBarsChangeListeners(methodName, 1, 10)
                methodsAppliedMap[methodName]!!.add(30)

                MethodInterface.firstBar("double", "Sigma", 10, 50, 30)
                setSeekBarChangeListener(1, "double", 2)
            }

            "Laplacian sharp" -> {
                mapMethodValues(1)
                MethodInterface.firstBar("kernel", "Kernel size", 0, 4, 0)
                setSeekBarChangeListener(1, "kernel", 0)
            }

            "Unsharp mask and highboost filter" -> Toast.makeText(this, "Method in progress", Toast.LENGTH_SHORT).show()

            "Gradient sharp" -> {
                MethodInterface.firstSpinner("Kernel type:")
                val kernelTypes = arrayOf("Sobel", "SobelX", "SobelY", "Prewitt", "PrewittX", "PrewittY", "Roberts")
                setSpinnerAdapter(kernelTypes)
            }

            "Ideal lowpass", "Ideal highpass" -> {
                val isLowpass = if (methodName == "Ideal lowpass") { 1 } else { 0 }
                val maxCutOffFrequency = if (isGalleryMode)
                                            min(currentImage.cols(), currentImage.rows())
                                        else
                                            min(outputImage.cols(), outputImage.rows())
                mapMethodValues(maxCutOffFrequency / 2, isLowpass)

                MethodInterface.firstBar("norm_freq", "Cut-off frequency", 0, maxCutOffFrequency, maxCutOffFrequency / 2)
                setSeekBarChangeListener(1, "norm_freq", 0)
            }

            "Butterworth lowpass", "Butterworth highpass" -> {
                val isLowpass = if (methodName == "Butterworth lowpass") { 1 } else { 0 }
                val maxCutOffFrequency = if (isGalleryMode)
                                            min(currentImage.cols(), currentImage.rows())
                                        else
                                            min(outputImage.cols(), outputImage.rows())
                mapMethodValues(maxCutOffFrequency / 2, isLowpass, 3)

                MethodInterface.firstBar ("norm_freq", "Cut-off frequency", 0, maxCutOffFrequency, maxCutOffFrequency / 2)
                MethodInterface.secondBar("direct", "n", 1, 5,3)
                setSeekBarChangeListener(1, "norm_freq", 0)
                setSeekBarChangeListener(2, "int"      , 2)
            }

            "Gaussian lowpass", "Gaussian highpass" -> {
                val isLowpass = if (methodName == "Gaussian lowpass") { 1 } else { 0 }
                val maxCutOffFrequency = if (isGalleryMode)
                                            min(currentImage.cols(), currentImage.rows())
                                        else
                                            min(outputImage.cols(), outputImage.rows())
                mapMethodValues(maxCutOffFrequency / 2, isLowpass)

                MethodInterface.firstBar("norm_freq", "Cut-off frequency", 0, maxCutOffFrequency, maxCutOffFrequency / 2)
                setSeekBarChangeListener(1, "norm_freq", 0)
            }

            "Laplacian" -> methodsAppliedMap[methodName] = null

            "Geometric mean" -> setKernelBarsChangeListeners(methodName, 1, 10)

            "Contraharmonic mean" -> {
                setKernelBarsChangeListeners(methodName, 1, 10)
                methodsAppliedMap[methodName]!!.add(0)
                MethodInterface.firstBar("double", "Order Q", -20, 20, 0)

                binding.FirstBarBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    @SuppressLint("SetTextI18n")
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        when (progress) {
                            -10  -> binding.llMathParamsMethodName.text = "Harmonic mean"
                            0    -> binding.llMathParamsMethodName.text = "Arithmetic mean"
                            else -> binding.llMathParamsMethodName.text = methodName
                        }

                        val d1 = progress.toDouble() / 10
                        binding.FirstBarProgress.text = d1.toString()
                        methodsAppliedMap[methodName]!![2] = progress

                        processInGalleryMode()
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }

            "Median" -> {
                mapMethodValues(3)
                MethodInterface.firstBar("kernel", "Kernel size", 1, 10, 1)

                binding.FirstBarBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        val prog = progress * 2 + 1
                        binding.FirstBarProgress.text = prog.toString()
                        methodsAppliedMap[methodName]!![0] = prog

                        processInGalleryMode()
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }

            "Min", "Max", "Midpoint" -> setKernelBarsChangeListeners(methodName, 1, 10)

            "RGB planes" -> {
                MethodInterface.firstSpinner("Color plane:")
                val rgbPlanes = arrayOf("Red", "Green", "Blue")
                setSpinnerAdapter(rgbPlanes)
            }

            "RGB to HSI" -> {
                MethodInterface.firstSpinner("Current plane:")
                val hsiPlanes = arrayOf("Hue", "Saturation", "Intensity")
                setSpinnerAdapter(hsiPlanes)
            }

            "Intensity slicing" -> {
                mapMethodValues(120, 150, 180)
                MethodInterface.firstBar ("direct", "Intensity (yellow)", 0, 255, 120)
                MethodInterface.secondBar("direct", "Intensity (magenta)", 0, 255, 150)
                MethodInterface.thirdBar ("direct", "Intensity (cyan)", 0, 255, 180)

                setSeekBarChangeListener(1, "int", 0)
                setSeekBarChangeListener(2, "int", 1)
                setSeekBarChangeListener(3, "int", 2)

            }

            "Intensity to color" -> {
                binding.ExtraComment.visibility = View.VISIBLE
                binding.ExtraComment.text = "s = abs(sin(r x freq x pi + shift x pi))"
                mapMethodValues(10, 0, 2, 4)
                MethodInterface.firstBar ("double", "Sinusoid frequency", 10, 40, 10)
                MethodInterface.secondBar("double", "Red phase shift"   , 0 , 10, 0)
                MethodInterface.thirdBar ("double", "Green phase shift" , 0 , 10, 2)
                MethodInterface.fourthBar("double", "Blue phase shift"  , 0 , 10, 4)

                setSeekBarChangeListener(1, "double", 0)
                setSeekBarChangeListener(2, "double", 1)
                setSeekBarChangeListener(3, "double", 2)
                setSeekBarChangeListener(4, "double", 3)
            }

            "Color balance" -> {
                if (isGalleryMode) {
                    if (currentImage.channels() == 1) {
                        toast(Message.RGB_NEEDED)
                        MethodInterface.baseInterface()
                        methodsAppliedArray.remove(methodName)
                    }
                }
                else {
                    toast(Message.NOT_AVAILABLE)
                    MethodInterface.baseInterface()
                    methodsAppliedArray.remove(methodName)
                }
            }

            "Erosion", "Dilation", "Opening", "Closing", "Smoothing", "Gradient" -> {
                MethodInterface.firstSpinner("SE shape:")
                binding.FirstSpinnerImage.visibility = View.VISIBLE

                val seShapes = arrayOf("Rect", "Ellipse", "Cross", "Disk")
                setSpinnerAdapter(seShapes)
                mapMethodValues(3, 3)
                
                binding.FirstSpinnerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        strelShape = parent!!.getItemAtPosition(position).toString()

                        if (strelShape != "Disk") {
                            when (strelShape) {
                                "Rect"    -> binding.FirstSpinnerImage.setImageResource(R.drawable.strel_rect)
                                "Cross"   -> binding.FirstSpinnerImage.setImageResource(R.drawable.strel_cross)
                                "Ellipse" -> binding.FirstSpinnerImage.setImageResource(R.drawable.strel_ellipse)
                            }

                            if (binding.FirstBar.visibility == View.VISIBLE)
                                binding.FirstBar.visibility = View.GONE
                            if (binding.KernelBars.visibility == View.GONE)
                                MethodInterface.kernelBars(1, 10, 1)

                            setKernelBarsChangeListeners(methodName, 1, 10, true)
                        }
                        else {
                            binding.FirstSpinnerImage.setImageResource(R.drawable.strel_disk)
                            if (binding.KernelBars.visibility == View.VISIBLE)
                                binding.KernelBars.visibility = View.GONE
                            if (binding.FirstBar.visibility == View.GONE)
                                MethodInterface.firstBar("direct", "Radius", 1, 5, 1)

                            methodsAppliedMap[methodName]!![0] = 1
                            setSeekBarChangeListener(1, "int", 0)
                        }

                        processInGalleryMode()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            "Binary threshold" -> {
                mapMethodValues(128)
                MethodInterface.firstBar("direct", "Threshold value", 1, 254, 128)
                setSeekBarChangeListener(1, "int", 0)
            }

            "Otsu's threshold" -> {
                binding.ExtraComment.visibility = View.VISIBLE
                binding.ExtraComment.text = "Threshold value: _"
            }

            "Hough circle" -> {
                mapMethodValues(50, 100, 20, 50)
                MethodInterface.firstBar ("direct", "Min distance factor", 1, 100, 50)
                MethodInterface.secondBar("direct", "Canny top threshold", 2, 254, 100)
                MethodInterface.thirdBar ("direct", "Min radius"         , 1, 200, 20)
                MethodInterface.fourthBar("direct", "Max radius"         , 1, 200, 50)

                setSeekBarChangeListener(1, "int", 0)
                setSeekBarChangeListener(2, "int", 1)
                setSeekBarChangeListener(3, "int", 2)
                setSeekBarChangeListener(4, "int", 3)
            }

            "Haar transform" -> {
                MethodInterface.firstSpinner("Compression:")
                val compressions = arrayOf("L1 Approximation", "L1 Horizontal", "L1 Vertical", "L1 Diagonal",
                                            "L2 Approximation", "L2 Horizontal", "L2 Vertical", "L2 Diagonal", "All")
                setSpinnerAdapter(compressions)
            }
        }

        processInGalleryMode()
    }

    //==============================================================================================
    //============================== W O R K M O D E :  G A L L E R Y ==============================
    //==============================================================================================

    private fun pickImageGallery () {
        val pickImageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                try {
                    // Set user interface image
                    if (data == null) throw AssertionError()
                    val inputStream = contentResolver.openInputStream(data.data!!)
                    val srcUserBitmap = BitmapFactory.decodeStream(inputStream)
                    binding.userImageView.setImageBitmap(srcUserBitmap)

                    // Get image matrix
                    Utils.bitmapToMat(srcUserBitmap, srcUserImage)
                    srcUserImage.copyTo(currentImage)

                    // Set image information
                    setImageInfo(srcUserImage)

                    // Set resize resolutions for picked image
                    setGalleryResolutions()

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageResultLauncher.launch(intent)
    }

    private fun setGalleryResolutions() {
        val scales = arrayOf(100, 80, 60, 40, 20)
        var width : Array<Int> = Array(scales.size){0}
        var height : Array<Int> = Array(scales.size){0}
        val resolutions : Array<String> = Array(scales.size){ "" }

        for (i in 0 until (scales.size)) {
            width[i]  = (srcUserImage.cols() * scales[i] / 100)
            height[i] = (srcUserImage.rows() * scales[i] / 100)
            resolutions[i] = "${scales[i]}% - ${height[i]} x ${width[i]}"
        }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, resolutions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.imageInfoHeaderSpinner.adapter = spinnerAdapter

        binding.imageInfoHeaderSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    srcUserImage.copyTo(currentImage)
                }
                else {
                    val resolution = org.opencv.core.Size(width[position].toDouble(), height[position].toDouble())
                    Imgproc.resize(srcUserImage, currentImage, resolution, 0.0, 0.0, Imgproc.INTER_AREA)
                }
                isGalleryMode = false
                currentImage = imageProcessing(currentImage)
                isGalleryMode = true
                setUserImage(currentImage)
                setImageInfo(currentImage)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    //==============================================================================================
    //============================ W O R K M O D E :  R E A L - T I ME =============================
    //==============================================================================================

    //---------------------------- S U P P O R T    F U N C T I O N S ------------------------------

    // P R E V I E W

    private fun setPreview () : Preview {
        val tempPreview = Preview.Builder().build()
        tempPreview.setSurfaceProvider(binding.previewView.surfaceProvider)
        return tempPreview
    }

    // I M A G E    C A P T U R E

    /*
    private fun setImageCapture () : ImageCapture {
        val tempImageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetRotation(binding.previewView.display.rotation)
                .build()

        binding.btnSaveImage.setOnClickListener {
            if (binding.llMathParams.visibility == View.GONE) {
                if (binding.llSetOfCategories.visibility == View.VISIBLE ||
                        binding.llSetOfMethods.visibility == View.VISIBLE)
                    binding.btnAddMethod.performClick()
                if (binding.llTracking.visibility == View.VISIBLE)
                    binding.btnTracking.performClick()

                tempImageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        displayCapturedImage(true)

                        val inputImage  = Mat()
                        var outputImage : Mat
                        val capturedBitmap = imageCaptureToBitmap(image)
                        Utils.bitmapToMat(capturedBitmap, inputImage)

                        runOnUiThread {
                            outputImage = imageProcessing(inputImage)
                            if (outputImage.cols() > outputImage.rows())
                                Core.rotate(outputImage, outputImage, Core.ROTATE_90_CLOCKWISE)

                            setUserImage(outputImage)
                            setImageInfo(outputImage)
                            dstUserBitmap = Bitmap.createBitmap(outputImage.cols(), outputImage.rows(), Bitmap.Config.ARGB_8888)
                            Utils.matToBitmap(outputImage, dstUserBitmap) // VARIABLE GLOBAL dstUserBitmap SE USA JUNTO A SAVE IMAGE
                        }
                    }
                })
            }
            else {
                toast(Message.IN_PROGRESS)
            }
        }

        return tempImageCapture
    }

    private fun imageCaptureToBitmap(image : ImageProxy) : Bitmap {
        val byteBuffer = image.planes[0].buffer
        val bytes = ByteArray(byteBuffer.remaining())
        byteBuffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
    }
    */

    private fun displayCapturedImage (freezeCapture : Boolean) {
        if (freezeCapture) {
            runOnUiThread {
                cameraProvider.unbind(preview, imageAnalysis)
                binding.toolBarLayout.visibility = View.GONE
                binding.llCapture.visibility = View.VISIBLE
            }
        }
        else {
            binding.llCapture.visibility = View.GONE
            binding.toolBarLayout.visibility = View.VISIBLE
            binding.previewView.post { startStreaming() }
        }
    }

    // I M A G E    A N A L Y S I S
    private fun setImageAnalysis() : ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(resolutionSize)
            .build()

        imageAnalysis.setAnalyzer(executor, { image ->
            val analyzerBitmap : Bitmap = imageAnalysisToBitmap(image)
            val inputImage = Mat()
            Utils.bitmapToMat(analyzerBitmap, inputImage)

            runOnUiThread {
                this.outputImage = imageProcessing(inputImage)
                setUserImage(outputImage)
                setImageInfo(outputImage)
            }
            image.close()
        })
        return imageAnalysis
    }

    /*
    private fun setImageAnalysis() : ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(resolutionSize)
                .build()

        imageAnalysis.setAnalyzer(executor, { image ->
            val analyzerBitmap : Bitmap = imageAnalysisToBitmap(image)
            val inputImage = Mat()
            Utils.bitmapToMat(analyzerBitmap, inputImage)

            runOnUiThread {
                val outputImage = imageProcessing(inputImage)
                setUserImage(outputImage)
                setImageInfo(outputImage)
                this.rows = outputImage.rows()
                this.cols = outputImage.cols()
            }
            image.close()
        })
        return imageAnalysis
    }

     */

    @SuppressLint("UnsafeOptInUsageError")
    private fun imageAnalysisToBitmap (imageProxy : ImageProxy) : Bitmap {
        val image   : Image = imageProxy.image!!
        val planes  : Array<Image.Plane>  = image.planes
        val yBuffer : ByteBuffer = planes[0].buffer
        val uBuffer : ByteBuffer = planes[1].buffer
        val vBuffer : ByteBuffer = planes[2].buffer
        val ySize   : Int = yBuffer.remaining()
        val uSize   : Int = uBuffer.remaining()
        val vSize   : Int = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)

        val imageBytes = out.toByteArray()
        val analysisBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        matrix.setRotate(90F)
        return Bitmap.createBitmap(analysisBitmap, 0, 0, analysisBitmap.width, analysisBitmap.height, matrix, true)
    }

    //------------------------------- M A I N    F U N C T I O N S ---------------------------------

    private fun startStreaming () {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
                try {
                    cameraProvider = cameraProviderFuture.get()
                    val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

                    preview       = setPreview()
                    //imageCapture  = setImageCapture()
                    imageAnalysis = setImageAnalysis()

                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(this@ProcessingActivity, cameraSelector, preview, imageAnalysis)
                    //val camera = cameraProvider.bindToLifecycle(this@ProcessingActivity, cameraSelector, preview, imageCapture, imageAnalysis)

                    // This commented code is to find out possible output resolution sizes and it is related to Camera2
                    // What has been tried? :
                    // Even if resolution size is 5000x5000, we get a reduced resolution around 1080x1080.
                    // Also there are many, non-symmetrical, types of resolution which were shown using Toast.
                    /*
                    val cameraId = Camera2CameraInfo.from(camera.cameraInfo).cameraId
                    val cameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    val configs: StreamConfigurationMap? = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

                    val imageAnalysisSizes = configs?.getOutputSizes(ImageFormat.YUV_420_888)
                    imageAnalysisSizes?.forEach {
                        Toast.makeText(this, "Image capturing YUV_420_888 available output size: $it", Toast.LENGTH_LONG).show()
                    }
                     */

                } catch (e : ExecutionException) {}
            }, ContextCompat.getMainExecutor(this)
        )
    }

    private fun allPermissionsGranted () : Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) startStreaming()
        }
        else {
            Toast.makeText(this, "Permissions not granted by user", Toast.LENGTH_SHORT).show()
            this.finish()
        }
    }

    //==============================================================================================
    //=============================  I M A G E     P R O C E S S I N G  ============================
    //==============================================================================================

    private fun imageProcessing (inputImage : Mat) : Mat {

        return if (methodsAppliedArray.size > 0) {
                    val index : Int = if (isGalleryMode) { methodsAppliedArray.lastIndex } else { 0 }
                    imageProcessingLoop(inputImage, index)
                }
                else {
                    inputImage
                }
    }

    private fun imageProcessingLoop (inputImage : Mat, index : Int) : Mat {
        var image  = inputImage

        fun isGray () : Boolean {
            if (image.channels() > 1) {
                binding.btnCancelMethod.performClick()
                toast(Message.GRAY_NEEDED)
                return false
            }
            return true
        }

        fun isRGB () : Boolean {
            if (image.channels() == 1) {
                binding.btnCancelMethod.performClick()
                toast(Message.RGB_NEEDED)
                return false
            }
            return true
        }

        when (val method = methodsAppliedArray[index]) {
            "To grayscale" -> {
                if (image.channels() == 4)
                    Imgproc.cvtColor(image, image, Imgproc.COLOR_RGBA2GRAY)
                else if (image.channels() == 3)
                    Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY)
            }

            "Negative"               -> image = BasicMethods.negate(image)
            "Log"                    -> image = BasicMethods.log(image)
            "Inverse log"            -> image = BasicMethods.inverseLog(image)

            "Histogram equalization" -> if (isGray()) image = BasicMethods.equilizeHist(image)

            "Power-law"  -> {
                var gamma = methodsAppliedMap[method]!![0].toDouble()
                gamma /= 10
                image = BasicMethods.powerLaw(image, gamma)
            }

            "Contrast stretching" -> {
                val r1 = methodsAppliedMap[method]!![0]
                val s1 = methodsAppliedMap[method]!![1]
                val r2 = methodsAppliedMap[method]!![2]
                val s2 = methodsAppliedMap[method]!![3]
                image = BasicMethods.contrastStretching(image, r1, s1, r2, s2)
            }

            "Bit-plane slicing" -> {
                if (isGray()) {
                    val plane = methodsAppliedMap[method]!![0]
                    image = BasicMethods.bitPlaneSlicing(image, plane)
                }
            }

            "Mean blur" -> {
                val width  = methodsAppliedMap[method]!![0]
                val height = methodsAppliedMap[method]!![1]
                image = SpatialMethods.boxFilter(image, width, height)
            }

            "Gaussian blur" -> {
                val width  = methodsAppliedMap[method]!![0]
                val height = methodsAppliedMap[method]!![1]
                val sigma  = methodsAppliedMap[method]!![2].toDouble() / 10
                image = SpatialMethods.GaussianBlur(image, width, height, sigma)
            }

            "Laplacian sharp" -> image = SpatialMethods.spatialLaplacian(image, methodsAppliedMap[method]!![0])

            "Gradient sharp" -> if (isGray()) image = SpatialMethods.firstDerivative(image, binding.FirstSpinnerSpinner.selectedItemPosition)

            // ========================================== FREQUENCY ===========================================
            "Ideal lowpass", "Ideal highpass" -> {
                if (isGray()) {
                    val cutOffFrequency = methodsAppliedMap[method]!![0]
                    val isLowpass       = methodsAppliedMap[method]!![1] == 1

                    val idealFilter = FrequencyMethods.freqIdealFilter(isLowpass, image, cutOffFrequency)
                    image = FrequencyMethods.convolveDFT(image, idealFilter)
                }
            }

            "Butterworth lowpass", "Butterworth highpass" -> {
                if (isGray()) {
                    val cutOffFrequency = methodsAppliedMap[method]!![0]
                    val isLowpass = methodsAppliedMap[method]!![1] == 1
                    val n = methodsAppliedMap[method]!![2]

                    val butterworthFilter = FrequencyMethods.freqButterworthFilter(isLowpass, image, cutOffFrequency, n)
                    image = FrequencyMethods.convolveDFT(image, butterworthFilter)
                }
            }

            "Gaussian lowpass", "Gaussian highpass" -> {
                if (isGray()) {
                    val cutOffFrequency = methodsAppliedMap[method]!![0]
                    val isLowpass = methodsAppliedMap[method]!![1] == 1

                    val gaussianFilter = FrequencyMethods.freqGaussianFilter(isLowpass, image, cutOffFrequency)
                    image = FrequencyMethods.convolveDFT(image, gaussianFilter)
                }
            }

            "Laplacian" -> {
                if (isGray()) {
                    val laplaceFilter = FrequencyMethods.freqLaplacianFilter(image)
                    image = FrequencyMethods.convolveDFT(image, laplaceFilter)
                }
            }

            "Geometric mean" -> {
                val width  = methodsAppliedMap[method]!![0]
                val height = methodsAppliedMap[method]!![1]
                image = NoiseMethods.geometricMeanFilter(image, width, height)
            }

            "Contraharmonic mean" -> {
                val width  = methodsAppliedMap[method]!![0]
                val height = methodsAppliedMap[method]!![1]
                val q      = methodsAppliedMap[method]!![2].toDouble() / 10
                image = NoiseMethods.contraharmonicMeanFilter(image, q, width, height)
            }

            "Median" -> image = NoiseMethods.medianFilter(image, methodsAppliedMap[method]!![0])

            "Min" -> {
                val width  = methodsAppliedMap[method]!![0]
                val height = methodsAppliedMap[method]!![1]
                image = NoiseMethods.minFilter(image, width, height)
            }

            "Max" -> {
                val width  = methodsAppliedMap[method]!![0]
                val height = methodsAppliedMap[method]!![1]
                image = NoiseMethods.maxFilter(image, width, height)
            }

            "Midpoint" -> {
                val width  = methodsAppliedMap[method]!![0]
                val height = methodsAppliedMap[method]!![1]
                image = NoiseMethods.midpointFilter(image, width, height)
            }

            "RGB planes" -> if (isRGB()) image = ColorMethods.RGBtoPlanes(image, binding.FirstSpinnerSpinner.selectedItemPosition)

            "RGB to HSI" -> if (isRGB()) image = ColorMethods.RGBtoHSI(image, binding.FirstSpinnerSpinner.selectedItemPosition)

            "Intensity slicing" -> {
                if (isGray()) {
                    val intensity1 = methodsAppliedMap[method]!![0]
                    val intensity2 = methodsAppliedMap[method]!![1]
                    val intensity3 = methodsAppliedMap[method]!![2]
                    image = ColorMethods.intensitySlicing(image, intensity1, intensity2, intensity3)
                }
            }

            "Intensity to color" -> {
                if (isGray()) {
                    val freq = methodsAppliedMap[method]!![0].toDouble() / 10
                    val shiftR = methodsAppliedMap[method]!![1].toDouble() / 10
                    val shiftG = methodsAppliedMap[method]!![2].toDouble() / 10
                    val shiftB = methodsAppliedMap[method]!![3].toDouble() / 10
                    image = ColorMethods.intensityToColor(image, freq, shiftR, shiftG, shiftB)
                }
            }

            "Color balance" -> if (isRGB()) image = ColorMethods.colorBalance(image, 5F)

            "Erosion", "Dilation", "Opening", "Closing", "Smoothing", "Gradient" -> {
                val width  = methodsAppliedMap[method]!![0] // or radius
                val height = methodsAppliedMap[method]!![1]
                val strel  = MorphologyMethods.getStrel(strelShape, width, height)
                image = MorphologyMethods.morphologyMethod(method, image, strel)
            }

            "Binary threshold" -> if (isGray()) image = SegmentationMethods.binaryThreshold(image, methodsAppliedMap[method]!![0])

            "Otsu's threshold" -> {
                if (isGray()) {
                    image = SegmentationMethods.otsuThreshold(image)
                    var otsuText = SegmentationMethods.getOtsuValue().toString()
                    otsuText = "Threshold value: $otsuText"
                    binding.ExtraComment.text = otsuText
                }
            }

            "Hough circle" -> {
                val minDistFactor = methodsAppliedMap[method]!![0]
                val cannyParam    = methodsAppliedMap[method]!![1]
                val minRadius     = methodsAppliedMap[method]!![2]
                val maxRadius     = methodsAppliedMap[method]!![3]
                image = SegmentationMethods.houghCircle(image, minDistFactor, cannyParam, minRadius, maxRadius)
            }

            "Haar transform" -> if (isGray()) image = Wavelets.haarForward(image, binding.FirstSpinnerSpinner.selectedItemPosition)
        }

        return  if ((index+1) < methodsAppliedArray.size)
                    imageProcessingLoop(image, index+1)
                else
                    image
    }
}