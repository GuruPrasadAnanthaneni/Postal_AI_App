package com.project.postalapp.ui


import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.project.postalapp.databinding.ActivityWebBinding

class WebActivity : AppCompatActivity() {
    private val bind by lazy { ActivityWebBinding.inflate(layoutInflater) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)
        val url = intent.getStringExtra("url")!!


        bind.webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        bind.webView.settings.domStorageEnabled = true

        bind.webView.settings.loadWithOverviewMode = true
        bind.webView.settings.useWideViewPort = true
        bind.webView.settings.builtInZoomControls = true
        bind.webView.settings.displayZoomControls = false

        bind.webView.settings.javaScriptEnabled = true
        bind.webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        bind.webView.settings.userAgentString =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36"

        bind.webView.webViewClient = object : WebViewClient() {
            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?,
            ) {
                handler?.proceed()
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url ?: "")
                return true
            }
        }


        bind.webView.loadUrl(url)

    }


}
