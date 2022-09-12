package com.example.instagram.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.databinding.ProfilePostItemBinding
import com.example.instagram.model.Post

class ProfilePostAdapter(
    private val context: Context,
    private val posts: List<Post>
) : RecyclerView.Adapter<ProfilePostAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ProfilePostItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.profile_post_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val post = posts[position]
            Glide.with(context).load(post.content).into(binding.postContentIv)
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}