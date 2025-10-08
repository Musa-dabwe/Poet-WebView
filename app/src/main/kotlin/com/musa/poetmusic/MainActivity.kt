package com.musa.poetmusic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.musa.poetmusic.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Unzip website content from assets to internal storage
        unzipFromAssets("index.zip")

        // Load the local index.html file
        val indexFile = File(filesDir, "index.html")
        if (indexFile.exists()) {
            binding.webview.settings.javaScriptEnabled = true
            binding.webview.settings.allowFileAccess = true
            binding.webview.loadUrl("file://${indexFile.absolutePath}")
        }
    }

    private fun unzipFromAssets(zipFileName: String) {
        val outputDir = filesDir
        // A simple way to check if we've already unzipped is to check for a key file.
        val checkFile = File(outputDir, "index.html")
        if (checkFile.exists()) {
            // Content is already unzipped.
            return
        }

        try {
            assets.open(zipFileName).use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    var zipEntry = zipInputStream.nextEntry
                    while (zipEntry != null) {
                        val newFile = File(outputDir, zipEntry.name)
                        if (zipEntry.isDirectory) {
                            newFile.mkdirs()
                        } else {
                            File(newFile.parent).mkdirs()
                            FileOutputStream(newFile).use { fileOutputStream ->
                                zipInputStream.copyTo(fileOutputStream)
                            }
                        }
                        zipInputStream.closeEntry()
                        zipEntry = zipInputStream.nextEntry
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}