package com.jamali.eparenting.ui.home.modul

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.R
import com.jamali.eparenting.databinding.ActivityDetailModulBinding
import com.jamali.eparenting.databinding.ActivityListModulBinding

class DetailModulActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailModulBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailModulBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imbBackDetail.setOnClickListener {
            finish()
        }
    }
}