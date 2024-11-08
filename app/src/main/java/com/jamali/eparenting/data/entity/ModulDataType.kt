package com.jamali.eparenting.data.entity

import android.os.Parcelable
import com.jamali.eparenting.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModulDataType(
    val title: String,
    val imageResId: Int,
    val type: String,
) : Parcelable

object ModulTemplateData {
    val forumItems = listOf(
        ModulDataType("Pra Nikah", R.drawable.pranikah_community_forum, "Pranikah"),
        ModulDataType("Balita", R.drawable.balita_community_forum, "Balita"),
        ModulDataType("Remaja", R.drawable.sd_community_forum, "Remaja"),
        ModulDataType("SMP", R.drawable.smp_community_forum, "SMP"),
        ModulDataType("SMA", R.drawable.sma_community_forum, "SMA")
    )
}