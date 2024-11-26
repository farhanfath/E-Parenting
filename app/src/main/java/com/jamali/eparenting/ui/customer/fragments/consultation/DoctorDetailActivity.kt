package com.jamali.eparenting.ui.customer.fragments.consultation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jamali.eparenting.R
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ActivityDoctorDetailBinding

class DoctorDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val doctorData = intent.getParcelableExtra<User>("doctor")

        binding.backBtn.setOnClickListener {
            finish()
        }

        setupDataDoctor(doctorData)
    }

    private fun setupDataDoctor(doctorData: User?) {
        if (doctorData != null) {
            binding.apply {
                doctorNameTextView.text = doctorData.username
                detailsTextView.text = doctorData.description
                doctorSpecialityTextView.text = doctorData.speciality

                Glide.with(this@DoctorDetailActivity)
                    .load(doctorData.profile)
                    .error(R.drawable.ic_avatar)
                    .placeholder(R.drawable.ic_avatar)
                    .into(doctorImageView)
            }

            binding.contactNowButton.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("name", doctorData.username)
                intent.putExtra("email", doctorData.email)
                intent.putExtra("uid", doctorData.uid)
                intent.putExtra("profile", doctorData.profile)
                startActivity(intent)
                finish()
            }
        }
    }
}