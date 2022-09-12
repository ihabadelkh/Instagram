package com.example.instagram.fragment

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.adapter.PostAdapter
import com.example.instagram.databinding.FragmentFeedBinding
import com.example.instagram.model.Post
import com.example.instagram.model.UserModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FeedFragment : Fragment() {

    private lateinit var binding: FragmentFeedBinding
    private lateinit var mDatabase: DatabaseReference
    private lateinit var pDialog: ProgressDialog
    private var adapter: PostAdapter? = null
    private var posts: ArrayList<Post>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFirebaseDatabase()
        initProgressDialog()
        getPosts()

    }

    private fun initProgressDialog() {
        pDialog = ProgressDialog(this.context)
        pDialog.setMessage("Loading...")
        pDialog.setCanceledOnTouchOutside(false)

    }

    private fun initFirebaseDatabase() {
        mDatabase = Firebase.database.reference
    }

    private fun hasFollowed(index: Int, post: Post, id: String?) {
        if (post.author == null || post.author?.uid == null) {
            return;
        }
        val userId = post.author?.uid
        mDatabase.child("users").child(userId!!).get().addOnSuccessListener {
            val user = it.getValue(UserModel::class.java)
            if (user?.followers == null || user.followers!!.size == 0) {
                post.hasFollowed = false
            } else {
                for (follower in user.followers!!) {
                    if (follower == id) {
                        post.hasFollowed = true
                    }
                }
            }
            posts?.set(index, post)
            if (adapter != null) {
                adapter?.notifyDataSetChanged()
            }
        }

    }

    private fun updateFollow() {
        val currentUserId = Firebase.auth.currentUser?.uid
        for ((index, post) in posts!!.withIndex()) {
            hasFollowed(index, post, currentUserId)
        }
    }

    private fun hasLiked(post: Post, id: String) {
        if (post.likes == null || post.likes!!.size == 0) {
            post.hasLiked = false
            return
        }
        for (like in post.likes!!) {
            if (like == id) {
                post.hasLiked = true
                return
            }
        }
        post.hasLiked = false
    }

    fun getPosts() {
        val mAuth = Firebase.auth
        if (mAuth.currentUser != null) {
            pDialog.show()
            mDatabase.child("posts").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    posts = ArrayList()
                    if (snapshot.children.count() > 0) {
                        for (dataSnapshot in snapshot.children) {
                            val post = dataSnapshot.getValue(Post::class.java)
                            if (post != null) {
                                hasLiked(post, mAuth.currentUser!!.uid)
                                posts?.add(post)
                            }
                        }
                        initRecyclerView(posts!!)
                        updateFollow()
                    } else {
                        pDialog.dismiss()
                        Toast.makeText(activity, "can't get Posts", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    pDialog.dismiss()
                    Toast.makeText(activity, "can't get Posts", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

    private fun initRecyclerView(posts: ArrayList<Post>) {
        if (posts == null || posts.size == 0) {
            return
        }
        binding.rvPost.layoutManager = LinearLayoutManager(this.context)
        adapter = this.context?.let { PostAdapter(this, it, posts, mDatabase) }
        binding.rvPost.adapter = adapter
        pDialog.dismiss()
    }


}