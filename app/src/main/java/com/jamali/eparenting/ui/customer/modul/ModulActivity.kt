package com.jamali.eparenting.ui.customer.modul

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.data.ModulDataType
import com.jamali.eparenting.databinding.ActivityListModulBinding

class ModulActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListModulBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListModulBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackFromListMaterial.setOnClickListener {
            finish()
        }

        val modulData = intent.getParcelableExtra<ModulDataType>("modul_data")
        val type = intent.getStringExtra("type").toString()
        binding.tvTitleListMaterial.text = modulData?.title

        setPdfModulByType(type)
    }

    private fun setPdfModulByType(type: String) {
        val pdfUrl = when (type) {
            "Pranikah" -> "MODUL_PRA_NIKAH.pdf"
            "Balita" -> "MODUL_BALITA.pdf"
            "SD" -> "MODUL_SEKOLAH_DASAR.pdf"
            "SMP" -> "MODUL_SEKOLAH_MENENGAH_PERTAMA.pdf"
            "SMA" -> "MODUL_SEKOLAH_MENENGAH_ATAS.pdf"
            else -> ""
        }

        binding.pdfViewer.fromAsset(pdfUrl)
            .onLoad {
                binding.progressBar.visibility = View.GONE
            }
            .onPageError { page, _ ->
                // Sembunyikan ProgressBar jika ada kesalahan
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading page $page", Toast.LENGTH_SHORT).show()
            }
            .load()
    }
}