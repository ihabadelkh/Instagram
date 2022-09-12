package com.example.instagram.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.databinding.ActivityCreatBinding
import com.example.instagram.model.Post
import com.example.instagram.model.UserModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class CreateActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityCreatBinding
    private lateinit var uploadedUri: Uri
    private lateinit var database: DatabaseReference
    private lateinit var pDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initEvents()
        initProgressDialog()
        initFirebaseDatabase()
    }

    private fun initProgressDialog() {
        pDialog = ProgressDialog(this)
        pDialog.setMessage("Loading...")
        pDialog.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        binding.ivUpload.setOnClickListener(this)
        binding.btnCreatePost.setOnClickListener(this)
    }

    private fun initFirebaseDatabase() {
        database = Firebase.database.reference
    }

    private fun chooseImage() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            intent.type = "image/*"
            intent.putExtra("crop", "true")
            intent.putExtra("scale", "true")
            intent.putExtra("aspectX", 16)
            intent.putExtra("aspectY", 17)
            startActivityForResult(intent, SignUpActivity.PICK_IMAGE_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                SignUpActivity.PICK_IMAGE_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SignUpActivity.PICK_IMAGE_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }
            val uri = data?.data
            uploadedUri = uri!!
            val bitmap = uriToBitmap(uri)
            binding.uploadContainer.visibility = View.GONE
            Glide.with(this).load(bitmap).into(binding.ivPost)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SignUpActivity.PICK_IMAGE_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage()
                }
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

    private fun createFirebasePost(uid: String?, content: String?) {
        val mAuth = Firebase.auth

        if (mAuth.currentUser != null) {

            database.child("users").child(mAuth.currentUser!!.uid).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val author = snapshot.getValue(UserModel::class.java)!!
                    val post = Post()
                    post.author = author
                    post.id = uid
                    post.content = content
                    post.nLikes = 0

                    database.child("posts").child(uid.toString()).setValue(post)
                    pDialog.dismiss()
                    goToMainActivity()
                }

                override fun onCancelled(error: DatabaseError) {
                    pDialog.dismiss()
                }

            })

            database?.child("users")?.child(mAuth.currentUser!!.uid)?.get()?.addOnSuccessListener {
                val user = it.getValue(UserModel::class.java)
                user!!.nPosts = if (user.nPosts != null) user.nPosts!!.plus(1) else 1
                database.child("users").child(mAuth.currentUser!!.uid).setValue(user)
            }

        }


    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun uploadPostContent() {
        if (uploadedUri == null) {
            return
        }
        pDialog.show()
        val storage = Firebase.storage
        val storageRef = storage.reference
        val uuid = UUID.randomUUID().toString()
        val postRef = storageRef.child("posts/$uuid.jpg")
        postRef.putBytes(getUploadedImage()).addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                postRef.downloadUrl.addOnSuccessListener { uri ->
                    createFirebasePost(uuid, uri.toString())
                }
            } else {
                pDialog.dismiss()
                Toast.makeText(this, "Cannot upload your post", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getUploadedImage(): ByteArray {
        binding.ivPost.isDrawingCacheEnabled = true
        binding.ivPost.buildDrawingCache()
        val bitmap = (binding.ivPost.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivUpload -> chooseImage()
            R.id.btnCreatePost -> uploadPostContent()
        }
    }
}