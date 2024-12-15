package com.jamali.eparenting.ui.admin.management.reports.content

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.data.reports.Report
import com.jamali.eparenting.data.reports.ReportItem
import com.jamali.eparenting.databinding.FragmentReportsBinding
import com.jamali.eparenting.ui.adapters.ReportDataAdapter
import com.jamali.eparenting.ui.admin.management.reports.DetailReportedActivity
import com.jamali.eparenting.utils.Utility

class CommentReportFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var reportsAdapter: ReportDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchReportedComments()
    }

    private fun setupRecyclerView() {
        reportsAdapter = ReportDataAdapter(
            type = "comment",
            onDeleteContentClickListener = { reportItem  ->
                deleteReportedComment(reportItem.id, reportItem.postId)
            },
            onDeleteReportClickListener = { commentId ->
                deleteReports(commentId)
            },
            onUnblockUserClickListener = {},
            onBlockUserClickListener = {},
            onItemClickListener = { reportItem ->
                val intent = Intent(requireContext(), DetailReportedActivity::class.java)
                intent.putExtra("type", "comment")
                intent.putExtra("commentId", reportItem.id)
                intent.putExtra("postId", reportItem.postId)
                startActivity(intent)
            }
        )
        binding.recyclerViewReports.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reportsAdapter
        }
    }

    private fun fetchReportedComments() {
        binding.progressBar.visibility = View.VISIBLE
        val reportsRef = Utility.database.getReference("reports").child("comment_reports")

        reportsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reportMap = mutableMapOf<String, ReportItem>()

                for (reportSnapshot in snapshot.children) {
                    val report = reportSnapshot.getValue(Report::class.java)
                    val commentId = reportSnapshot.child("commentId").getValue(String::class.java)
                    val postId = reportSnapshot.child("postId").getValue(String::class.java)
                    val commentText = reportSnapshot.child("commentText").getValue(String::class.java)
                    val commentAuthorId = reportSnapshot.child("commentAuthorId").getValue(String::class.java)

                    if (report != null && commentId != null) {
                        val existingReport = reportMap[commentId]
                        val updatedReports = (existingReport?.reports ?: emptyList()) + report

                        reportMap[commentId] = ReportItem(
                            id = commentId,
                            postId = postId ?: "",
                            content = commentText ?: "",
                            authorId = commentAuthorId ?: "",
                            reportCount = updatedReports.size,
                            reports = updatedReports
                        )
                    }
                }

                // Urutkan berdasarkan jumlah laporan terbanyak
                val sortedReports = reportMap.values.sortedByDescending { it.reportCount }
                reportsAdapter.submitList(sortedReports)

                binding.progressBar.visibility = View.GONE
                binding.emptyStateTextView.visibility =
                    if (sortedReports.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Utility.showToast(requireContext(), "Gagal memuat laporan: ${error.message}")
            }

        })
    }

    private fun deleteReportedComment(commentId: String, postId: String) {
        val postsRef = Utility.database.getReference("communityposts").child(postId)
        val reportsRef = Utility.database.getReference("reports").child("comment_reports")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Hapus Komentar")
            .setMessage("Anda yakin ingin menghapus komentar ini beserta semua laporannya?")
            .setPositiveButton("Hapus") { _, _ ->
                binding.progressBar.visibility = View.VISIBLE

                // Dapatkan referensi postingan
                postsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(postSnapshot: DataSnapshot) {
                        // Kurangi commentCount
                        val currentCommentCount = postSnapshot.child("commentCount").getValue(Int::class.java) ?: 0
                        val updatedCommentCount = maxOf(0, currentCommentCount - 1)

                        // Update commentCount
                        postsRef.child("commentCount").setValue(updatedCommentCount)

                        // Hapus komentar dari postingan
                        postsRef.child("comments").child(commentId).removeValue()
                            .addOnSuccessListener {
                                // Hapus semua laporan terkait komentar
                                reportsRef.orderByChild("commentId").equalTo(commentId)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (reportSnapshot in snapshot.children) {
                                                reportSnapshot.ref.removeValue()
                                            }

                                            binding.progressBar.visibility = View.GONE
                                            Utility.showToast(requireContext(), "Komentar berhasil dihapus")

                                            // Refresh daftar laporan
                                            fetchReportedComments()
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            binding.progressBar.visibility = View.GONE
                                            Utility.showToast(requireContext(), "Gagal menghapus laporan: ${error.message}")
                                        }
                                    })
                            }
                            .addOnFailureListener { exception ->
                                binding.progressBar.visibility = View.GONE
                                Utility.showToast(requireContext(), "Gagal menghapus komentar: ${exception.localizedMessage}")
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.progressBar.visibility = View.GONE
                        Utility.showToast(requireContext(), "Gagal mengambil data postingan: ${error.message}")
                    }
                })
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteReports(commentId: String) {
        val reportsRef = Utility.database.getReference("reports").child("comment_reports")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Hapus Laporan")
            .setMessage("Anda yakin ingin menghapus semua laporan untuk komentar ini tanpa menghapus komentarnya?")
            .setPositiveButton("Hapus Laporan") { _, _ ->
                binding.progressBar.visibility = View.VISIBLE

                // Hapus semua laporan terkait postingan
                reportsRef.orderByChild("commentId").equalTo(commentId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (reportSnapshot in snapshot.children) {
                                reportSnapshot.ref.removeValue()
                            }

                            binding.progressBar.visibility = View.GONE
                            Utility.showToast(requireContext(), "Laporan berhasil dihapus")

                            // Refresh daftar laporan
                            fetchReportedComments()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            binding.progressBar.visibility = View.GONE
                            Utility.showToast(requireContext(), "Gagal menghapus laporan: ${error.message}")
                        }
                    })
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}