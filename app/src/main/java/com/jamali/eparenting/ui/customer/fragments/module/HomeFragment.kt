package com.jamali.eparenting.ui.customer.fragments.module

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jamali.eparenting.data.model.ModulTemplateData
import com.jamali.eparenting.databinding.FragmentHomeBinding
import com.jamali.eparenting.ui.adapters.ModulAdapter

//class HomeFragment : ModuleListFragment() {
//    override var hideAdminFunction: Boolean = true
//}

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvModul.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ModulAdapter(
                modulDataList = ModulTemplateData.forumItems
            )
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: android.graphics.Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.set(8, 8, 8, 8)
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

