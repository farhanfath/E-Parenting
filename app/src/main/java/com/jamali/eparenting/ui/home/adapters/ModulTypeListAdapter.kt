package com.jamali.eparenting.ui.home.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jamali.eparenting.data.entity.fromapi.ModulItem
import com.jamali.eparenting.databinding.ItemListModulBinding
import com.jamali.eparenting.ui.home.modul.DetailModulActivity

class ModulTypeListAdapter(private val modulDataList: List<ModulItem>) :
    RecyclerView.Adapter<ModulTypeListAdapter.ModulTypeListViewHolder>(){

    class ModulTypeListViewHolder(val binding: ItemListModulBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModulTypeListViewHolder {
        val binding = ItemListModulBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ModulTypeListViewHolder(binding)
    }

    override fun getItemCount(): Int = modulDataList.size

    override fun onBindViewHolder(holder: ModulTypeListViewHolder, position: Int) {
        val modul = modulDataList[position]
        with(holder.binding) {
            tvTitleListMaterial.text = modul.judul
            tvDescListMaterial.text = modul.penjelasan
        }
        holder.binding.root.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailModulActivity::class.java)
            intent.putExtra("modul_data_detail", modul)
            context.startActivity(intent)
        }
    }
}