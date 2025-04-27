package com.jamali.eparenting.ui.customer.modul

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jamali.eparenting.R
import com.jamali.eparenting.data.model.PostType
import com.jamali.eparenting.databinding.ActivityListModulBinding
import com.jamali.eparenting.di.Injection
import com.jamali.eparenting.ui.adapters.ModuleOptionsBottomSheetDialog
import com.jamali.eparenting.ui.adapters.ModuleUserListAdapter
import com.jamali.eparenting.ui.admin.management.modulemanagement.PdfViewerActivity
import com.jamali.eparenting.utils.PdfDownloader
import com.jamali.eparenting.utils.RecyclerViewUtils
import com.jamali.eparenting.utils.Result
import com.jamali.eparenting.viewmodel.ModuleViewModel
import kotlinx.coroutines.launch

class ModulActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListModulBinding

    private val viewModelFactory by lazy {
        Injection.provideVieModelFactory()
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[(ModuleViewModel::class.java)]
    }

    private lateinit var pdfDownloader: PdfDownloader

    private lateinit var adapter: ModuleUserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListModulBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val modulType = intent.getStringExtra("type")

        binding.btnBackFromListMaterial.setOnClickListener {
            finish()
        }
        binding.tvTitleListMaterial.text = modulType

        if (modulType != null) {
            setupRvAdapter(modulType)
        }
        setupObservers()

    }

    private fun setupRvAdapter(type: String) {
        val normalizeType = when(type) {
            "Pranikah" -> PostType.PRANIKAH
            "Balita" -> PostType.BALITA
            "SD" -> PostType.SD
            "SMP" -> PostType.SMP
            "SMA" -> PostType.SMA
            else -> PostType.UMUM
        }
        adapter = ModuleUserListAdapter(
            moduleType = normalizeType,
            onClick = { module ->
                ModuleOptionsBottomSheetDialog(
                    onLihatClick = {
                        PdfViewerActivity.start(this, module.isi, module.title)
                    },
                    onUnduhClick = {
                        pdfDownloader.downloadPdf(
                            pdfUrl = module.isi,
                            title = module.title
                        )
                    }
                ).show(supportFragmentManager, "module_options")
            }
        )

        binding.rvListModulByType.apply {
            adapter = this@ModulActivity.adapter
            layoutManager = LinearLayoutManager(this@ModulActivity)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.moduleState.collect { result ->
                when (result) {
                    is Result.Success -> {
                        showLoadingState(false)
                        adapter.submitFilteredList(result.data)
                        RecyclerViewUtils.setEmptyState(result.data.isEmpty(), emptyView = binding.emptyView, recyclerView = binding.rvListModulByType)
                    }
                    is Result.Error -> {
                        showLoadingState(false)
                    }
                    is Result.Loading -> {
                        showLoadingState(true)
                    }
                }
            }
        }
    }

    private fun showLoadingState(state: Boolean, message: String = getString(R.string.loading_module_text)) {
        binding.loadingLayout.loadingText.text = message
        if (state) {
            binding.loadingLayout.root.visibility = View.VISIBLE
        } else {
            binding.loadingLayout.root.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getALlModules()
    }
}