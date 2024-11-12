package com.jamali.eparenting.ui.home.fragments.forum.detailforum

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jamali.eparenting.R
import com.jamali.eparenting.databinding.ActivityDetailForumBinding

class DetailForumActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetailForumBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailForumBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}