package com.jamali.eparenting.ui.home.fragments.forum.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jamali.eparenting.R
import com.jamali.eparenting.data.entity.ForumCommunityTypeItem
import com.jamali.eparenting.databinding.FragmentSubForumCommunityBinding
import com.jamali.eparenting.ui.home.adapters.ForumCommunityAdapter

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

        val forumItems = listOf(
            ForumCommunityTypeItem("Pra Nikah", R.drawable.pranikah_community_forum),
            ForumCommunityTypeItem("Balita", R.drawable.balita_community_forum),
            ForumCommunityTypeItem("Sekolah Dasar", R.drawable.sd_community_forum),
            ForumCommunityTypeItem("SMP", R.drawable.smp_community_forum),
            ForumCommunityTypeItem("SMA", R.drawable.sma_community_forum)
        )

        binding.rvTypeForum.adapter = ForumCommunityAdapter(forumItems)
        binding.rvTypeForum.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}