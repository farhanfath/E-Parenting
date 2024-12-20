package com.jamali.eparenting.ui.customer.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.jamali.eparenting.R
import com.jamali.eparenting.utils.Utility
import com.jamali.eparenting.databinding.FragmentProfileBinding
import com.jamali.eparenting.ui.customer.user.UserProfileActivity
import com.jamali.eparenting.ui.doctor.EditDoctorActivity
import com.jamali.eparenting.ui.rules.CompleteRulesActivity

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
            val logoutFragment =
                LogOutFragment()
            logoutFragment.show(childFragmentManager, logoutFragment.tag)
        }

        binding.ivProfilePicture.setOnClickListener {
            UserProfileActivity.startActivity(requireContext(), userId = Utility.auth.currentUser?.uid.toString())
        }

//        binding.containerLayoutEditProfile.setOnClickListener{
//            val intent = Intent(requireContext(), UpdateProfileActivity::class.java)
//            startActivity(intent)
//        }

        binding.containerLayoutEditPassword.setOnClickListener {
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            val intent = Intent(requireContext(), CompleteRulesActivity::class.java)
            intent.putExtra(
                CompleteRulesActivity.EXTRA_RULES_TYPE,
                CompleteRulesActivity.RULES_TYPE_PRIVACY
            )
            startActivity(intent)
        }

        binding.btnTermsOfService.setOnClickListener {
            val intent = Intent(requireContext(), CompleteRulesActivity::class.java)
            intent.putExtra(
                CompleteRulesActivity.EXTRA_RULES_TYPE,
                CompleteRulesActivity.RULES_TYPE_TERMSOFSERVICES
            )
            startActivity(intent)
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
            val userProfile = data.child("profile").value.toString()
            if (userProfile.isNotEmpty()) {
                Glide.with(this)
                    .load(userProfile)
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .into(binding.ivProfilePicture)
            }

            val role = data.child("role").value.toString()

            binding.containerLayoutEditProfile.setOnClickListener {
                setupEditProfileNavigation(role)
            }

        }.addOnFailureListener {
            showLoading(false)
            binding.tvUsername.text = getString(R.string.failed_get_name)
        }
    }

    private fun setupEditProfileNavigation(role: String) {
        when(role) {
            "customer" -> {
                val intent = Intent(requireContext(), UpdateProfileActivity::class.java)
                startActivity(intent)
            }
            "doctor" -> {
                val intent = Intent(requireContext(), EditDoctorActivity::class.java)
                startActivity(intent)
            }
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

    override fun onResume() {
        super.onResume()
        showUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}