package com.jamali.eparenting.ui.customer.fragments.consultation

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.jamali.eparenting.R
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ActivityDoctorDetailBinding
import com.jamali.eparenting.ui.rules.CompleteRulesActivity

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

        setupRuleConsultation()

        setupDataDoctor(doctorData)
    }

    private fun setupRuleConsultation() {
        val text = getString(R.string.consultationRule)
        val spannableString = SpannableString(text)

        val start = text.indexOf("aturan komunitas")
        val end = start + "aturan komunitas".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@DoctorDetailActivity, CompleteRulesActivity::class.java)
                intent.putExtra(
                    CompleteRulesActivity.EXTRA_RULES_TYPE,
                    CompleteRulesActivity.RULES_TYPE_COMMUNITY
                )
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true  // Tambahkan garis bawah
                ds.color = ContextCompat.getColor(this@DoctorDetailActivity, R.color.blue)
            }
        }

        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.consultationRule.text = spannableString
        binding.consultationRule.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupDataDoctor(doctorData: User?) {
        if (doctorData != null) {
            binding.apply {
                doctorNameTextView.text = doctorData.username
                detailsTextView.text = doctorData.description
                doctorSpecialityTextView.text = doctorData.speciality
                scheduleTextView.text = doctorData.activeDay

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