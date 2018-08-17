package com.thinklearn.tide.activitydriver

import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Window
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.thinklearn.tide.interactor.ContentInteractor

class CurriculumChapters : AppCompatActivity() {

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
            println("Directory access permission not granted")
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    4)
        } else {
            println("Permissions ok, launching page")
            loadPage()
        }
    }

    fun loadPage() {
        val contentStartPage = ContentInteractor().content_start_page
        val webView = findViewById<WebView>(R.id.curriculum_page)
        //val contentStartPage = Environment.getExternalStorageDirectory().path + "/LTry/index.html"
        webView.loadUrl("file://" + contentStartPage)

        val base_path = Environment.getExternalStorageDirectory().getPath() + "/LearningGrid/"
        MediaScannerConnection.scanFile(this, arrayOf(base_path), null, null);
    }
    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.curriculum_page)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty())
        {
            loadPage()
        }
    }
}

class ActivityInterface(val curriculum_context: CurriculumChapters) {
    @JavascriptInterface
    fun activityResult(datapoint: String) {
        println(datapoint)
        val webView = curriculum_context.findViewById<WebView>(R.id.curriculum_page)
        webView.post { webView.goBack() }
    }
}
