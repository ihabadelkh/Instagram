package com.example.instagram.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.instagram.R
import com.example.instagram.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogInActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var pDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initEvents()
        initProgressDialog()
        initFirebaseAuth()
    }

    private fun initProgressDialog() {
        pDialog = ProgressDialog(this)
        pDialog.setMessage("Loading...")
        pDialog.setCanceledOnTouchOutside(false)
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null) {
            goToMainActivity()
        }
    }

    private fun initEvents() {
        binding.btnLogin.setOnClickListener(this)
        binding.txtRegister.setOnClickListener(this)
    }

    private fun initFirebaseAuth() {
        mAuth = Firebase.auth
    }

    private fun callFirebaseAuthService(email: String, password: String) {
        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                pDialog.dismiss()
                goToMainActivity()
            } else {
                pDialog.dismiss()
                Toast.makeText(
                    this@LogInActivity,
                    "Authentication Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validateUserCredentials(email: String?, password: String?): Boolean {
        if (email != null && email == EMPTY_STRING) {
            Toast.makeText(this@LogInActivity, "Please input your email", Toast.LENGTH_LONG).show()
            return false
        }
        if (password != null && password == EMPTY_STRING) {
            Toast.makeText(this@LogInActivity, "Please input your password", Toast.LENGTH_LONG)
                .show()
            return false
        }
        return true
    }

    private fun login() {
        val email = binding.edtEmail.text.toString().trim { it <= ' ' }
        val password = binding.edtPassword.text.toString().trim { it <= ' ' }
        if (validateUserCredentials(email, password)) {
            // call firebase authentication service.
            pDialog.show()
            callFirebaseAuthService(email, password)
        }
    }

    private fun goToMainActivity() {
        intent = Intent(this@LogInActivity, MainActivity::class.java);
        startActivity(intent);
        finish()
    }

    private fun goToSignUpScreen() {
        val intent = Intent(this@LogInActivity, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnLogin -> login()
            R.id.txtRegister -> goToSignUpScreen()
        }
    }

    companion object {
        private const val EMPTY_STRING = ""
    }

}