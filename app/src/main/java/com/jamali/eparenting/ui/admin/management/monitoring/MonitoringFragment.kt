package com.jamali.eparenting.ui.admin.management.monitoring

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.databinding.FragmentMonitoringBinding
import com.jamali.eparenting.utils.Utility

class MonitoringFragment : Fragment() {

    private var _binding: FragmentMonitoringBinding? = null
    private val binding get() = _binding!!

    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pieChart = binding.userStatisticsChart
        setupUserStatisticsChart()

    }

    private fun setupUserStatisticsChart() {
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(false)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)

        val userRef = Utility.database.reference.child("users")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCount = snapshot.children.count {
                    it.child("role").value == "customer"
                }
                val doctorCount = snapshot.children.count {
                    it.child("role").value == "doctor"
                }
                val adminCount = snapshot.children.count {
                    it.child("role").value == "admin"
                }

                val entries = arrayListOf(
                    PieEntry(userCount.toFloat(), "Customer"),
                    PieEntry(doctorCount.toFloat(), "Dokter"),
                    PieEntry(adminCount.toFloat(), "Admin")
                )

                val dataSet = PieDataSet(entries, "Statistik Pengguna")
                dataSet.colors = listOf(
                    ContextCompat.getColor(requireContext(), R.color.user_color),
                    ContextCompat.getColor(requireContext(), R.color.doctor_color),
                    ContextCompat.getColor(requireContext(), R.color.admin_color)
                )

                dataSet.valueTextColor = Color.BLACK
                dataSet.valueTextSize = 12f

                val data = PieData(dataSet)
                data.setValueFormatter(PercentFormatter(pieChart))

                pieChart.data = data
                pieChart.animateY(1000)
                pieChart.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}