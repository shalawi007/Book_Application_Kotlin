package com.example.bookapplicationkotlin

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.example.bookapplicationkotlin.databinding.ActivityProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {

    //binding
    private lateinit var binding: ActivityProfileEditBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //image uri (picked image)
    private var imageUri:Uri? = null

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Pleas wait..")
        progressDialog.setCanceledOnTouchOutside(false)

        loadUserInfo()

        //handle back button
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        //handle pick image from gallery or camera
        binding.profileIv.setOnClickListener{
            showImageAttachMenu()
        }

        //handle update button
        binding.updateBtn.setOnClickListener {
            validateData()

        }
    }

    private var name = ""
    private fun validateData() {
        //get data
        name = binding.nameEt.text.toString().trim()

        //validate
        if (name.isEmpty()){
            //name not entered
            Toast.makeText(this,"Enter name please..",Toast.LENGTH_SHORT).show()
        }
        else{
            if (imageUri == null){
                //update without image
                updateProfile("")

            }
            else{

                uploadImage()
            }
        }

    }

    private fun uploadImage() {
        progressDialog.setMessage("Uploading profile image")
        progressDialog.show()

        //image path & name, uid for replace
        val filePathAndName = "ProfileImages/"+firebaseAuth.uid

        //storage ref
        val  reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                //image uploaded, get Url
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"
                updateProfile(uploadedImageUrl)

            }
            .addOnFailureListener { e->
                //failed to upload
                progressDialog.dismiss()
                Toast.makeText(this,"Failed to upload due to${e.message}",Toast.LENGTH_SHORT).show()

            }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Updating profile...")

        //setup info to update to db
        val hashMap:HashMap<String, Any> = HashMap()
        hashMap["name"] = "$name"
        if (imageUri != null){
            hashMap["profileImage"] = uploadedImageUrl
        }

        //update to db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()


            }
            .addOnFailureListener { e->
                //failed to upload
                progressDialog.dismiss()
                Toast.makeText(this,"Failed to update profile due to${e.message}",Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadUserInfo() {
        //db reference
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"


                    //set data
                    binding.nameEt.setText(name)

                    //set Image
                    try {
                        Glide.with(this@ProfileEditActivity).load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)

                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun showImageAttachMenu(){
        /*Show pop up menu with options camera, Gallery*/

        //setup popup menu
        val popupMenu = PopupMenu(this, binding.profileIv)
        popupMenu.menu.add(Menu.NONE,0,0,"Camera")
        popupMenu.menu.add(Menu.NONE,1,1,"Gallery")
        popupMenu.show()

        //handle popupmenu item click
        popupMenu.setOnMenuItemClickListener { item->
            //get id of clicked item
            val id = item.itemId
            if (id == 0){
                //camera click
                pickCamera()

            }
            else if (id == 1){
                // gallery click
                pickGallery()

            }


            true
        }


    }

    private fun pickGallery() {
        //intent to pick img from Gallery
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)


    }


    private fun pickCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)

    }

    // handle result of camera intent
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            //get uri of image
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                //imageUri = data!!.data

                //set to image view
                binding.profileIv.setImageURI(imageUri)
            } else {
                //cancelled
                Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show()
            }

        }
    )

    //handle result of gallery intent
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            //get uri of image
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                //imageUri = data!!.data

                //set to image view
                binding.profileIv.setImageURI(imageUri)
            } else {
                //cancelled
                Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show()
            }

        }
    )
}