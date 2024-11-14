package com.jamali.eparenting.ui.home.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jamali.eparenting.data.entity.ModulDataType
import com.jamali.eparenting.databinding.ItemModulTypeBinding
import com.jamali.eparenting.ui.home.modul.ModulActivity

class ModulAdapter(private val modulDataList: List<ModulDataType>) :
    RecyclerView.Adapter<ModulAdapter.ModulViewHolder>(){

    class ModulViewHolder(val binding: ItemModulTypeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModulViewHolder {
        val binding = ItemModulTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ModulViewHolder(binding)
    }

    override fun getItemCount(): Int = modulDataList.size

    override fun onBindViewHolder(holder: ModulViewHolder, position: Int) {
        val communityType = modulDataList[position]
        with(holder.binding) {
            moduleTitle.text = communityType.title
            moduleDescription.text = communityType.description
            moduleIcon.setImageResource(communityType.imageResId)
            cardViewModul.setCardBackgroundColor(holder.itemView.context.getColor(communityType.backgroundColor))
            cardViewModul.strokeColor = holder.itemView.context.getColor(communityType.strokeColor)
        }
        holder.binding.root.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ModulActivity::class.java)
            intent.putExtra("modul_data", communityType)
            intent.putExtra("type", communityType.type)
            context.startActivity(intent)
        }
    }
}