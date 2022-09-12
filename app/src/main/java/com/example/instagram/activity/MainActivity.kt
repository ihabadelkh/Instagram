package com.example.instagram.activity

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.instagram.R
import com.example.instagram.databinding.ActivityMainBinding
import com.example.instagram.fragment.FeedFragment
import com.example.instagram.fragment.NotificationFragment
import com.example.instagram.fragment.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initEvents()
        initFragment(savedInstanceState)
        initFirebaseAuth()
    }

    private fun initEvents() {
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(this)
        binding.ivPlus.setOnClickListener(this)
        binding.ivLogout.setOnClickListener(this)
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val fragment = FeedFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                .commit()
        }
    }

    private fun initFirebaseAuth() {
        mAuth = Firebase.auth
    }

    private fun goToCreatePost() {
        val intent = Intent(this, CreateActivity::class.java)
        startActivity(intent)
    }

    private fun goToLoginActivity() {
        val intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)
    }

    private fun logout() {
        AlertDialog.Builder(this)
            .setMessage("Do you want to Logout?")
            .setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
                mAuth.signOut()
                goToLoginActivity()
                finish()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })
            .setTitle("Logout")
            .create()
            .show()

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {
                val fragment = FeedFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
                return true
            }
            R.id.navigation_notification -> {
                val fragment = NotificationFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
                return true
            }
            R.id.navigation_profile -> {
                val fragment = ProfileFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
                return true
            }
        }
        return false
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivLogout -> logout()
            R.id.ivPlus -> goToCreatePost()
        }
    }
}