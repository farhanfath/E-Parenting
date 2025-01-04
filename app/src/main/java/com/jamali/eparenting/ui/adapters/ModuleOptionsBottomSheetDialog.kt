package com.jamali.eparenting.ui.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jamali.eparenting.databinding.LayoutModuleDialogBinding

class ModuleOptionsBottomSheetDialog (
private val onUnduhClick: () -> Unit,
private val onLihatClick: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding : LayoutModuleDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutModuleDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnUnduhPdf.setOnClickListener {
            onUnduhClick.invoke()
            dismiss()
        }

        binding.btnLihatPdf.setOnClickListener {
            onLihatClick.invoke()
            dismiss()
        }
    }
}