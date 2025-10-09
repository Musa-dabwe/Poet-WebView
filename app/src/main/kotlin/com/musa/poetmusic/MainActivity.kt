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
    private val fileChooserRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.allowFileAccess = true
        binding.webview.settings.domStorageEnabled = true
        binding.webview.webViewClient = WebViewClient()
        binding.webview.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                val intent = fileChooserParams.createIntent()
                if (fileChooserParams.mode == FileChooserParams.MODE_OPEN_MULTIPLE) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                try {
                    startActivityForResult(intent, fileChooserRequestCode)
                } catch (e: Exception) {
                    return false
                }
                return true
            }
        }
        binding.webview.loadUrl("https://poetmusic.netlify.app/")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == fileChooserRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    filePathCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
                    filePathCallback = null
                }
            } else {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = null
            }
        }
    }
}