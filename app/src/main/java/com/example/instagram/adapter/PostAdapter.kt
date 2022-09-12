package com.example.instagram.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.databinding.PostItemBinding
import com.example.instagram.fragment.FeedFragment
import com.example.instagram.model.Notification
import com.example.instagram.model.Post
import com.example.instagram.model.UserModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class PostAdapter(
    private val feedFragment: FeedFragment,
    private val context: Context,
    private val posts: List<Post>,
    private var database: DatabaseReference
) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = PostItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val currentUserId = Firebase.auth.currentUser?.uid
            val post = posts[position]
            Glide.with(context).load(post.author?.avatar).circleCrop().into(binding.authorAvatarIv)
            binding.authorNameTxt.text = post.author?.name
            Glide.with(context).load(post.content).into(binding.postContentIv)
            if (currentUserId == post.author?.uid) {
                binding.followTxt.visibility = View.GONE
                binding.dot.visibility = View.GONE
            } else {
                if (post.hasFollowed == true) {
                    binding.followTxt.text = "Followed"
                    binding.followTxt.setTextColor(Color.DKGRAY)
                } else {
                    binding.followTxt.text = "Follow"
                }
            }
            val hearIcon = if (post.hasLiked == true) R.drawable.heart_active else R.drawable.heart
            Glide.with(context).load(hearIcon).into(binding.heartIv)
            if (post.hasLiked == true) {
                binding.likeCountTxt.text = "${post.likes?.size} likes"
            } else {
                binding.likeCountTxt.text = "0 likes"
            }
            binding.followTxt.setOnClickListener {
                toggleFollow(post)
            }
            binding.heartIv.setOnClickListener {
                toggleLikes(post)
            }
        }

    }

    override fun getItemCount(): Int {
        return posts.size
    }

    private fun toggleFollow(post: Post) {
        val currentUserId = Firebase.auth.currentUser?.uid
        if (currentUserId != null) {
            database.child("users").child(currentUserId).get().addOnSuccessListener { currentUserSnapshot ->
                val currentUserInfo = currentUserSnapshot.getValue(UserModel::class.java)
                database.child("users").child(post.author?.uid!!).get().addOnSuccessListener { postUserSnapshot ->
                    val postUserInfo = postUserSnapshot.getValue(UserModel::class.java)
                    val followers = ArrayList<String>()
                    if (post.hasFollowed == null) {
                        followers.add(currentUserId)
                    } else if (post.hasFollowed == true) {
                        for (follower in postUserInfo?.followers!!) {
                            if (follower != currentUserId) {
                                followers.add(follower)
                            }
                        }
                    } else if (post.hasFollowed == false) {
                        for (follower in postUserInfo?.followers!!) {
                            if (follower != currentUserId) {
                                followers.add(follower)
                                followers.add(currentUserId)
                            }
                        }
                    }
                    postUserInfo?.followers = followers
                    postUserInfo?.nFollowers = followers.size
                    database = Firebase.database.reference
                    database.child("users").child(post.author?.uid!!).setValue(postUserInfo)
                    val notification = Notification()
                    notification.id = UUID.randomUUID().toString()
                    notification.notificationImage = currentUserInfo?.avatar
                    notification.notificationMessage = currentUserInfo?.name + " has Followed you"
                    notification.receiverId = post.author!!.uid
                    createNotification(notification)
                }
            }
        }
    }

    private fun toggleLikes(post: Post) {
        database = Firebase.database.reference
        val currentUserId = Firebase.auth.currentUser?.uid
        database.child("users").child(currentUserId!!).get().addOnSuccessListener {
            val currentUserInfo = it.getValue(UserModel::class.java)
            database.child("posts").child(post.id!!).get().addOnSuccessListener {
                val post = it.getValue(Post::class.java)

                val likes = ArrayList<String>()
                if (post?.hasLiked == null) {
                    likes.add(currentUserId!!)
                } else if (post.hasLiked == true) {
                    for (like in post?.likes!!) {
                        if (currentUserId != like) {
                            likes.add(currentUserId!!)
                        }
                    }
                } else if (post.hasLiked == false) {
                    for (like in post?.likes!!) {
                        likes.add(like)
                    }
                }
                val notification = Notification()
                notification.id = UUID.randomUUID().toString()
                notification.notificationImage = currentUserInfo?.avatar
                notification.notificationMessage = currentUserInfo?.name + " has liked you post"
                notification.receiverId = post?.author!!.uid
                createNotification(notification)
                post?.likes = likes
                post?.nLikes = likes.size
                database.child("posts").child(post?.id!!).setValue(post)
                feedFragment.getPosts()
            }
        }
    }

    private fun createNotification(notification: Notification) {
        if (notification.id == null) {
            return
        }
        database = Firebase.database.reference
        database.child("notifications").child(notification.id!!).setValue(notification)
    }

}