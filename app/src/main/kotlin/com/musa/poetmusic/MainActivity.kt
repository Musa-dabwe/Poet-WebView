package com.musa.poetmusic

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.musa.poetmusic.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webView = binding.webview
        val settings = webView.settings

        // Enable essential WebView features
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.domStorageEnabled = true
        settings.setMediaPlaybackRequiresUserGesture(false) // ✅ Allow programmatic playback

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                // Clean up previous callback if any
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback

                val intent = fileChooserParams.createIntent().apply {
                    if (fileChooserParams.mode and FileChooserParams.MODE_OPEN_MULTIPLE != 0) {
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }
                }

                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
                } catch (e: Exception) {
                    this@MainActivity.filePathCallback = null
                    return false
                }
                return true
            }
        }

        // ✅ Load URL without trailing space
        webView.loadUrl("https://poetmusic.netlify.app/")
    }

    override fun onResume() {
        super.onResume()
        binding.webview.onResume()
        binding.webview.resumeTimers() // Keeps JS timers (e.g., playlist logic) running
    }

    override fun onPause() {
        super.onPause()
        binding.webview.onPause()
        // ❌ Do NOT call pauseTimers() — it breaks auto-advance between tracks
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            val callback = filePathCallback ?: return

            var results: Array<Uri>? = null

            if (resultCode == Activity.RESULT_OK && data != null) {
                // Single file
                if (data.data != null) {
                    results = arrayOf(data.data!!)
                } else {
                    // Multiple files via ClipData
                    data.clipData?.let { clipData ->
                        val uris = mutableListOf<Uri>()
                        for (i in 0 until clipData.itemCount) {
                            clipData.getItemAt(i).uri?.let { uri ->
                                uris.add(uri)
                            }
                        }
                        if (uris.isNotEmpty()) {
                            results = uris.toTypedArray()
                        }
                    }
                }
            }

            callback.onReceiveValue(results)
            filePathCallback = null
        }
    }
}