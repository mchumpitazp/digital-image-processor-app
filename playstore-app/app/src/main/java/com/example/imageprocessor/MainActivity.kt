package com.example.imageprocessor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.imageprocessor.databinding.ActivityMainBinding
import android.view.LayoutInflater

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this@MainActivity))
        setContentView(binding.root)

        binding.galleryView.setOnClickListener {
            val intent = Intent(this@MainActivity, ProcessingActivity::class.java)
            intent.putExtra("isGalleryMode", true)
            startActivity(intent)
        }

        binding.realTimeView.setOnClickListener {
            val intent = Intent(this@MainActivity, ProcessingActivity::class.java)
            intent.putExtra("isGalleryMode", false)
            startActivity(intent)
        }
    }
}