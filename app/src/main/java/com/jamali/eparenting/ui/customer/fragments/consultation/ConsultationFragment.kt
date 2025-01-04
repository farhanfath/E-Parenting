package com.jamali.eparenting.ui.customer.fragments.consultation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.data.model.User
import com.jamali.eparenting.databinding.FragmentConsultationBinding
import com.jamali.eparenting.ui.adapters.ConsultationAdapter
import com.jamali.eparenting.utils.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConsultationFragment : Fragment() {

    private var _binding: FragmentConsultationBinding? = null
    private val binding get() = _binding!!

    /**
     * Daftar konsultasi expert yang akan ditampilkan di RecyclerView.
     */
    private val consultationExpertList = mutableListOf<User>()
    private lateinit var consultationAdapter: ConsultationAdapter

    private var valueEventListener: ValueEventListener? = null
    private var databaseReference: DatabaseReference? = null

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

        setupView()
        rvAdapterSetup()
        loadConsultationExpert()
    }

    private fun setupView() {
        binding.apply {
            tvTitle.text = getString(R.string.konsultasi_text)
            noRvConsultationData.text = getString(R.string.empty_rv_consultation_customer)
        }
    }

    private fun rvAdapterSetup() {
        consultationAdapter = ConsultationAdapter(consultationExpertList)
        binding.rvConsultation.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = consultationAdapter
        }
    }

    private fun loadConsultationExpert() {
        databaseReference = Utility.database.getReference("users")

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Periksa apakah Fragment masih terhubung ke Activity
                if (!isAdded()) return

                consultationExpertList.clear()
                val tempList = mutableListOf<User>()

                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java)
                    if (user != null && user.role == "doctor" && user.status) {
                        tempList.add(user)
                    }
                }

                consultationExpertList.addAll(tempList.reversed())

                // Gunakan view lifecycle owner untuk memastikan Fragment masih aktif
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    consultationAdapter.notifyDataSetChanged()

                    binding.noRvConsultationData.visibility = if (consultationExpertList.isEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded()) return

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    context?.let {
                        Utility.showToast(it, error.message)
                    }
                }
            }
        }

        // Tambahkan listener
        valueEventListener?.let { listener ->
            databaseReference?.addValueEventListener(listener)
        }
    }

    override fun onDestroyView() {
        // Hapus listener saat Fragment di-destroy
        valueEventListener?.let { listener ->
            databaseReference?.removeEventListener(listener)
        }
        _binding = null
        super.onDestroyView()
    }
}