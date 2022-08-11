package com.example.bookapplicationkotlin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bookapplicationkotlin.databinding.ActivityPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.sql.Timestamp

class PdfAddActivity : AppCompatActivity() {

    //viewBinding
    private lateinit var binding: ActivityPdfAddBinding

    //firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth

    // progress dialog
    private lateinit var progressDialog: ProgressDialog

    //arraylist to hold pdf categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //uri of picked pdf
    private var pdfUri:Uri? = null

    //TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        //setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // handle back click
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click show category pick dialog
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }
        //handle click pick pdf intent
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }
        //handle click, start uploading pdf
        binding.submitBtn.setOnClickListener {
            /*1- Validate Data
            * 2- upload pdf to Firebase
            * 3- Get URL of uploaded pdf
            * 4- Upload pdf into firebase db*/
            validateData()
        }


    }
    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        //1- Validate Data
        Log.d(TAG, "validateData: validating Data")
        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        //validate data
        if (title.isEmpty()){
            Toast.makeText(this, "Enter title...", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()){
            Toast.makeText(this, "Enter description...", Toast.LENGTH_SHORT).show()
        }
        else if (category.isEmpty()){
            Toast.makeText(this, "Pick Title...", Toast.LENGTH_SHORT).show()
        }
        else if (pdfUri == null)
            Toast.makeText(this, "Pick PDF...", Toast.LENGTH_SHORT).show()
        else{
            //begin upload
            uploadPdfDbStorage()
        }



    }

    private fun uploadPdfDbStorage() {
        //2- upload pdf to Firebase
        Log.d(TAG, "uploadPdfDb: uploading to storage...")

        //show progress dialog
        progressDialog.setMessage("Uploading Pdf...")
        progressDialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()

        //pdf path in fb
        val filePathAndName ="Books/$timestamp"
        //storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot->
                Log.d(TAG, "uploadPdfDb: PDF uploaded now getting url...")

                //3- Get URL of uploaded pdf
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb(uploadedPdfUrl, timestamp)
                Toast.makeText(this, "Enter title...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Log.d(TAG, "uploadPdfDb:  Failed to upload ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}...", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl:String, timestamp: Long) {
        //4- Upload pdf into firebase db
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db..")
        progressDialog.setMessage("Uploading Pdf info...")

        //uid of current user
        val uid = firebaseAuth.uid

        //setup data to upload
        val hashMap: HashMap<String,Any?> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        //db reference DB
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: uploaded successfully...")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded Successfully...", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e->
                Log.d(TAG, "uploadPdfInfoToDb:  Failed to upload ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}...", Toast.LENGTH_SHORT).show()
            }


    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading Pdf Categories")
        //init Arraylist
        categoryArrayList = ArrayList()

        //db reference to load Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before data adding
                categoryArrayList.clear()
                for (ds in  snapshot.children) {
                    //get data as model
                    val model = ds.getValue(ModelCategory::class.java)

                    //add to arraylist
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog (){
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        //get string array of categories from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices){
            categoriesArray[i] = categoryArrayList[i].category
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray){dialog, which->
                //handle item click
                //get clicked item
                selectedCategoryId = categoryArrayList[which].category
                selectedCategoryTitle = categoryArrayList[which].id
                //set category to txt view
                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")

            }
            .show()
    }

    private fun pdfPickIntent(){
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)

    }
    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ result ->
            if (result.resultCode == RESULT_OK){
                Log.d(TAG, "PDF PICKED")
                pdfUri = result.data!!.data
            }
            else{
                Log.d(TAG, "PDF PICK CANCELLED")
                Toast.makeText(this, "Cancelled ...", Toast.LENGTH_SHORT).show()
            }

        }
    )
}