package com.example.bookapplicationkotlin

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapplicationkotlin.databinding.ActivityLoginBinding
import com.example.bookapplicationkotlin.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    //viewBinding
    private lateinit var binding: ActivityLoginBinding

    //firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth

    // progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog for app status
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //No Account Click
        binding.noAccountTxv.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }

        //Login Click
        binding.loginBtn.setOnClickListener {
            /*Steps
            * 1- Input data
            * 2- Validate data
            * 3- Login - Firebase Auth
            * 4- Check user type- Firebase Auth*/
            validateData()
        }
    }
    private var email = ""
    private var pass = ""

    private fun validateData() {
        // 1- Input data
        email = binding.emailEd.text.toString().trim()
        pass = binding.passwordEd.text.toString().trim()

        //2- Validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email ...", Toast.LENGTH_SHORT).show()
        }
        else if (pass.isEmpty()){
            Toast.makeText(this, "Enter Password...", Toast.LENGTH_SHORT).show()

        }
        else{
            loginUser()
        }

    }

    private fun loginUser() {
        //3- Login - Firebase Auth
        progressDialog.setMessage("Logging In ...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email,pass)
            .addOnSuccessListener {
                // check db
                checkUser()
            }
            .addOnFailureListener { e->
                // account create fail
                progressDialog.dismiss()
                Toast.makeText(this,"Login failed due to..${e.message}",Toast.LENGTH_SHORT).show()
            }


    }

    private fun checkUser() {
        /*4- Check user type- Firebase Auth
        * if User- Move to Dashboard
        * If Admin - Move to Admin Dashboard*/
        progressDialog.setMessage("Checking User...")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object :ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    //get user type
                    val userType = snapshot.child("userType").value
                    if (userType == "user"){

                        startActivity(Intent(this@LoginActivity,UserDashboardActivity::class.java))
                        finish()
                    }
                    else if (userType == "admin"){
                        startActivity(Intent(this@LoginActivity,AdminDashboardActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


    }
}