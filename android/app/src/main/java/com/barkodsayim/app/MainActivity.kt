package com.barkodsayim.app

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

/**
 * Barkod Sayim — Native Android WebView wrapper.
 *
 * Tum web uygulamasi assets/ klasorunden yuklenir. Firebase yapilandirilmissa
 * Firestore'a internet uzerinden baglanir; degilse yerel mod calisir.
 *
 * iData lazerleri Keyboard Wedge modunda calistigi icin WebView icindeki
 * input alanlarina dogrudan tarama yapilir.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true           // localStorage / IndexedDB
            allowFileAccess = false
            allowContentAccess = false
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
        }

        // Iyi bir tarama deneyimi icin scrollbar gizle
        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = false
        webView.overScrollMode = View.OVER_SCROLL_NEVER

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean = false   // ayni WebView icinde yukle
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                android.util.Log.d("BarkodSayim", "${cm.messageLevel()} ${cm.lineNumber()}: ${cm.message()}")
                return true
            }
        }

        // Statu cubugu temasi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        }

        // Asset'lerden index.html yukle
        webView.loadUrl("file:///android_asset/index.html")
    }

    /**
     * iData tetik tusu bazi modellerde KEYCODE_F* veya KEYCODE_CAMERA gonderir.
     * Bu durumda Wedge cikartmadiysak WebView'a iletilmesi gerek.
     * Default davranisi koruyoruz; klavye-wedge zaten DOM'a dusurur.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
