package com.jamali.eparenting.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jamali.eparenting.R
import com.jamali.eparenting.data.model.Module
import com.jamali.eparenting.databinding.ItemModulListBinding

class ModuleManagementListAdapter(
    private val onClick: (Module) -> Unit,
    private val onDeleteClick: (Module, Int) -> Unit,
    private val hideDeleteButton: Boolean
) : ListAdapter<Module, ModuleManagementListAdapter.ViewHolder>(ModuleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemModulListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = getItem(position)
        holder.bind(folder)
    }

    inner class ViewHolder(private val binding: ItemModulListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(modul: Module) {
            val context = binding.root.context
            binding.tvJudul.text = modul.title
            binding.tvEdisi.text = context.getString(R.string.tanggal_modul, modul.uploadedDate)
            binding.tvModuleType.text = modul.type.toString()

            binding.root.setOnClickListener {
                onClick(modul)
            }

            binding.btnDelete.visibility = if (hideDeleteButton) View.GONE else View.VISIBLE
            binding.btnDelete.setOnClickListener {
                onDeleteClick(modul, adapterPosition)
            }
        }
    }
}

class ModuleDiffCallback : DiffUtil.ItemCallback<Module>() {
    override fun areItemsTheSame(oldItem: Module, newItem: Module): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Module, newItem: Module): Boolean {
        return oldItem == newItem
    }
}