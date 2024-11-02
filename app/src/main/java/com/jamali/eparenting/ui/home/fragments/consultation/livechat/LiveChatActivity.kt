package com.jamali.eparenting.ui.home.fragments.consultation.livechat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.databinding.ActivityLiveChatBinding

class LiveChatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLiveChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}