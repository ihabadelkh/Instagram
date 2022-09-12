package com.example.instagram.fragment

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.adapter.ProfilePostAdapter
import com.example.instagram.databinding.FragmentProfileBinding
import com.example.instagram.model.Post
import com.example.instagram.model.UserModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var pDialog: ProgressDialog
    private lateinit var mDatabase: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initEvent()
        initFirebaseDatabase()
        getProfile()
        getPosts()
    }

    private fun initViews() {
        binding.postBottomLine.visibility = View.GONE

        pDialog = ProgressDialog(this.context)
        pDialog.setMessage("Loading...")
        pDialog.setCanceledOnTouchOutside(false)
    }

    private fun initEvent() {
        binding.postIv.setOnClickListener {
            binding.postBottomLine.visibility = View.VISIBLE

        }
    }

    private fun initFirebaseDatabase() {
        mDatabase = Firebase.database.reference
    }

    private fun getProfile() {
        val currentUserId = Firebase.auth.currentUser?.uid
        if (currentUserId != null) {
            pDialog.show()
            mDatabase.child("users").child(currentUserId).get().addOnSuccessListener {
                pDialog.dismiss()
                val user = it.getValue(UserModel::class.java)
                Glide.with(this).load(user?.avatar).circleCrop().into(binding.authorAvatarIv)
                binding.nPostsTxt.text = if (user?.nPosts != null) user.nPosts.toString() else "0"
                binding.nFollowersTxt.text =
                    if (user?.nFollowers != null) user.nFollowers.toString() else "0"
            }.addOnFailureListener {
                pDialog.dismiss()
            }
        }
    }

    private fun initRecyclerView(posts: ArrayList<Post>) {
        if (posts == null) {
            return
        }
        binding.profilePostRv.layoutManager = GridLayoutManager(this.context, 3)
        val adapter = this.context?.let { ProfilePostAdapter(it, posts) }
        binding.profilePostRv.adapter = adapter
        pDialog.dismiss()
    }

    private fun getPosts() {
        val mAuth = Firebase.auth
        if (mAuth.currentUser != null) {
            pDialog.show()
            mDatabase.child("posts").orderByChild("id")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val posts = ArrayList<Post>()
                        if (snapshot.children.count() > 0) {
                            for (postSnapshot in snapshot.children) {
                                val post = postSnapshot.getValue(Post::class.java)
                                if (post != null && post.author?.uid == mAuth.currentUser!!.uid) {
                                    posts.add(post)
                                }
                            }
                        } else {
                            pDialog.dismiss()
                        }
                        initRecyclerView(posts)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        pDialog.dismiss()
                        Toast.makeText(
                            context,
                            "Cannot fetch list of posts",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })
        }
    }


}