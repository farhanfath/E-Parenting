package com.jamali.eparenting.ui.home.dashboard.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jamali.eparenting.R
import com.jamali.eparenting.Utility
import com.jamali.eparenting.databinding.FragmentProfileBinding
import com.jamali.eparenting.ui.auth.LoginActivity
import com.marsad.stylishdialogs.StylishAlertDialog

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogout.setOnClickListener {
            logoutHandler()
        }

        showUserProfile()
    }

    private fun showUserProfile() {
        showLoading(true)

        val userId = Utility.auth.currentUser?.uid.toString()
        val userRef = Utility.database.getReference("users").child(userId)

        userRef.get().addOnSuccessListener { data ->
            showLoading(false)
            binding.tvUsername.text = data.child("username").value.toString()
        }.addOnFailureListener {
            showLoading(false)
            binding.tvUsername.text = getString(R.string.failed_get_name)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.shimmerViewContainer.startShimmer()
            binding.shimmerViewContainer.visibility = View.VISIBLE
            binding.tvUsername.visibility = View.GONE
        } else {
            binding.shimmerViewContainer.stopShimmer()
            binding.shimmerViewContainer.visibility = View.GONE
            binding.tvUsername.visibility = View.VISIBLE
        }
    }

    private fun logoutHandler() {
        StylishAlertDialog(requireContext(), StylishAlertDialog.WARNING)
            .setTitleText("Apakah Anda yakin?")
            .setContentText("Anda akan keluar dari akun Anda!")
            .setConfirmText("Ya, keluar")
            .setConfirmClickListener {
                Utility.auth.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
            .setCancelButton("Batal",StylishAlertDialog::dismissWithAnimation)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}