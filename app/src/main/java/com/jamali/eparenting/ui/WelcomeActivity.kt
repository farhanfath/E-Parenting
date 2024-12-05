package com.jamali.eparenting.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jamali.eparenting.R
import com.jamali.eparenting.databinding.ActivityWelcomeBinding
import com.jamali.eparenting.ui.auth.LoginActivity
import com.jamali.eparenting.ui.auth.RegisterActivity
import com.jamali.eparenting.ui.customer.adapters.WelcomeAdapter
import com.jamali.eparenting.ui.customer.adapters.WelcomeSlide

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var adapter: WelcomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupView()
        setupData()
    }

    private fun setupData() {
        binding.registerCvBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.toLoginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun setupView() {
        val slides = createSlides()
        setupViewPager(slides)
    }

    private fun createSlides(): List<WelcomeSlide> {
        val animations = resources.obtainTypedArray(R.array.slide_animations)
        val titles = resources.getStringArray(R.array.slide_titles)
        val descriptions = resources.getStringArray(R.array.slide_descriptions)

        val slides = mutableListOf<WelcomeSlide>()
        for (i in titles.indices) {
            slides.add(
                WelcomeSlide(
                    animations.getResourceId(i, -1),
                    titles[i],
                    descriptions[i]
                )
            )
        }
        animations.recycle()

        return slides
    }

    private fun setupViewPager(slides: List<WelcomeSlide>) {
        adapter = WelcomeAdapter(slides)
        binding.welcomeSlides.adapter = adapter
        binding.indicatorImageDetail.setViewPager(binding.welcomeSlides)
    }
}