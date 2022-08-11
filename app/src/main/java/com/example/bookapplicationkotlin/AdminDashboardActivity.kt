package com.example.bookapplicationkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookapplicationkotlin.databinding.ActivityAdminDashboardBinding
import com.example.bookapplicationkotlin.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardActivity : AppCompatActivity() {

    //viewBinding
    private lateinit var binding: ActivityAdminDashboardBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //Logout click
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        // Add Category Click
        binding.addCatBtn.setOnClickListener {
            startActivity(Intent(this,CategoryAddActivity::class.java))

        }

    }

    private fun checkUser() {
        // get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            //not logged in, direct to main
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
        else{
            // logged in
            val email = firebaseUser.email
            binding.subTitleTv.text =email
        }
    }
}