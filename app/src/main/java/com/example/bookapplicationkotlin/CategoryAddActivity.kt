package com.example.bookapplicationkotlin

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookapplicationkotlin.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddActivity : AppCompatActivity() {

    //viewBinding
    private lateinit var binding: ActivityCategoryAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //back btn click
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //update begin click
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }
    private var category = ""

    private fun validateData() {

        //get data
        category = binding.categoryEt.text.toString().trim()

        //validate
        if (category.isEmpty()){
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        }
        else{
            addCategoryFb()
        }
    }

    private fun addCategoryFb() {
        //show progress
        progressDialog.show()

        //get Timestamp
        val timestamp = System.currentTimeMillis()

        //setup hashmap
        val hashMap = HashMap<String,Any>() // value could be any type
        hashMap ["id"] = "$timestamp" // string quote because timestamp is double, string for id
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        //add to firebase db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                // update success
                progressDialog.dismiss()
                Toast.makeText(this, "Added Successfully...", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e->
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}