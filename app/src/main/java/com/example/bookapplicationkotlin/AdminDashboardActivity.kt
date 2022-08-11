package com.example.bookapplicationkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.bookapplicationkotlin.databinding.ActivityAdminDashboardBinding
import com.example.bookapplicationkotlin.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminDashboardActivity : AppCompatActivity() {

    //viewBinding
    private lateinit var binding: ActivityAdminDashboardBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    //adapter
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //called when user type
                try {
                    adapterCategory.filter.filter(s)


                }
                catch (e: Exception){

                }
            }

            override fun afterTextChanged(p0: Editable?) {
                TODO("Not yet implemented")
            }
        })

        //Logout click
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        // Add Category Click
        binding.addCatBtn.setOnClickListener {
            startActivity(Intent(this,CategoryAddActivity::class.java))

        }
        // add pdf page click
        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))
        }

    }

    private fun loadCategories() {
        //init arraylist
        categoryArrayList = ArrayList()

        //get all categories from FB db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before data adding
                categoryArrayList.clear()
                for (ds in  snapshot.children){
                    //get data as model
                    val model = ds.getValue(ModelCategory::class.java)

                    //add to arraylist
                    categoryArrayList.add(model!!)
                }
                //setup adapter
                adapterCategory = AdapterCategory(this@AdminDashboardActivity,categoryArrayList)
                //set adapter to recycler view
                binding.categoriesRv.adapter = adapterCategory
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

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