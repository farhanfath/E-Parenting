package com.jamali.eparenting.ui.customer.fragments.consultation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.FragmentConsultationBinding
import com.jamali.eparenting.ui.customer.adapters.ConsultationAdapter
import com.jamali.eparenting.utils.Utility

class ConsultationFragment : Fragment() {

    private var _binding: FragmentConsultationBinding? = null
    private val binding get() = _binding!!

    /**
     * Daftar konsultasi expert yang akan ditampilkan di RecyclerView.
     */
    private val consultationExpertList = mutableListOf<User>()
    private lateinit var consultationAdapter: ConsultationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsultationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvAdapterSetup()

        loadConsultationExpert()
    }

    private fun rvAdapterSetup() {
        consultationAdapter = ConsultationAdapter(consultationExpertList)
        binding.rvConsultation.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = consultationAdapter
        }
    }

    private fun loadConsultationExpert() {
        val databaseReference = Utility.database.getReference("users")
        databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    consultationExpertList.clear()

                    // Gunakan temporary list untuk menampung data sebelum difilter dan diurutkan
                    val tempList = mutableListOf<User>()

                    // Iterasi snapshot dan filter berdasarkan type
                    for (data in snapshot.children) {
                        val post = data.getValue(User::class.java)
                        if (post != null && post.role == "doctor") {
                            tempList.add(post)
                        }
                    }

                    // Urutkan dari yang terbaru (descending) dan tambahkan ke communityList
                    consultationExpertList.addAll(tempList.reversed())

                    // Update UI di main thread
                    requireActivity().runOnUiThread {
                        consultationAdapter.notifyDataSetChanged()

                        // Optional: Tambahkan indikator jika list kosong
                        when {
                            consultationExpertList.isNotEmpty() -> {
                                // Sembunyikan empty state jika ada
                                binding.noExpertActive.visibility = View.GONE
                            }
                            else -> {
                                // Tampilkan empty state jika ada
                                binding.noExpertActive.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    requireActivity().runOnUiThread {
                        Utility.showToast(requireContext(), error.message)
                    }
                }
            })
    }
}