package com.jamali.eparenting.ui.rules

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.R
import com.jamali.eparenting.databinding.ActivityCommunityRulesBinding

class CompleteRulesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityRulesBinding
    private lateinit var contentRules : String

    companion object {
        const val EXTRA_RULES_TYPE = "RULES_TYPE"
        const val RULES_TYPE_COMMUNITY = "COMMUNITY"
        const val RULES_TYPE_PRIVACY = "PRIVACY"
        const val RULES_TYPE_TERMSOFSERVICES = "TERMSOFSERVICES"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityRulesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rulesType = intent.getStringExtra(EXTRA_RULES_TYPE) ?: RULES_TYPE_COMMUNITY
        setupRules(rulesType)

        binding.ivBack.setOnClickListener {
            finish()
        }

    }

    private fun setupRules(rulesType: String) {
        contentRules = when (rulesType) {
            RULES_TYPE_COMMUNITY -> getString(R.string.community_rules_full)
            RULES_TYPE_PRIVACY -> getString(R.string.privacy_policy_full)
            RULES_TYPE_TERMSOFSERVICES -> getString(R.string.terms_of_service_full)
            else -> getString(R.string.community_rules_full)
        }.replace("<![CDATA[", "").replace("]]>", "")

        // Pilih judul berdasarkan tipe
        binding.tvTitleRules.text = when (rulesType) {
            RULES_TYPE_COMMUNITY -> "Aturan Komunitas"
            RULES_TYPE_PRIVACY -> "Kebijakan Privasi"
            RULES_TYPE_TERMSOFSERVICES -> "Ketentuan Layanan"
            else -> "Aturan Komunitas"
        }

        setupCommunityRules()
    }

    private fun setupCommunityRules() {

        val webView = binding.webView
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
            $contentRules
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