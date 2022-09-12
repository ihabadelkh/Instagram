package com.example.instagram.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.adapter.NotificationAdapter
import com.example.instagram.databinding.FragmentNotificationBinding
import com.example.instagram.model.Notification
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var mDatabase: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFirebaseDatabase()
        getNotification()
    }

    private fun initFirebaseDatabase() {
        mDatabase = Firebase.database.reference
    }

    private fun initRecyclerView(notification: ArrayList<Notification>) {
        if (notification == null) {
            return
        }
        binding.rvNotification.layoutManager = LinearLayoutManager(this.context)
        val adapter = this.context.let { NotificationAdapter(it!!, notification) }
        binding.rvNotification.adapter = adapter
    }

    private fun getNotification() {
        val mAuth = Firebase.auth.currentUser
        if (mAuth != null) {
            mDatabase.child("notifications").orderByChild("id").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifications = ArrayList<Notification>()
                    if (snapshot.children.count() > 0) {
                        for (NotificationSnapshot in snapshot.children) {
                            val notification = NotificationSnapshot.getValue(Notification::class.java)
                            if (notification != null && notification.receiverId.equals(mAuth.uid.toString())) {
                                notifications.add(notification)
                            }
                        }
                    }
                    initRecyclerView(notifications)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

}