package com.example.bookapplicationkotlin

import android.content.Intent
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
            startActivity(Intent(this,LoginActivity::class.java))

        }
    // skip click to continue to main screen
        binding.skipBtn.setOnClickListener {
            startActivity(Intent(this,UserDashboardActivity::class.java))

        }

    }
}