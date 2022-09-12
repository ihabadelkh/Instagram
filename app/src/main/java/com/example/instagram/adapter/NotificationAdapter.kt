package com.example.instagram.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.databinding.NotificationItemBinding
import com.example.instagram.model.Notification

class NotificationAdapter(
    private val context: Context,
    private val notifications: List<Notification>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)  {
        val binding = NotificationItemBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val notification = notifications[position]
            Glide.with(context).load(notification.notificationImage).circleCrop().into(binding.notificationIv)
            binding.notificationMessageTxt.text = notification.notificationMessage
        }
    }

    override fun getItemCount(): Int {
        return notifications.size
    }
}