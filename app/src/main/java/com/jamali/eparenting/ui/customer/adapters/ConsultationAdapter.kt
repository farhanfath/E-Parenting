package com.jamali.eparenting.ui.customer.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jamali.eparenting.R
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ItemConsultationExpertBinding
import com.jamali.eparenting.ui.customer.fragments.consultation.ChatActivity

class ConsultationAdapter(private val doctorList: List<User>) :
    RecyclerView.Adapter<ConsultationAdapter.ForumCommunityViewHolder>(){

    class ForumCommunityViewHolder(val binding: ItemConsultationExpertBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumCommunityViewHolder {
        val binding = ItemConsultationExpertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForumCommunityViewHolder(binding)
    }

    override fun getItemCount(): Int = doctorList.size

    override fun onBindViewHolder(holder: ForumCommunityViewHolder, position: Int) {
        val doctor = doctorList[position]
        with(holder.binding) {
            tvUsername.text = doctor.username
            tvSpeciality.text = doctor.speciality
            tvSchedule.text = doctor.activeDay

            Glide.with(holder.itemView.context)
                .load(doctor.profile)
                .error(R.drawable.ic_avatar)
                .placeholder(R.drawable.ic_avatar)
                .into(ivImage)
        }
        holder.binding.root.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("name", doctor.username)
            intent.putExtra("email", doctor.email)
            intent.putExtra("uid", doctor.uid)
            holder.itemView.context.startActivity(intent)
        }
    }
}