package com.example.imageprocessor

import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.example.imageprocessor.databinding.ActivityProcessingBinding

class MethodInterface (inputBinding : ActivityProcessingBinding) {
    private val binding = inputBinding

    private fun displayBar (ll : LinearLayout, barName : TextView, bar : SeekBar,
                    name : String, min : Int, progress : Int, max : Int) {
        ll.visibility = View.VISIBLE
        barName.text  = name
        bar.min       = min
        bar.progress  = progress
        bar.max       = max
    }

    private fun setBarLimits (minValue : TextView, progressValue : TextView, maxValue : TextView,
                      type : String, min : Int, progress : Int, max : Int) {
        var minText      = "No init"
        var progressText = "No init"
        var maxText      = "No init"

        when (type) {
            "direct" -> {
                minText      = min.toString()
                progressText = progress.toString()
                maxText      = max.toString()
            }
            "double" -> {
                minText      = (min.toDouble() / 10).toString()
                progressText = (progress.toDouble() / 10).toString()
                maxText      = (max.toDouble() / 10).toString()
            }
            "kernel" -> {
                minText      = (min * 2 + 1).toString()
                progressText = (progress * 2 + 1).toString()
                maxText      = (max * 2 + 1).toString()
            }
            "norm_freq" -> {
                minText      = "0"
                progressText = "0.500"
                maxText      = "1"
            }
        }

        minValue.text      = minText
        progressValue.text = progressText
        maxValue.text      = maxText
    }

    fun baseInterface () {
        if (binding.llMathParams.visibility == View.GONE) {
            binding.llSetOfMethods.visibility = View.GONE
            binding.llMathParams.visibility = View.VISIBLE
        }
        else {
            binding.llSetOfMethods.visibility = View.VISIBLE
            binding.llMathParams.visibility = View.GONE
        }
    }


    fun firstBar (type : String, name : String, min : Int, max : Int, progress : Int) {
        displayBar(binding.FirstBar, binding.FirstBarName, binding.FirstBarBar, name, min, progress, max)
        setBarLimits(binding.FirstBarMinValue, binding.FirstBarProgress, binding.FirstBarMaxValue, type, min, progress, max)
    }

    fun secondBar (type : String, name : String, min : Int, max : Int, progress : Int) {
        displayBar(binding.SecondBar, binding.SecondBarName, binding.SecondBarBar, name, min, progress, max)
        setBarLimits(binding.SecondBarMinValue, binding.SecondBarProgress, binding.SecondBarMaxValue, type, min, progress, max)
    }

    fun thirdBar (type : String, name : String, min : Int, max : Int, progress : Int) {
        displayBar(binding.ThirdBar, binding.ThirdBarName, binding.ThirdBarBar, name, min, progress, max)
        setBarLimits(binding.ThirdBarMinValue, binding.ThirdBarProgress, binding.ThirdBarMaxValue, type, min, progress, max)
    }

    fun fourthBar (type : String, name : String, min : Int, max : Int, progress : Int) {
        displayBar(binding.FourthBar, binding.FourthBarName, binding.FourthBarBar, name, min, progress, max)
        setBarLimits(binding.FourthBarMinValue, binding.FourthBarProgress, binding.FourthBarMaxValue, type, min, progress, max)
    }

    fun kernelBars (min : Int, max : Int, progress : Int) {
        val minText  = (min * 2 + 1).toString()
        val maxText  = (max * 2 + 1).toString()
        var progText = (progress * 2 + 1).toString()
        progText += " x $progText"

        binding.KernelBars.visibility = View.VISIBLE
        binding.KernelHeightBar.min = min
        binding.KernelWidthBar.min  = min
        binding.KernelHeightBar.max = max
        binding.KernelWidthBar.max  = max
        binding.KernelHeightBar.progress = progress
        binding.KernelWidthBar.progress  = progress

        binding.KernelMinValue.text = minText
        binding.KernelMaxValue.text = maxText
        binding.KernelProgress.text = progText
    }

    fun firstSpinner (spinnerText : String) {
        binding.FirstSpinner.visibility = View.VISIBLE
        binding.FirstSpinnerText.text = spinnerText
        binding.FirstSpinnerImage.visibility = View.GONE
    }

}