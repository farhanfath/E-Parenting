package com.jamali.eparenting.ui.home.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jamali.eparenting.R
import com.jamali.eparenting.application.Utility
import com.jamali.eparenting.data.entity.Message
import com.jamali.eparenting.databinding.DeleteLayoutBinding
import com.jamali.eparenting.databinding.ReceiveMsgBinding
import com.jamali.eparenting.databinding.SendMsgBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MessagesAdapter(
    var context: Context,
    messages: ArrayList<Message>?,
    senderRoom: String,
    receiverRoom: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    lateinit var messages: ArrayList<Message>
    private val ITEM_SENT = 1
    private val ITEM_RECEIVE = 2
    private val senderRoom: String
    private val receiverRoom: String
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            SentMsgHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.receive_msg, parent, false)
            ReceiverMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (Utility.auth.currentUser?.uid == message.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMsgHolder) {
            setupSentMessageHolder(holder, message)
        } else if (holder is ReceiverMsgHolder) {
            setupReceiverMessageHolder(holder, message)
        }
    }

    private fun setupSentMessageHolder(holder: SentMsgHolder, message: Message) {
        if (message.message == "photo") {
            holder.binding.image.visibility = View.VISIBLE
            holder.binding.message.visibility = View.GONE
            holder.binding.mLinear.visibility = View.GONE
            Glide.with(context).load(message.imageUrl).into(holder.binding.image)
        } else {
            holder.binding.message.text = message.message
        }

        holder.itemView.setOnLongClickListener {
            showDeleteDialog(message, senderRoom, receiverRoom)
            false
        }
    }

    private fun setupReceiverMessageHolder(holder: ReceiverMsgHolder, message: Message) {
        if (message.message == "photo") {
            holder.binding.image.visibility = View.VISIBLE
            holder.binding.message.visibility = View.GONE
            holder.binding.mLinear.visibility = View.GONE
            Glide.with(context).load(message.imageUrl).into(holder.binding.image)
        } else {
            holder.binding.message.text = message.message
        }

        holder.itemView.setOnLongClickListener {
            showDeleteDialog(message, senderRoom, receiverRoom)
            false
        }
    }

    private fun showDeleteDialog(message: Message, senderRoom: String, receiverRoom: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null, false)
        val binding = DeleteLayoutBinding.bind(view)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Delete Message")
            .setView(binding.root)
            .create()

        binding.forEveryone.setOnClickListener {
            message.message = "This message is removed"
            message.messageId?.let {
                database.child("chats").child(senderRoom).child("messages").child(it).setValue(message)
                database.child("chats").child(receiverRoom).child("messages").child(it).setValue(message)
            }
            dialog.dismiss()
        }

        binding.forMe.setOnClickListener {
            message.messageId?.let {
                database.child("chats").child(senderRoom).child("messages").child(it).removeValue()
            }
            dialog.dismiss()
        }

        binding.cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    inner class SentMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiverMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ReceiveMsgBinding.bind(itemView)
    }

    init {
        if (messages != null) {
            this.messages = messages
        }
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }
}
