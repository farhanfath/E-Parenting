package com.jamali.eparenting.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jamali.eparenting.data.repository.AppRepository
import com.jamali.eparenting.databinding.ActivityMainBinding
import com.jamali.eparenting.ui.home.adapters.CommunityAdapter
import com.jamali.eparenting.ui.home.post.PostActivity
import com.jamali.eparenting.utils.ViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CommunityAdapter
    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory(AppRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.postFab.setOnClickListener {
            startActivity(Intent(this, PostActivity::class.java))
        }

        adapter = CommunityAdapter(emptyList())
        binding.communityPostRv.layoutManager = LinearLayoutManager(this)
        binding.communityPostRv.adapter = adapter

        viewModel.communityList.observe(this) { data ->
            adapter.updateData(data)
        }
        viewModel.fetchAllCommunityData()

    }
}