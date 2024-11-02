package com.jamali.eparenting.ui.home.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.Utility
import com.jamali.eparenting.data.entity.User
import com.jamali.eparenting.databinding.ItemListMessageBinding
import com.jamali.eparenting.ui.home.fragments.consultation.livechat.LiveChatActivity

class ContactAdapter(private val userList: List<User>) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(val binding: ItemListMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemListMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val user = userList[position]
        with(holder.binding) {
            tvNameProfile.text = user.username

            // Memantau perubahan status dari Firebase Realtime Database
            val statusRef = Utility.database.getReference("users").child(user.uid).child("status")
            statusRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.getValue(String::class.java)
                    when(status) {
                        "online" -> userStatus.setImageResource(R.drawable.ic_online)
                        "busy" -> userStatus.setImageResource(R.drawable.ic_busy)
                        "offline" -> userStatus.setImageResource(R.drawable.ic_offline)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("ContactAdapter", "Failed to read user status", error.toException())
                }
            })
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, LiveChatActivity::class.java)
            intent.putExtra("name", user.username)
            intent.putExtra("email", user.email)
            intent.putExtra("uid", user.uid)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = userList.size
}