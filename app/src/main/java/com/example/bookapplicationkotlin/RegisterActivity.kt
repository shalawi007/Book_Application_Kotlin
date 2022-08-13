package com.example.bookapplicationkotlin

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapplicationkotlin.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    //viewBinding
    private lateinit var binding: ActivityRegisterBinding

    //firebase Auth
    private lateinit var firebaseAuth:FirebaseAuth

    // progress dialog
    private lateinit var progressDialog:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //back button click
        binding.backBtn.setOnClickListener {
            onBackPressed() // direct previous screen
        }

        // Register btn click
        binding.regBtn.setOnClickListener {
            /*Steps
            * 1- Input data
            * 2- Validate data
            * 3- Create Account - Firebase Auth
            * 4- Save user info- Firebase Realtime db*/
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var pass = ""

    private fun validateData(){
        // 1- Input
        name = binding.nameEd.text.toString().trim()
        email = binding.emailEd.text.toString().trim()
        pass = binding.passwordEd.text.toString().trim()
        val cPass = binding.cPasswordEd.text.toString().trim()

        //2- Validate
        if (name.isEmpty()){
            Toast.makeText(this, "Enter your name...", Toast.LENGTH_SHORT).show()
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // invalid email pattern
            Toast.makeText(this,"Invalid Email ...",Toast.LENGTH_SHORT).show()
        }
        else if (pass.isEmpty()){
            Toast.makeText(this, "Enter your password...", Toast.LENGTH_SHORT).show()
        }
        else if (cPass.isEmpty()){
            Toast.makeText(this, "Confirm Password...", Toast.LENGTH_SHORT).show()
        }
        else if (pass != cPass){
            Toast.makeText(this, "Unmatched Password...", Toast.LENGTH_SHORT).show()
        }
        else{
            createUserAccount()
        }


    }
    private fun createUserAccount(){
        // 3- Create Account - Firebase Auth

        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

        //create User in firebase
        firebaseAuth.createUserWithEmailAndPassword(email,pass)
            .addOnSuccessListener {
                // update db
                updateUserInfo()
            }
            .addOnFailureListener { e->
                // account create fail
                progressDialog.dismiss()
                Toast.makeText(this,"Account failed to create..${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        //4- Save user info- Firebase Realtime db

        progressDialog.setMessage("Saving User Info...")

        //timestamp
        val timestamp = System.currentTimeMillis()

        // get current user id
        val uid = firebaseAuth.uid

        //data structure in db
        val hashMap:HashMap<String,Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" // empty add after
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        //set data to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                // user info saved
                progressDialog.dismiss()
                Toast.makeText(this,"Account created successfully..",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, UserDashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                //failure on data add to db
                progressDialog.dismiss()
                Toast.makeText(this,"Failed saving user info due to${e.message}",Toast.LENGTH_SHORT).show()
            }

    }
}