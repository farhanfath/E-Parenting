package com.jamali.eparenting.ui.customer.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jamali.eparenting.utils.Utility
import com.jamali.eparenting.databinding.LayoutLogoutDialogBinding
import com.jamali.eparenting.ui.auth.LoginActivity

class LogOutFragment : BottomSheetDialogFragment() {

    private var _binding: LayoutLogoutDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutLogoutDialogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutBtn.setOnClickListener {
            Utility.auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        binding.cancelBtn.setOnClickListener {
            dismiss()
        }
    }
}