package com.example.instagram.activity

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
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.databinding.ActivitySignUpBinding
import com.example.instagram.model.UserModel
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.jar.Manifest

class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignUpBinding
    private var uploadedURI: String? = null
    private lateinit var database: DatabaseReference
    private lateinit var pDialog: ProgressDialog
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initEvents()
        initProgressDialog()

    }

    private fun initProgressDialog() {
        pDialog = ProgressDialog(this)
        pDialog.setMessage("Loading...")
        pDialog.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        binding.btnRegister.setOnClickListener(this)
        binding.ivUserAvatar.setOnClickListener(this)
        binding.txtLogin.setOnClickListener(this)
    }

    private fun chooseImage() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            intent.putExtra("crop", "true")
            intent.putExtra("scale", "true")
            intent.putExtra("aspectX", "16")
            intent.putExtra("aspectY", "9")
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }
            val uri = data?.data
            uploadedURI = uri.toString()
            if (uri != null) {
                val imageBitmap = uriToBitmap(uri)
                Glide.with(this)
                    .load(imageBitmap)
                    .circleCrop()
                    .into(binding.ivUserAvatar)
                binding.txtUserAvatar.visibility = View.GONE
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage()
                }
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

    private fun validate(
        fullName: String?,
        email: String?,
        password: String?,
        confirmPassword: String?
    ): Boolean {
        if (uploadedURI == null || uploadedURI.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please upload your avatar", Toast.LENGTH_LONG)
                .show();
            return false;
        }
        if (fullName == null || fullName.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please input your full name", Toast.LENGTH_LONG)
                .show();
            return false;
        }
        if (email == null || email.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please input your email", Toast.LENGTH_LONG)
                .show();
            return false;
        }
        if (password == null || password.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please input your password", Toast.LENGTH_LONG)
                .show();
            return false;
        }
        if (confirmPassword == null || confirmPassword.equals(EMPTY_STRING)) {
            Toast.makeText(
                this@SignUpActivity,
                "Please input your confirm password",
                Toast.LENGTH_LONG
            ).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(
                this@SignUpActivity,
                "Your password and confirm password must be matched",
                Toast.LENGTH_LONG
            ).show();
            return false;
        }
        return true;
    }

    private fun goToLoginActivity() {
        val intent = Intent(this@SignUpActivity, LogInActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun insertFirebaseDatabase(
        userId: String?,
        fullName: String?,
        email: String?,
        avatar: String?
    ) {

        val userModel = UserModel()
        userModel.uid = userId
        userModel.name = fullName
        userModel.email = email
        userModel.avatar = avatar
        database = Firebase.database.reference
        database.child("users").child(userId!!).setValue(userModel)

    }

    private fun createFirebaseAccount(
        fullName: String?,
        email: String?,
        password: String?,
        avatar: String?
    ) {
        if (email != null && password != null) {
            auth = Firebase.auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        pDialog.dismiss()
                        val userId = auth.currentUser?.uid
                        insertFirebaseDatabase(userId.toString(), fullName, email, avatar)
                        goToLoginActivity()
                    } else {
                        pDialog.dismiss()
                        Toast.makeText(
                            this@SignUpActivity,
                            "Cannot create your account, please try again",
                            Toast.LENGTH_LONG
                        ).show();
                    }
                }
        } else {
            Toast.makeText(
                this@SignUpActivity,
                "Please provide your email and password",
                Toast.LENGTH_LONG
            ).show();
        }
    }

    private fun uploadUserAvatar(fullname: String?, email: String?, password: String?) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val uuid = UUID.randomUUID().toString()
        val avatarRef = storageRef.child("users/$uuid.jpg")
        //____________________________________________________________________________________-
        binding.ivUserAvatar.isDrawingCacheEnabled = true
        binding.ivUserAvatar.buildDrawingCache()
        val bitmap = (binding.ivUserAvatar?.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = avatarRef.putBytes(data)
        //______________________________________________________________________________________-
        uploadTask.addOnFailureListener {
            pDialog.dismiss()
            Toast.makeText(this, "Cannot upload your avatar", Toast.LENGTH_LONG).show();
        }.addOnSuccessListener {
            avatarRef.downloadUrl.addOnSuccessListener(OnSuccessListener { uri ->
                if (uri != null) {
                    this.createFirebaseAccount(fullname, email, password, uri.toString())
                }
            })
        }
    }

    private fun register() {
        val fullName = binding.edtFullName.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()
        val confirmPassword = binding.edtConfirmPass.text.toString().trim()
        if (validate(fullName, email, password, confirmPassword)) {
            pDialog.show()
            uploadUserAvatar(fullName, email, password)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivUserAvatar -> chooseImage()
            R.id.btnRegister -> register()
            R.id.txtLogin -> goToLoginActivity()
            else -> {}
        }
    }

    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 1000
        const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1001
        const val EMPTY_STRING = ""
    }
}