package com.jamali.eparenting.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.jamali.eparenting.R
import com.jamali.eparenting.databinding.ActivityWelcomeBinding
import com.jamali.eparenting.ui.auth.LoginActivity
import com.jamali.eparenting.ui.auth.PhoneAuthActivity
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
        setupViewPagerListener()
    }

    private fun setupData() {
        binding.btnLoginEmail.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.btnLoginPhone.setOnClickListener {
            startActivity(Intent(this, PhoneAuthActivity::class.java))
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

        binding.authCv.visibility = View.GONE
        binding.authCv.alpha = 0f
    }

    private fun setupViewPagerListener() {
        binding.welcomeSlides.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // Tampilkan tombol hanya di slide terakhir
                if (position == adapter.itemCount - 1) {
                    showButtonWithSwipeUpAnimation()
                } else {
                    binding.authCv.visibility = View.GONE
                    binding.authCv.translationY = 0f
                    binding.authCv.alpha = 0f
                }
            }
        })
    }

    private fun showButtonWithSwipeUpAnimation() {
        // Atur visibilitas dan posisi awal
        binding.authCv.visibility = View.VISIBLE
        binding.authCv.translationY = 200f  // Geser ke bawah sebelum animasi
        binding.authCv.alpha = 0f

        // Animasi translasi (swipe up)
        val translationAnimator = ObjectAnimator.ofFloat(
            binding.authCv,
            "translationY",
            200f,
            0f
        ).apply {
            duration = 500  // Durasi animasi 500 milidetik
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Animasi alpha (fade in)
        val alphaAnimator = ObjectAnimator.ofFloat(
            binding.authCv,
            "alpha",
            0f,
            1f
        ).apply {
            duration = 500
        }

        // Jalankan animasi bersamaan
        translationAnimator.start()
        alphaAnimator.start()
    }
}