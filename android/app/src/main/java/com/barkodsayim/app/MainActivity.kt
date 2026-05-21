package com.barkodsayim.app

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(
            result.resultCode, result.data
        )
        filePathCallback?.onReceiveValue(uris)
        filePathCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
        }

        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = false
        webView.overScrollMode = View.OVER_SCROLL_NEVER

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean = false
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                Log.d("BarkodSayim", "${cm.messageLevel()} ${cm.lineNumber()}: ${cm.message()}")
                return true
            }

            override fun onShowFileChooser(
                view: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback
                val intent = params?.createIntent() ?: run {
                    filePathCallback = null
                    return false
                }
                return try {
                    filePicker.launch(intent)
                    true
                } catch (e: Exception) {
                    filePathCallback = null
                    Toast.makeText(this@MainActivity,
                        "Dosya secici acilamadi: ${e.message}",
                        Toast.LENGTH_LONG).show()
                    false
                }
            }
        }

        webView.addJavascriptInterface(JsBridge(), "AndroidBridge")
        webView.setDownloadListener { url, _, contentDisposition, mimeType, _ ->
            if (url.startsWith("blob:") || url.startsWith("data:")) {
                val filename = URLUtil.guessFileName(url, contentDisposition, mimeType)
                val js = """
                    (function() {
                        try {
                            var xhr = new XMLHttpRequest();
                            xhr.open('GET', '$url', true);
                            xhr.responseType = 'blob';
                            xhr.onload = function() {
                                if (xhr.status >= 200 && xhr.status < 300) {
                                    var fr = new FileReader();
                                    fr.onloadend = function() {
                                        var s = (fr.result + '');
                                        var b64 = s.indexOf(',') >= 0 ? s.split(',')[1] : '';
                                        window.AndroidBridge.saveBase64(
                                            '${filename.replace("'", "\\'")}',
                                            '${(mimeType ?: "").replace("'", "\\'")}',
                                            b64
                                        );
                                    };
                                    fr.readAsDataURL(xhr.response);
                                }
                            };
                            xhr.send();
                        } catch (e) { console.error('blob fetch err', e); }
                    })();
                """.trimIndent()
                webView.evaluateJavascript(js, null)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }

        webView.loadUrl("file:///android_asset/index.html")
    }

    inner class JsBridge {
        @JavascriptInterface
        fun saveBase64(filename: String, mimeType: String, base64: String) {
            runOnUiThread {
                try {
                    val data = Base64.decode(base64, Base64.DEFAULT)
                    val safeName = filename.ifBlank { "indirilen.bin" }
                    val type = mimeType.ifBlank { "application/octet-stream" }
                    val ok = saveToDownloads(safeName, type, data)
                    Toast.makeText(this@MainActivity,
                        if (ok) "Indirildi: $safeName" else "Kayit hatasi",
                        Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity,
                        "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveToDownloads(filename: String, mimeType: String, data: ByteArray): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val cv = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)
                    ?: return false
                resolver.openOutputStream(uri)?.use { it.write(data) }
                true
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, filename)
                FileOutputStream(file).use { it.write(data) }
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                    this.data = Uri.fromFile(file)
                })
                true
            }
        } catch (e: Exception) {
            Log.e("BarkodSayim", "save error", e)
            false
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}