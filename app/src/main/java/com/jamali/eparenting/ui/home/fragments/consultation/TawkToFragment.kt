package com.jamali.eparenting.ui.home.fragments.consultation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jamali.eparenting.BuildConfig
import com.jamali.eparenting.databinding.FragmentTawkToBinding

class TawkToFragment : Fragment() {

    private var _binding: FragmentTawkToBinding? = null
    private val binding get() = _binding!!

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

        tawkToWebView.loadUrl(BuildConfig.TAWKTO_URL)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}