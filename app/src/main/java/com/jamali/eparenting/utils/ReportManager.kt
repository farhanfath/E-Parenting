package com.jamali.eparenting.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.ServerValue
import com.jamali.eparenting.data.Comment
import com.jamali.eparenting.data.CommunityPost
import com.jamali.eparenting.utils.Utility.auth
import com.jamali.eparenting.utils.Utility.database

object ReportManager {
    private val commonReasons = arrayOf(
        "Konten tidak pantas",
        "Spam",
        "Kekerasan",
        "Informasi palsu",
        "Lainnya"
    )

    fun reportPost(
        context: Context,
        community: CommunityPost,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val currentUserId = auth.currentUser?.uid ?: return

        showReportDialog(context, "Laporkan Postingan") { selectedReason ->
            val reportsRef = database.getReference("reports").child("post_reports")

            val reportData = hashMapOf(
                "postId" to community.id,
                "reporterId" to currentUserId,
                "authorId" to community.userId,
                "reason" to selectedReason,
                "timestamp" to ServerValue.TIMESTAMP,
                "postContent" to community.description,
                "postType" to community.type.name,
                "postUserName" to community.username,
                "thumbnail" to community.thumbnail
            )

            reportsRef.push().setValue(reportData)
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Postingan telah dilaporkan. Terima kasih atas laporan Anda.",
                        Toast.LENGTH_SHORT
                    ).show()
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        context,
                        "Gagal melaporkan postingan: ${exception.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ReportManager", "Post report submission failed", exception)
                    onFailure(exception)
                }
        }
    }

    // Metode untuk melaporkan komentar
    fun reportComment(
        context: Context,
        comment: Comment,
        communityPostId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val currentUserId = auth.currentUser?.uid ?: return

        showReportDialog(context, "Laporkan Komentar") { selectedReason ->
            val commentsReportsRef = database.getReference("reports").child("comment_reports")

            val reportData = hashMapOf(
                "commentId" to comment.id,
                "postId" to communityPostId,
                "reporterId" to currentUserId,
                "commentAuthorId" to comment.userId,
                "commentText" to comment.text,
                "reason" to selectedReason,
                "timestamp" to ServerValue.TIMESTAMP,
                "username" to comment.username
            )

            commentsReportsRef.push().setValue(reportData)
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Komentar telah dilaporkan. Terima kasih atas laporan Anda.",
                        Toast.LENGTH_SHORT
                    ).show()
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        context,
                        "Gagal melaporkan komentar: ${exception.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ReportManager", "Comment report submission failed", exception)
                    onFailure(exception)
                }
        }
    }

    // Metode untuk melaporkan pengguna
    fun reportUser(
        context: Context,
            reportedUserId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(context, "Anda harus login untuk melaporkan pengguna", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch current user's username first
        database.getReference("users").child(reportedUserId).child("username")
            .get()
            .addOnSuccessListener { usernameSnapshot ->
                val reportedUsername = usernameSnapshot.getValue(String::class.java) ?: "Pengguna Tidak Dikenal"

                showReportDialog(context, "Laporkan Pengguna") { selectedReason ->
                    val reportsRef = database.getReference("reports").child("user_reports")

                    val reportData = hashMapOf(
                        "reportedUserId" to reportedUserId,
                        "reporterId" to currentUserId,
                        "reportedUsername" to reportedUsername, // Added reported user's username
                        "reason" to selectedReason,
                        "timestamp" to ServerValue.TIMESTAMP
                    )

                    val reportKey = reportsRef.push().key

                    if (reportKey != null) {
                        reportsRef.child(reportKey).setValue(reportData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Pengguna berhasil dilaporkan", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    context,
                                    "Gagal melaporkan pengguna: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("ReportManager", "User report submission failed", exception)
                                onFailure(exception)
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Gagal mengambil username: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("ReportManager", "Failed to retrieve username", exception)
                onFailure(exception)
            }
    }

    private fun showReportDialog(
        context: Context,
        title: String,
        onReasonSelected: (String) -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setItems(commonReasons) { _, which ->
                val selectedReason = commonReasons[which]
                onReasonSelected(selectedReason)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}