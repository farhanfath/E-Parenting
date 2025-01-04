package com.jamali.eparenting.ui.admin.management.modulemanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jamali.eparenting.R
import com.jamali.eparenting.data.model.Module
import com.jamali.eparenting.databinding.FragmentModuleManagementListBinding
import com.jamali.eparenting.di.Injection
import com.jamali.eparenting.ui.adapters.ModuleManagementListAdapter
import com.jamali.eparenting.ui.adapters.ModuleOptionsBottomSheetDialog
import com.jamali.eparenting.utils.PdfDownloader
import com.jamali.eparenting.utils.RecyclerViewUtils
import com.jamali.eparenting.utils.Result
import com.jamali.eparenting.viewmodel.ModuleViewModel
import kotlinx.coroutines.launch

open class ModuleListFragment : Fragment() {

    open var hideAdminFunction: Boolean = false

    private val viewModelFactory by lazy {
        Injection.provideVieModelFactory()
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[(ModuleViewModel::class.java)]
    }

    private lateinit var adapter: ModuleManagementListAdapter

    private lateinit var pdfDownloader: PdfDownloader

    private var _binding: FragmentModuleManagementListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentModuleManagementListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pdfDownloader = PdfDownloader(requireContext())
        binding.textEmpty.text = getString(R.string.no_module_text)

        setupFab()
        setupRvAdapter()
        setupObservers()

    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_nav_management_module_list_to_nav_management_module_add)
        }
        when(hideAdminFunction) {
            true -> binding.fabAdd.visibility = View.GONE
            false -> binding.fabAdd.visibility = View.VISIBLE
        }
    }

    private fun setupRvAdapter() {
        adapter = ModuleManagementListAdapter(
            onClick = { module ->
                ModuleOptionsBottomSheetDialog(
                    onLihatClick = {
                        PdfViewerActivity.start(requireContext(), module.isi, module.title)
                    },
                    onUnduhClick = {
                        pdfDownloader.downloadPdf(
                            pdfUrl = module.isi,
                            title = module.title
                        )
                    }
                ).show(childFragmentManager, "module_options")
            },
            onDeleteClick = { module, _ ->
                showDeleteBuletinDialog(module)
            },
            hideDeleteButton = hideAdminFunction
        )

        binding.rvAdmin.apply {
            adapter = this@ModuleListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.moduleState.collect { result ->
                when (result) {
                    is Result.Success -> {
                        showLoadingState(false)
                        adapter.submitList(result.data)
                        RecyclerViewUtils.setEmptyState(result.data.isEmpty(), emptyView = binding.emptyView, recyclerView = binding.rvAdmin)
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

    private fun showDeleteBuletinDialog(modul: Module) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Buletin")
            .setMessage("Apakah Anda yakin ingin menghapus modul ini beserta seluruh isinya?")
            .setPositiveButton("Hapus") { _, _ ->
                showLoadingState(true, "Menghapus Modul...")
                viewModel.deleteModule(modul)
            }
            .setNegativeButton("Batal", null)
            .show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}