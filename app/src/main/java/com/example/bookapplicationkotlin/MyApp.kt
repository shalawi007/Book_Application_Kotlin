package com.example.bookapplicationkotlin

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.icu.text.CaseMap
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.sql.Timestamp
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.log

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {

        // format time function
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            //format dd/mm/yyyy
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        //get pdf size function
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"
            //using url for file metadata in FB
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetaData ->
                    Log.d(TAG, "loadPdfSize: got metadata...")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size byte $bytes")

                    //convert bytes to KB/MB
                    val kb = bytes / 1024
                    val mb = kb / 1024
                    if (mb > 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)} Bytes"
                    }
                }
                .addOnFailureListener { e ->
                    //failed to get Metadata
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message} ")

                }
        }

        // load page
        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ){
            val TAG = "PDF_THUMBNAIL_TAG"

            //using url for file metadata in FB
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "loadPdfSize: got metadata...")
                    Log.d(TAG, "loadPdfSize: Size byte $bytes")

                    //set to pdfview
                    pdfView.fromBytes(bytes)
                        .pages(0)//show first page only
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages->
                            Log.d(TAG, "loadPdfFromUrlSinglePage: Pages $nbPages")
                            // pdf Loaded,set page count & thumbnail
                            progressBar.visibility = View.INVISIBLE

                            if (pagesTv!= null){
                                pagesTv.text = "$nbPages"
                            }

                        }
                        .load()
                }
                .addOnFailureListener { e ->
                    //failed to get Metadata
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message} ")

                }
        }
        fun loadCategory(categoryId: String, categoryTv: TextView){
            //load category using categoryId from FB
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        val category = "${snapshot.child("category").value}"
                        //set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

        }


        fun deleteBook(context: Context,bookId: String, bookUrl: String, bookTitle: String){
            /*param details*/

            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: deleteing...")

            //progressdiaglog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Deleting from storage...")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: deleted from storage..")
                    Log.d(TAG, "deleteBook: Deleting from db now ....")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: deleted successfully .. ")
                            Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {e->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Failed to delete due to ${e.message}")
                            Toast.makeText(context, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                }
                .addOnFailureListener {e->
                    progressDialog.dismiss()
                    Log.d(TAG, "deleteBook: Failed to delete due to ${e.message}")
                    Toast.makeText(context, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()

                }
        }

        fun incrementBookViewCount(bookId: String){
            //get current views
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount == ""|| viewsCount == "null"){
                            viewsCount ="0";

                        }

                        //2 increment views count
                        val newViewsCount = viewsCount.toLong()+1

                        //setup data for update
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

    }
}