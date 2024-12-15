package com.jamali.eparenting.ui.customer.fragments.forum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.jamali.eparenting.databinding.FragmentForumBinding
import com.jamali.eparenting.ui.adapters.ForumViewPagerAdapter

class ForumFragment : Fragment() {

    private var _binding: FragmentForumBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentForumBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = ForumViewPagerAdapter(childFragmentManager, lifecycle)
        binding.vpForum.adapter = adapter

        TabLayoutMediator(binding.tabsLayoutForum, binding.vpForum) { tab, position ->
            when (position) {
                0 -> tab.text = "Personal"
                1 -> tab.text = "Kategori"
            }
        }.attach()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}