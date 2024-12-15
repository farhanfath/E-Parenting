package com.jamali.eparenting.ui.admin.management.reports.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.jamali.eparenting.ui.customer.user.UserProfileActivity
import com.jamali.eparenting.utils.Utility

class UserReportFragment : Fragment() {

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
        fetchReportedUsers()
    }

    private fun setupRecyclerView() {
        reportsAdapter = ReportDataAdapter(
            type = "user",
            onDeleteContentClickListener = {},
            onDeleteReportClickListener = { reportedUserId ->
                deleteReports(reportedUserId)
            },
            onUnblockUserClickListener = { userId ->
                showUnblockConfirmationDialog(userId)
            },
            onBlockUserClickListener = { userId ->
                showBlockConfirmationDialog(userId)
            },
            onItemClickListener = { reportItem ->
                UserProfileActivity.startActivity(requireContext(), reportItem.id)
            }
        )
        binding.recyclerViewReports.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reportsAdapter
        }
    }

    private fun showBlockConfirmationDialog(userId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Blokir Pengguna")
            .setMessage("Apakah Anda yakin ingin memblokir pengguna ini? Pengguna tidak akan dapat mengakses akun.")
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Blokir") { dialog, _ ->
                blockReportedUser(userId)
                dialog.dismiss()
            }
            .show()
    }

    private fun showUnblockConfirmationDialog(userId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Buka Blokir Pengguna")
            .setMessage("Apakah Anda yakin ingin membuka blokir pengguna ini? Pengguna akan dapat kembali mengakses akun.")
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Buka Blokir") { dialog, _ ->
                unblockReportedUser(userId)
                dialog.dismiss()
            }
            .show()
    }

    private fun blockReportedUser(userId: String) {
        val usersRef = Utility.database.getReference("users")
        usersRef.child(userId).child("accountStatus").setValue("blocked")
            .addOnSuccessListener {
                // Opsional: Tambahkan notifikasi atau toast sukses
                Toast.makeText(requireContext(), "Pengguna berhasil diblokir", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Tangani jika proses blokir gagal
                Toast.makeText(requireContext(), "Gagal memblokir pengguna", Toast.LENGTH_SHORT).show()
            }
    }

    private fun unblockReportedUser(userId: String) {
        val usersRef = Utility.database.getReference("users")
        usersRef.child(userId).child("accountStatus").setValue("active")
            .addOnSuccessListener {
                // Opsional: Tambahkan notifikasi atau toast sukses
                Toast.makeText(requireContext(), "Pengguna berhasil dibuka blokir", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Tangani jika proses buka blokir gagal
                Toast.makeText(requireContext(), "Gagal membuka blokir pengguna", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchReportedUsers() {
        binding.progressBar.visibility = View.VISIBLE
        val reportsRef = Utility.database.getReference("reports").child("user_reports")

        reportsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reportMap = mutableMapOf<String, ReportItem>()

                for (reportSnapshot in snapshot.children) {
                    val report = reportSnapshot.getValue(Report::class.java)
                    val reportedUserId = reportSnapshot.child("reportedUserId").getValue(String::class.java)
                    val reportedUsername = reportSnapshot.child("reportedUsername").getValue(String::class.java)

                    if (report != null && reportedUserId != null) {
                        val existingReport = reportMap[reportedUserId]
                        val updatedReports = (existingReport?.reports ?: emptyList()) + report

                        reportMap[reportedUserId] = ReportItem(
                            id = reportedUserId,
                            reportCount = updatedReports.size,
                            content = reportedUsername ?: "",
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

    private fun deleteReports(reportedUserId: String) {
        val reportsRef = Utility.database.getReference("reports").child("user_reports")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Hapus Laporan")
            .setMessage("Anda yakin ingin menghapus semua laporan untuk akun ini?")
            .setPositiveButton("Hapus Laporan") { _, _ ->
                binding.progressBar.visibility = View.VISIBLE

                // Hapus semua laporan terkait postingan
                reportsRef.orderByChild("reportedUserId").equalTo(reportedUserId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (reportSnapshot in snapshot.children) {
                                reportSnapshot.ref.removeValue()
                            }

                            binding.progressBar.visibility = View.GONE
                            Utility.showToast(requireContext(), "Laporan berhasil dihapus")

                            // Refresh daftar laporan
                            fetchReportedUsers()
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