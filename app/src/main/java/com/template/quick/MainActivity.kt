package com.template.quick

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Adjust window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val webView = findViewById<WebView>(R.id.webview)
        webView.settings.javaScriptEnabled = true
        
        // Setup WebViewClient to handle errors while browsing
        webView.webViewClient = object : WebViewClient() {
            // Modern version of onReceivedError
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                // Only handle errors for the main frame (the actual page load)
                if (request?.isForMainFrame == true) {
                    val errorCode = error?.errorCode
                    // Check if the error is network-related
                    if (errorCode == WebViewClient.ERROR_HOST_LOOKUP || 
                        errorCode == WebViewClient.ERROR_CONNECT || 
                        errorCode == WebViewClient.ERROR_TIMEOUT) {
                        if (!isNetworkAvailable()) {
                            view?.loadUrl("file:///android_asset/offline.html")
                        }
                    }
                }
            }
        }

        // Initial load check
        if (isNetworkAvailable()) {
            webView.loadUrl("https://duckduckgo.com")
        } else {
            webView.loadUrl("file:///android_asset/offline.html")
        }
    }

    // Helper function to check if internet is available
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
