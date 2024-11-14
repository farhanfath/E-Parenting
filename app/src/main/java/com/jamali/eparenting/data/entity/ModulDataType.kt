package com.jamali.eparenting.data.entity

import android.os.Parcelable
import androidx.annotation.ColorRes
import com.jamali.eparenting.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModulDataType(
    val title: String,
    val description: String,
    val imageResId: Int,
    val type: String,
    @ColorRes val backgroundColor: Int,
    @ColorRes val strokeColor: Int
) : Parcelable

object ModulTemplateData {
    val forumItems = listOf(
        ModulDataType(
            "Pra Nikah",
            "Persiapan menuju kehidupan berkeluarga",
            R.drawable.ic_modul_pranikah,
            "Pranikah",
            R.color.module_pra_nikah,
            R.color.module_pra_nikah_stroke
        ),
        ModulDataType(
            "Balita",
            "Panduan merawat dan mendidik balita",
            R.drawable.ic_modul_balita,
            "Balita",
            R.color.module_balita,
            R.color.module_balita_stroke
        ),
        ModulDataType(
            "SD",
            "Mendampingi anak usia sekolah dasar",
            R.drawable.ic_modul_sd,
            "SD",
            R.color.module_sd,
            R.color.module_sd_stroke
        ),
        ModulDataType(
            "SMP",
            "Membimbing anak usia remaja awal",
            R.drawable.ic_modul_smp,
            "SMP",
            R.color.module_smp,
            R.color.module_smp_stroke

        ),
        ModulDataType(
            "SMA",
            "Mendukung tumbuh kembang remaja",
            R.drawable.ic_modul_sma,
            "SMA",
            R.color.module_sma,
            R.color.module_sma_stroke
        )
    )
}