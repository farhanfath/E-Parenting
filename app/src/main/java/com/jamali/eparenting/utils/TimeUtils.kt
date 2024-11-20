package com.jamali.eparenting.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {
    fun getTimeAgo(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - timestamp

        val seconds = timeDiff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 10 -> "Baru saja"
            seconds < 60 -> "$seconds detik yang lalu"
            minutes == 1L -> "Semenit yang lalu"
            minutes < 60 -> "$minutes menit yang lalu"
            hours == 1L -> "Sejam yang lalu"
            hours < 24 -> "$hours jam yang lalu"
            days == 1L -> "Kemarin"
            days < 7 -> "$days hari yang lalu"
            weeks == 1L -> "Seminggu yang lalu"
            weeks < 4 -> "$weeks minggu yang lalu"
            months == 1L -> "Sebulan yang lalu"
            months < 12 -> "$months bulan yang lalu"
            years == 1L -> "Setahun yang lalu"
            else -> "$years tahun yang lalu"
        }
    }

    // Format waktu detail (opsional)
    fun getDetailedTime(timestamp: Long): String {
        val date = Date(timestamp)
        val now = Date()
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))

        return if (isSameDay(date, now)) {
            "Hari ini ${SimpleDateFormat("HH:mm", Locale("id")).format(date)}"
        } else if (isYesterday(date)) {
            "Kemarin ${SimpleDateFormat("HH:mm", Locale("id")).format(date)}"
        } else {
            sdf.format(date)
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

    private fun isYesterday(date: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date
        cal2.add(Calendar.DAY_OF_YEAR, -1)
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

    fun formatTimestamp(timestamp: Long): String {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val netDate = Date(timestamp)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return "" // Return empty string if there's an error
        }
    }
}