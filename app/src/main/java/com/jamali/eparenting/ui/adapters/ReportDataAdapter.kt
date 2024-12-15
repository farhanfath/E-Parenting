package com.jamali.eparenting.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jamali.eparenting.R
import com.jamali.eparenting.data.reports.ReportItem
import com.jamali.eparenting.databinding.ItemReportContentBinding

class ReportDataAdapter(
    private val type: String,
    private val onDeleteContentClickListener: (ReportItem) -> Unit,
    private val onDeleteReportClickListener: (String) -> Unit,
    private val onUnblockUserClickListener: (String) -> Unit,
    private val onBlockUserClickListener: (String) -> Unit,
    private val onItemClickListener : (ReportItem) -> Unit
) : ListAdapter<ReportItem, ReportDataAdapter.ReportViewHolder>(ReportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportContentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding, type, onDeleteContentClickListener, onDeleteReportClickListener, onUnblockUserClickListener, onBlockUserClickListener, onItemClickListener)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReportViewHolder(
        private val binding: ItemReportContentBinding,
        private val type: String,
        private val onDeleteContentClickListener: (ReportItem) -> Unit,
        private val onDeleteReportClickListener: (String) -> Unit,
        private val onUnblockUserClickListener: (String) -> Unit,
        private val onBlockUserClickListener: (String) -> Unit,
        private val onItemClickListener : (ReportItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reportItem: ReportItem) {
            val context = binding.root.context
            binding.textViewReportedContent.text = context.getString(R.string.reported_content_text, reportItem.content)
            binding.textViewReportCount.text = context.getString(R.string.jumlah_laporan_text, reportItem.reportCount.toString())

            // Tampilkan detail laporan
            val reportDetailsText = reportItem.reports.joinToString("\n") { report ->
                "ID Pelapor : ${report.reporterId}\nAlasan : ${report.reason}\n"
            }
            binding.textViewReportDetails.text = reportDetailsText

            when(type) {
                "post" -> {
                    binding.buttonDeleteContent.text = context.getString(R.string.delete_post_text)
                    binding.blockUserLayout.visibility = View.GONE
                }
                "comment" -> {
                    binding.buttonDeleteContent.text = context.getString(R.string.delete_comment_text)
                    binding.blockUserLayout.visibility = View.GONE
                }
                "user" -> {
                    with(binding) {
                        buttonDeleteContent.text = context.getString(R.string.delete_user_text)
                        buttonDeleteContent.visibility = View.GONE
                        blockUserLayout.visibility = View.VISIBLE
                    }

                }
            }

            binding.buttonDeleteContent.setOnClickListener {
                onDeleteContentClickListener(reportItem)
            }

            binding.buttonDeleteReports.setOnClickListener {
                onDeleteReportClickListener(reportItem.id)
            }

            binding.buttonUnblockUser.setOnClickListener {
                onUnblockUserClickListener(reportItem.id)
            }

            binding.buttonBlockUser.setOnClickListener {
                onBlockUserClickListener(reportItem.id)
            }

            binding.navigateReportedContent.setOnClickListener {
                onItemClickListener(reportItem)
            }
        }
    }

    class ReportDiffCallback : DiffUtil.ItemCallback<ReportItem>() {
        override fun areItemsTheSame(oldItem: ReportItem, newItem: ReportItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ReportItem, newItem: ReportItem): Boolean {
            return oldItem == newItem
        }
    }
}
