package com.jamali.eparenting.ui.admin.management.modulemanagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jamali.eparenting.databinding.ActivityPdfViewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class PdfViewerActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_PDF_URL = "extra_pdf_url"
        private const val EXTRA_TITLE = "extra_title"

        fun start(context: Context, pdfUrl: String, title: String) {
            context.startActivity(Intent(context, PdfViewerActivity::class.java).apply {
                putExtra(EXTRA_PDF_URL, pdfUrl)
                putExtra(EXTRA_TITLE, title)
            })
        }
    }

    private lateinit var binding: ActivityPdfViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pdfUrl = intent.getStringExtra(EXTRA_PDF_URL) ?: return finish()
        val judul = intent.getStringExtra(EXTRA_TITLE) ?: "PDF Viewer"

        setupToolbar(judul)
        loadPdf(pdfUrl)
    }

    private fun setupToolbar(judul: String) {
        binding.toolbar.title = judul
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }


    private fun loadPdf(pdfUrl: String) {
        showLoading(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL(pdfUrl)
                val input = url.openStream()
                withContext(Dispatchers.Main) {
                    binding.pdfView.fromStream(input)
                        .defaultPage(0)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .onLoad {
                            showLoading(false)
                        }
                        .onError {
                            showLoading(false)
                            Toast.makeText(this@PdfViewerActivity, "Gagal memuat PDF", Toast.LENGTH_SHORT).show()
                        }
                        .load()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@PdfViewerActivity,"Gagal memuat PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(state: Boolean) {
        if (state) {
            binding.loadingLayout.root.visibility = View.VISIBLE
        } else {
            binding.loadingLayout.root.visibility = View.GONE
        }
    }
}