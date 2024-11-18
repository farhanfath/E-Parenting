package com.jamali.eparenting.ui.customer.fragments.consultation

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.jamali.eparenting.BuildConfig
import com.jamali.eparenting.databinding.FragmentTawkToBinding

class TawkToFragment : Fragment() {

    private var _binding: FragmentTawkToBinding? = null
    private val binding get() = _binding!!

    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val resultData = result.data?.data
                fileChooserCallback?.onReceiveValue(resultData?.let { arrayOf(it) })
            } else {
                fileChooserCallback?.onReceiveValue(null)
            }
            fileChooserCallback = null
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTawkToBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tawkToWebView = binding.tawkWebView
        val webSettings = tawkToWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true
        webSettings.domStorageEnabled = true

        tawkToWebView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                fileChooserCallback = filePathCallback
                val intent = fileChooserParams.createIntent()
                try {
                    fileChooserLauncher.launch(intent)
                } catch (e: Exception) {
                    fileChooserCallback = null
                    return false
                }
                return true
            }
        }

        // Load the tawk.to URL
        tawkToWebView.loadUrl(BuildConfig.TAWKTO_URL)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}