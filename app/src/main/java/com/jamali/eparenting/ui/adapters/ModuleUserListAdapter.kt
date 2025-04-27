package com.jamali.eparenting.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jamali.eparenting.R
import com.jamali.eparenting.data.model.Module
import com.jamali.eparenting.data.model.PostType
import com.jamali.eparenting.databinding.ItemModulListBinding

class ModuleUserListAdapter(
    private val moduleType: PostType,
    private val onClick: (Module) -> Unit
) : ListAdapter<Module, ModuleUserListAdapter.ViewHolder>(ModuleUserDiffCallback()) {

    private var filteredList: List<Module> = emptyList()

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

    fun submitFilteredList(moduleList: List<Module>) {
        filteredList = moduleList.filter { it.type == moduleType }
        submitList(filteredList)
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

            // Hiding delete button jika perlu
            binding.btnDelete.visibility = View.GONE
        }
    }
}

class ModuleUserDiffCallback : DiffUtil.ItemCallback<Module>() {
    override fun areItemsTheSame(oldItem: Module, newItem: Module): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Module, newItem: Module): Boolean {
        return oldItem == newItem
    }
}