package com.jamali.eparenting.ui.home.modul

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jamali.eparenting.application.ResultData
import com.jamali.eparenting.application.Utility
import com.jamali.eparenting.application.ViewModelFactory
import com.jamali.eparenting.data.entity.ModulDataType
import com.jamali.eparenting.data.entity.fromapi.ModulItem
import com.jamali.eparenting.data.viewmodel.ModulViewModel
import com.jamali.eparenting.databinding.ActivityListModulBinding
import com.jamali.eparenting.ui.home.adapters.ModulTypeListAdapter

class ModulActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListModulBinding
    private val viewModel by viewModels<ModulViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListModulBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackFromListMaterial.setOnClickListener {
            finish()
        }

        val modulData = intent.getParcelableExtra<ModulDataType>("modul_data")
        val type = intent.getStringExtra("type").toString()
        binding.tvTitleListMaterial.text = modulData?.title

        getModulListData(type)
    }

    private fun getModulListData(type: String) {
        viewModel.getModulsByType(type).observe(this) { result ->
            when (result) {
                is ResultData.Loading -> Utility.showLoading(binding.progressBar, true)
                is ResultData.Success -> {
                    Utility.showLoading(binding.progressBar, false)
                    setModulList(result.data.modul)
                }

                is ResultData.Error -> {
                    Utility.showLoading(binding.progressBar, false)
                    Utility.showToast(this, result.error)
                }
            }
        }
    }

    private fun setModulList(modul: List<ModulItem>) {
        if (modul.isEmpty()) {
            Utility.showToast(this, "Tidak Ada Data Modul")
        } else {
            val adapter = ModulTypeListAdapter(modul)
            binding.rvListMaterial.layoutManager = LinearLayoutManager(this)
            binding.rvListMaterial.adapter = adapter
        }
    }
}