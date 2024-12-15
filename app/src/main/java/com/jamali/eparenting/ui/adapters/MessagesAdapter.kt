package com.jamali.eparenting.ui.adapters

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jamali.eparenting.R
import com.jamali.eparenting.data.Message
import com.jamali.eparenting.databinding.DialogFullscreenImageBinding
import com.jamali.eparenting.databinding.ItemReceiveMsgBinding
import com.jamali.eparenting.databinding.ItemSendMsgBinding
import com.jamali.eparenting.utils.TimeUtils
import com.jamali.eparenting.utils.Utility

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_send_msg, parent, false)
            SentMsgHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_receive_msg, parent, false)
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
        if (message.imageUrl != null) {
            // Show image, hide message
            holder.binding.image.visibility = View.VISIBLE
            holder.binding.message.visibility = View.GONE
            holder.binding.mLinear.visibility = View.GONE

            // Load image with Glide
            Glide.with(context)
                .load(message.imageUrl)
                .placeholder(R.drawable.img_placeholder) // Add a placeholder image
                .error(R.drawable.img_placeholder) // Add an error image
                .into(holder.binding.image)

            // Set click listener for image
            holder.binding.image.setOnClickListener {
                showFullScreenImage(message.imageUrl!!)
            }
        } else {
            // Show message, hide image
            holder.binding.image.visibility = View.GONE
            holder.binding.message.visibility = View.VISIBLE
            holder.binding.mLinear.visibility = View.VISIBLE
            holder.binding.message.text = message.message
            holder.binding.timestamp.text = TimeUtils.formatTimestamp(message.timestamp)
        }
    }

    private fun setupReceiverMessageHolder(holder: ReceiverMsgHolder, message: Message) {
        if (message.imageUrl != null) {
            // Show image, hide message
            holder.binding.image.visibility = View.VISIBLE
            holder.binding.message.visibility = View.GONE
            holder.binding.mLinear.visibility = View.GONE

            // Load image with Glide
            Glide.with(context)
                .load(message.imageUrl)
                .placeholder(R.drawable.img_placeholder) // Add a placeholder image
                .error(R.drawable.img_placeholder) // Add an error image
                .into(holder.binding.image)

            // Set click listener for image
            holder.binding.image.setOnClickListener {
                showFullScreenImage(message.imageUrl!!)
            }
        } else {
            // Show message, hide image
            holder.binding.image.visibility = View.GONE
            holder.binding.message.visibility = View.VISIBLE
            holder.binding.mLinear.visibility = View.VISIBLE
            holder.binding.message.text = message.message
            holder.binding.timestamp.text = TimeUtils.formatTimestamp(message.timestamp)
        }
    }

    private fun showFullScreenImage(imageUrl: String) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val binding = DialogFullscreenImageBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        // Load full resolution image
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_placeholder)
            .into(binding.fullscreenImage)

        // Setup zoom functionality
        binding.fullscreenImage.setOnMatrixChangeListener { rect ->
            // Handle matrix changes if needed
        }

        binding.fullscreenImage.setOnClickListener {
            dialog.dismiss()
        }

        // Add close button
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // Add download button
        binding.downloadButton.setOnClickListener {
            downloadImage(imageUrl)
        }

        dialog.show()
    }

    private fun downloadImage(imageUrl: String) {
        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(imageUrl)
            val request = DownloadManager.Request(uri)
            request.apply {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setTitle("Image Download")
                setDescription("Downloading image from chat")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Chat_Image_${System.currentTimeMillis()}.jpg")
            }
            dm.enqueue(request)
            Toast.makeText(context, "Downloading image...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show()
        }
    }

    inner class SentMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemSendMsgBinding.bind(itemView)
    }

    inner class ReceiverMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemReceiveMsgBinding.bind(itemView)
    }

    init {
        if (messages != null) {
            this.messages = messages
        }
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }
}