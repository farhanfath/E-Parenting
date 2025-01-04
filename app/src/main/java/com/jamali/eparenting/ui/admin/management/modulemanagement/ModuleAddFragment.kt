package com.jamali.eparenting.ui.admin.management.modulemanagement

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.jamali.eparenting.R
import com.jamali.eparenting.data.model.PostType
import com.jamali.eparenting.databinding.FragmentModuleManagementAddBinding
import com.jamali.eparenting.di.Injection
import com.jamali.eparenting.utils.DateTimePickerUtil
import com.jamali.eparenting.utils.Result
import com.jamali.eparenting.utils.TimeUtils.formatDateToReadable
import com.jamali.eparenting.viewmodel.ModuleViewModel
import kotlinx.coroutines.launch

class ModuleAddFragment : Fragment() {

    private val viewModelFactory by lazy {
        Injection.provideVieModelFactory()
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[(ModuleViewModel::class.java)]
    }

    private var selectedPdfUri: Uri? = null

    private val pdfPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPdfUri = it
            showPdfPreview(it)
            // Update UI to show selected file name
            binding.tvSelectedFile.text = getFileName(it)
            binding.cardPdfPreview.visibility = View.VISIBLE
        }
    }

    private var selectedModuleType: PostType = PostType.UMUM

    private var _binding: FragmentModuleManagementAddBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentModuleManagementAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        setupUI()

    }

    private fun setupSpinner() {
        val types = PostType.entries.map { it.name }.toTypedArray()
        val adapter = ArrayAdapter(requireContext(), R.layout.item_forum_type, types)
        binding.insertTypeItem.setAdapter(adapter)
        binding.insertTypeItem.setOnItemClickListener { _, _, position, _ ->
            val selectedTypes = types[position]
            selectedModuleType = PostType.valueOf(selectedTypes)
        }
    }

    private fun setupUI() {
        binding.loadingLayout.loadingText.text = getString(R.string.add_module_text)

        binding.edtTanggalRilis.setOnClickListener {
            DateTimePickerUtil.showDatePicker(requireContext(), binding.edtTanggalRilis) { selectedDate ->
                val newDate = selectedDate.formatDateToReadable()
                binding.edtTanggalRilis.setText(newDate)
            }
        }

        binding.btnUploadPdf.setOnClickListener {
            pdfPickerLauncher.launch("application/pdf")
        }

        binding.btnSimpan.setOnClickListener {
            validateAndSaveModule()
        }
    }

    private fun validateAndSaveModule() {
        val judul = binding.edtJudul.text.toString()
        val tanggalRilis = binding.edtTanggalRilis.text.toString()

        when {
            judul.isEmpty() -> {
                binding.tilJudul.error = "Judul tidak boleh kosong"
            }
            tanggalRilis.isEmpty() -> {
                binding.tilTanggalRilis.error = "Tanggal rilis tidak boleh kosong"
            }
            selectedPdfUri == null -> {
                Toast.makeText(requireContext(), "Pilih file PDF terlebih dahulu", Toast.LENGTH_SHORT).show()
                return
            }
            !isFileSizeValid(selectedPdfUri!!) -> {
                Toast.makeText(requireContext(), "Ukuran file PDF tidak boleh lebih dari 5MB", Toast.LENGTH_SHORT).show()
                return
            }
            else -> {
                binding.tilJudul.error = null
                binding.tilTanggalRilis.error = null

                viewModel.createModule(
                    title = judul,
                    releaseDate = tanggalRilis,
                    pdfUri = selectedPdfUri!!,
                    moduleType = selectedModuleType
                )
                observeViewModel()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.operationState.collect { result ->
                when(result) {
                    is Result.Loading -> {
                        onLoading(true)
                    }
                    is Result.Success -> {
                        onLoading(false)
                        Toast.makeText(requireContext(), "Berhasil menambahkan modul", Toast.LENGTH_SHORT).show()
                        clearInput()
                        findNavController().popBackStack()
                    }
                    is Result.Error -> {
                        onLoading(false)
                        Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    private fun isFileSizeValid(uri: Uri): Boolean {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            it.moveToFirst()
            val fileSize = it.getLong(sizeIndex)
            val maxSize = 5L * 1024 * 1024 // 5MB in bytes
            return fileSize <= maxSize
        }
        return false
    }

    private fun showPdfPreview(uri: Uri) {
        binding.pdfView.fromUri(uri)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .load()
    }

    private fun getFileName(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        } ?: "File PDF dipilih"
    }

    private fun clearInput() {
        binding.edtJudul.text?.clear()
        binding.edtTanggalRilis.text?.clear()
        binding.tvSelectedFile.text = getString(R.string.no_file_selected)
        binding.cardPdfPreview.visibility = View.GONE
    }

    private fun onLoading(state: Boolean) {
        if (state) {
            binding.loadingLayout.root.visibility = View.VISIBLE
        } else {
            binding.loadingLayout.root.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}