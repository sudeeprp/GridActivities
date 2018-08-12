package com.thinklearn.tide.activitydriver

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Window
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.thinklearn.tide.interactor.ContentInteractor

class Curriculum : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getSupportActionBar()?.hide();
        setContentView(R.layout.activity_curriculum)

        val webView = findViewById<WebView>(R.id.curriculum_page)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.addJavascriptInterface(ActivityInterface(this), "Android")
        webView.webViewClient = WebViewClient()

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            println("Read permission not granted")
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    4)
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            println("Write permission not granted")
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    4)
        }
        val contentStartPage = ContentInteractor().content_start_page
        //val contentStartPage = Environment.getExternalStorageDirectory().path + "/LTry/index.html"
        webView.loadUrl("file://" + contentStartPage)
    }

    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.curriculum_page)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

class ActivityInterface(val curriculum_context: Curriculum) {
    @JavascriptInterface
    fun activityResult(datapoint: String) {
        println(datapoint)
        val webView = curriculum_context.findViewById<WebView>(R.id.curriculum_page)
        webView.post { webView.goBack() }
    }
}
