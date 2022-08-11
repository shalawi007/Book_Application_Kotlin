package com.example.bookapplicationkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookapplicationkotlin.databinding.ActivityAdminDashboardBinding
import com.example.bookapplicationkotlin.databinding.ActivityUserDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class UserDashboardActivity : AppCompatActivity() {

    //viewBinding
    private lateinit var binding:ActivityUserDashboardBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //Logout click
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    private fun checkUser() {
        // get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            //not logged, Anonymous user can stay
            binding.subTitleTv.text = "Not Logged in"
        }
        else{
            // logged in
            val email = firebaseUser.email
            binding.subTitleTv.text =email
        }
    }
}