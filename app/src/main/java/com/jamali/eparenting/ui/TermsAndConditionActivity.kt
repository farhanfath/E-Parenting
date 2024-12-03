package com.jamali.eparenting.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.R
import com.jamali.eparenting.databinding.ActivityTermsAndConditionBinding

class TermsAndConditionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsAndConditionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsAndConditionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTermsAndConditions()

        binding.ivBack.setOnClickListener {
            finish()
        }

    }

    private fun setupTermsAndConditions() {

        val webView = binding.webView
        // Styling CSS untuk membuat tampilan lebih proporsional
        val htmlContent = """
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
                    line-height: 1.6;
                    color: #333;
                    max-width: 800px;
                    margin: 0 auto;
                    padding: 16px;
                }
                h1 {
                    color: #2c3e50;
                    border-bottom: 2px solid #74C5AA;
                    padding-bottom: 10px;
                    text-align: center;
                }
                h2 {
                    color: #2c3e50;
                    border-bottom: 2px solid #74C5AA;
                    padding-bottom: 10px;
                }
                ul {
                    padding-left: 30px;
                }
                li {
                    margin-bottom: 10px;
                }
                .disclaimer {
                    color: #e74c3c;
                    font-weight: bold;
                    text-align: center;
                    margin-top: 20px;
                }
                .highlight {
                    font-weight: bold;
                    color: #e74c3c;
                }
            </style>
        </head>
        <body>
            ${getString(R.string.terms_conditions_full_new).replace("<![CDATA[", "").replace("]]>", "")}
        </body>
        </html>
        """

        webView.settings.apply {
            javaScriptEnabled = false
            domStorageEnabled = true
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
}