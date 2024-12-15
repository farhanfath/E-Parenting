package com.jamali.eparenting.ui.admin.management.reports.content

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.data.CommunityPost
import com.jamali.eparenting.data.reports.Report
import com.jamali.eparenting.data.reports.ReportItem
import com.jamali.eparenting.databinding.FragmentReportsBinding
import com.jamali.eparenting.ui.adapters.ReportDataAdapter
import com.jamali.eparenting.ui.admin.management.reports.DetailReportedActivity
import com.jamali.eparenting.utils.Utility

class PostReportFragment : Fragment() {

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
        fetchReportedPosts()
    }

    private fun setupRecyclerView() {
        reportsAdapter = ReportDataAdapter(
            type = "post",
            onDeleteContentClickListener = { reportItem ->
                deleteReportedPost(reportItem.id)
            },
            onDeleteReportClickListener = { postId ->
                deleteReports(postId)
            },
            onUnblockUserClickListener = {},
            onBlockUserClickListener = {},
            onItemClickListener = { reportItem ->
                val intent = Intent(requireContext(), DetailReportedActivity::class.java)
                intent.putExtra("type", "post")
                intent.putExtra("postReportedId", reportItem.id)
                startActivity(intent)
            }
        )
        binding.recyclerViewReports.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reportsAdapter
        }
    }

    private fun fetchReportedPosts() {
        binding.progressBar.visibility = View.VISIBLE
        val reportsRef = Utility.database.getReference("reports").child("post_reports")

        reportsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reportMap = mutableMapOf<String, ReportItem>()

                for (reportSnapshot in snapshot.children) {
                    val report = reportSnapshot.getValue(Report::class.java)
                    val postId = reportSnapshot.child("postId").getValue(String::class.java)
                    val postContent = reportSnapshot.child("postContent").getValue(String::class.java)
                    val authorId = reportSnapshot.child("authorId").getValue(String::class.java)

                    if (report != null && postId != null) {
                        val existingReport = reportMap[postId]
                        val updatedReports = (existingReport?.reports ?: emptyList()) + report

                        reportMap[postId] = ReportItem(
                            id = postId,
                            content = postContent ?: "",
                            authorId = authorId ?: "",
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

    private fun deleteReportedPost(postId: String) {
        // Referensi ke database
        val postsRef = Utility.database.getReference("communityposts")
        val reportsRef = Utility.database.getReference("reports")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Hapus")
            .setMessage("Anda yakin ingin menghapus postingan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                binding.progressBar.visibility = View.VISIBLE

                // Ambil detail postingan untuk mendapatkan URL thumbnail
                postsRef.child(postId).get().addOnSuccessListener { snapshot ->
                    val post = snapshot.getValue(CommunityPost::class.java)

                    // Fungsi untuk menghapus laporan
                    fun deleteReports() {
                        // Hapus laporan komentar untuk postingan ini
                        reportsRef.child("comment_reports")
                            .orderByChild("postId")
                            .equalTo(postId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(reportSnapshot: DataSnapshot) {
                                    for (report in reportSnapshot.children) {
                                        report.ref.removeValue()
                                    }

                                    // Hapus laporan postingan
                                    reportsRef.child("post_reports")
                                        .orderByChild("postId")
                                        .equalTo(postId)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(postReportSnapshot: DataSnapshot) {
                                                for (postReport in postReportSnapshot.children) {
                                                    postReport.ref.removeValue()
                                                }

                                                // Hapus postingan dari database
                                                // Ini akan otomatis menghapus komentar karena komentar ada di bawah node postingan
                                                postsRef.child(postId).removeValue()
                                                    .addOnSuccessListener {
                                                        binding.progressBar.visibility = View.GONE
                                                        Utility.showToast(requireContext(), "Postingan dan komentar berhasil dihapus")
                                                        fetchReportedPosts()
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        binding.progressBar.visibility = View.GONE
                                                        Utility.showToast(requireContext(), "Gagal menghapus postingan: ${exception.localizedMessage}")
                                                    }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                binding.progressBar.visibility = View.GONE
                                                Utility.showToast(requireContext(), "Gagal menghapus laporan postingan: ${error.message}")
                                            }
                                        })
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    binding.progressBar.visibility = View.GONE
                                    Utility.showToast(requireContext(), "Gagal menghapus laporan komentar: ${error.message}")
                                }
                            })
                    }

                    // Proses penghapusan
                    if (post?.thumbnail != null) {
                        try {
                            // Hapus gambar dari Firebase Storage
                            val storageReference = Utility.storage.getReferenceFromUrl(post.thumbnail)
                            storageReference.delete().addOnCompleteListener { storageTask ->
                                if (storageTask.isSuccessful) {
                                    // Gambar berhasil dihapus, lanjutkan proses
                                    deleteReports()
                                } else {
                                    // Gagal menghapus gambar, tetap lanjutkan proses
                                    deleteReports()
                                    Log.e("No image attach", "tidak ada gambar yang dihapus: ${storageTask.exception}")
                                }
                            }
                        } catch (e: Exception) {
                            // Tangani kesalahan jika URL tidak valid atau referensi tidak bisa dibuat
                            Log.e("No image attach", "tidak ada gambar yang dihapus: ${e.localizedMessage}")
                            deleteReports()
                        }
                    } else {
                        // Tidak ada gambar, langsung hapus laporan dan postingan
                        deleteReports()
                    }
                }.addOnFailureListener { databaseException ->
                    binding.progressBar.visibility = View.GONE
                    Utility.showToast(requireContext(), "Gagal mengambil detail postingan: ${databaseException.localizedMessage}")
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteReports(postId: String) {
        val reportsRef = Utility.database.getReference("reports").child("post_reports")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Hapus Laporan")
            .setMessage("Anda yakin ingin menghapus semua laporan untuk postingan ini tanpa menghapus postingannya?")
            .setPositiveButton("Hapus Laporan") { _, _ ->
                binding.progressBar.visibility = View.VISIBLE

                // Hapus semua laporan terkait postingan
                reportsRef.orderByChild("postId").equalTo(postId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (reportSnapshot in snapshot.children) {
                                reportSnapshot.ref.removeValue()
                            }

                            binding.progressBar.visibility = View.GONE
                            Utility.showToast(requireContext(), "Laporan berhasil dihapus")

                            // Refresh daftar laporan
                            fetchReportedPosts()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}