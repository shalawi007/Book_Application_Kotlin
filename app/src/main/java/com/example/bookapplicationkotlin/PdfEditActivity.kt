package com.example.bookapplicationkotlin

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.bookapplicationkotlin.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityPdfEditBinding

    private companion object{
        private const val TAG = "PDF_EDIT_TAG"
    }

    //book id from adapter pdf admin
    private var bookId = ""

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //arraylist for category titles
    private lateinit var categoryTitleArrayList:ArrayList<String>

    //arraylist for category ids
    private lateinit var categoryIdArrayList:ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        //setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait..")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        //handle click back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        //handle pick category click
        binding.categoryTv.setOnClickListener{
            categoryDialog()
        }

        //handle click for update
        binding.submitBtn.setOnClickListener {
            validateData()

        }

    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading Book info...")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("categoryTitle").value.toString()

                    //set to views
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    // load book category
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //get category
                                val category = snapshot.child("category").value
                                //set to txtview
                                binding.categoryTv.text = category.toString()

                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var title = ""
    private var description = ""

    private fun validateData() {
        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        //validate
        if (title.isEmpty()){
            Toast.makeText(this, "Enter Title", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()){
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show()

        }
        else if (selectedCategoryId.isEmpty()){
            Toast.makeText(this, "Pick Category", Toast.LENGTH_SHORT).show()

        }
        else{
            updatePdf()
        }

    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf info....")

        //show progress
        progressDialog.setMessage("Updating book info")
        progressDialog.show()

        //setup data
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        //start updating
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Updated Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""


    private fun categoryDialog() {
        /*Show dialog for category pick*/

        //string array from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]

        }
        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray){dialog, pos->
                //handle click for saving
                selectedCategoryId = categoryIdArrayList[pos]
                selectedCategoryTitle = categoryTitleArrayList[pos]

                //set to txtview
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()

    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories...")

        categoryIdArrayList = ArrayList()
        categoryTitleArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children){
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)
                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}