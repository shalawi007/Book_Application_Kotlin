package com.example.bookapplicationkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookapplicationkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    // login click
        binding.logBtn.setOnClickListener {

        }
    // skip click to continue to main screen
        binding.skipBtn.setOnClickListener {

        }

    }
}