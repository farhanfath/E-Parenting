package com.jamali.eparenting.ui.doctor

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jamali.eparenting.data.UserWithLastMessage
import com.jamali.eparenting.databinding.ItemConsultationCustomerBinding
import com.jamali.eparenting.ui.customer.fragments.consultation.ChatActivity
import com.jamali.eparenting.utils.TimeUtils

class CustomerAdapter(private val users: List<UserWithLastMessage>) :
    RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemConsultationCustomerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConsultationCustomerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userWithMessage = users[position]
        val user = userWithMessage.user

        with(holder.binding) {
            // Set user info
            userName.text = user.username
            userEmail.text = user.email

            // Set last message
            lastMessage.text = userWithMessage.lastMessage

            // Set time
            if (userWithMessage.lastMessageTime > 0) {
                lastMessageTime.text = TimeUtils.formatTimestamp(userWithMessage.lastMessageTime)
                lastMessageTime.visibility = View.VISIBLE
            } else {
                lastMessageTime.visibility = View.GONE
            }

            // Set click listener
            root.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("name", user.username)
                    putExtra("email", user.email)
                    putExtra("uid", user.uid)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = users.size
}