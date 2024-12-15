package com.jamali.eparenting.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jamali.eparenting.ui.admin.management.reports.content.CommentReportFragment
import com.jamali.eparenting.ui.admin.management.reports.content.PostReportFragment
import com.jamali.eparenting.ui.admin.management.reports.content.UserReportFragment

class AdminReportViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle)
    : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments = listOf(PostReportFragment(), CommentReportFragment(), UserReportFragment())

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}