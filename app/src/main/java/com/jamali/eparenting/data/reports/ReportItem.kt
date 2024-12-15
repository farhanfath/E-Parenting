package com.jamali.eparenting.data.reports

// Data class untuk item yang dilaporkan
data class ReportItem(
    val id: String = "", // ID laporan atau ID entitas yang dilaporkan
    val postId: String = "", // dibutuhkan untuk manage komentar
    val content: String = "", // Konten yang dilaporkan (teks post/komentar/profil)
    val authorId: String = "", // ID pembuat konten
    val reportCount: Int = 0,
    val reports: List<Report> = emptyList()
)

// Data class untuk laporan
data class Report(
    val reporterId: String = "",
    val reason: String = "",
    val timestamp: Long = 0
)