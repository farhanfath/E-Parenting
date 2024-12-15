package com.jamali.eparenting.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jamali.eparenting.ui.customer.fragments.forum.community.CommunityFragment
import com.jamali.eparenting.ui.customer.fragments.forum.personal.PersonalFragment

class ForumViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments = listOf(PersonalFragment(), CommunityFragment())

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}