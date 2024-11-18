package com.jamali.eparenting.ui.customer.fragments.forum.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jamali.eparenting.data.ForumCommunityData
import com.jamali.eparenting.databinding.FragmentSubForumCommunityBinding
import com.jamali.eparenting.ui.customer.adapters.ForumCommunityAdapter

class CommunityFragment : Fragment() {

    private var _binding: FragmentSubForumCommunityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSubForumCommunityBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvTypeForum.adapter = ForumCommunityAdapter(ForumCommunityData.forumItems)
        binding.rvTypeForum.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}