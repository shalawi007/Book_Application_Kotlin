package com.example.bookapplicationkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.example.bookapplicationkotlin.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {

    // view Binding
    private lateinit var binding:ActivityPdfViewBinding

    //TAG
    private companion object{
        const val TAG ="PDF_VIEW_TAG"
    }
    var bookId = ""


    //progress bar

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get book id from intent
        bookId = intent.getStringExtra("bookId")!!
        loadBookDetails()

        //handle go back click
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }


    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get Pdf URL from db")

        //get book url with book id
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book url
                    val pdfUrl = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: PDF_URL: $pdfUrl")

                    //load pdf
                    loadPdfFromUrl("$pdfUrl")

                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadPdfFromUrl(pdfUrl: String) {
        Log.d(TAG, "loadPdfFromUrl: Get Pdf from fb storage using Url")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.d(TAG, "loadPdfFromUrl: pdf acquired from url")

                //load pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)//set false to scroll vertical
                    .onPageChange{page, pageCount->
                        //set current page and total pg in tb subtitle
                        val currentPage = page + 1
                        binding.subTitleTv.text = "$currentPage/$pageCount" // e.g 3/33
                        Log.d(TAG, "loadPdfFromUrl: $currentPage/$pageCount")
                    }
                    .onError { t->
                        Log.d(TAG, "loadPdfFromUrl: Failed cause: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG, "loadPdfFromUrl: Failed cause: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e->
                Log.d(TAG, "loadPdfFromUrl: Failed to get url due to ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
    }
}