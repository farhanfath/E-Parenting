package com.jamali.eparenting.ui.home.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jamali.eparenting.R
import com.jamali.eparenting.Utility
import com.jamali.eparenting.data.entity.Message
import com.jamali.eparenting.databinding.DeleteLayoutBinding
import com.jamali.eparenting.databinding.ReceiveMsgBinding
import com.jamali.eparenting.databinding.SendMsgBinding

class MessagesAdapter(
    var context: Context,
    messages:ArrayList<Message>?,
    senderRoom:String,
    receiverRoom: String
): RecyclerView.Adapter<RecyclerView.ViewHolder?>()
{

    lateinit var messages: ArrayList<Message>
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    val senderRoom: String
    var receiverRoom: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            SentMsgHolder(view)
        }
        else {
            val view = LayoutInflater.from(context).inflate(R.layout.receive_msg, parent, false)
            ReceiverMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (Utility.auth.currentUser?.uid.equals(message.senderId)) {
            ITEM_SENT
        }
        else {
            ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder.javaClass == SentMsgHolder::class.java) {
            val viewHolder = holder as SentMsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.text = message.message
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout,null,false)
                val binding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()

                binding.forEveryone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { it1 ->
                        Utility.firestore.collection("chats")
                            .document(senderRoom)
                            .collection("message")
                            .document(it1)
                            .set(message)
                    }

                    message.messageId.let { it1 ->
                        Utility.firestore.collection("chats")
                            .document(receiverRoom)
                            .collection("message")
                            .document(it1!!)
                            .set(message)
                    }
                    dialog.dismiss()
                }
                binding.forMe.setOnClickListener {
                    message.messageId.let { it1 ->
                        Utility.firestore.collection("chats")
                            .document(senderRoom)
                            .collection("message")
                            .document(it1!!)
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                false
            }
        }
        else {
            val viewHolder = holder as ReceiverMsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .into(viewHolder.binding.image)
            }

            viewHolder.binding.message.text = message.message
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout,null,false)
                val binding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()

                binding.forEveryone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { it1 ->
                        Utility.firestore.collection("chats")
                            .document(senderRoom)
                            .collection("message")
                            .document(it1)
                            .set(message)
                    }

                    message.messageId.let { it1 ->
                        Utility.firestore.collection("chats")
                            .document(receiverRoom)
                            .collection("message")
                            .document(it1!!)
                            .set(message)
                    }
                    dialog.dismiss()
                }
                binding.forMe.setOnClickListener {
                    message.messageId.let { it1 ->
                        Utility.firestore.collection("chats")
                            .document(senderRoom)
                            .collection("message")
                            .document(it1!!)
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                false
            }
        }
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