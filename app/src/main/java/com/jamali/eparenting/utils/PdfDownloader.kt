package com.jamali.eparenting.utils

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PdfDownloader(private val context: Context) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun downloadPdf(pdfUrl: String, title: String) {
        if (!checkStoragePermission()) {
            requestStoragePermission()
            return
        }

        try {
            val fileName = "$title.pdf"
            val request = DownloadManager.Request(Uri.parse(pdfUrl))
                .setTitle(fileName)
                .setDescription("Mengunduh buletin...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setMimeType("application/pdf")

            downloadManager.enqueue(request)
            Toast.makeText(context, "Mengunduh buletin...", Toast.LENGTH_SHORT).show()

            // Register broadcast receiver to listen for download completion
            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    context?.unregisterReceiver(this)
                    Toast.makeText(
                        context,
                        "Buletin berhasil diunduh",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(
                        onComplete,
                        IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                        Context.RECEIVER_NOT_EXPORTED,
                    )
                }
            }

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Gagal mengunduh buletin: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true // Android 10 and above don't need storage permission for downloads
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (context is Activity) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
    }
}