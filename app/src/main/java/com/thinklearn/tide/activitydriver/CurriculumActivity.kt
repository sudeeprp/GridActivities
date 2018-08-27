package com.thinklearn.tide.activitydriver

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.thinklearn.tide.interactor.ContentInteractor

class CurriculumActivity : AppCompatActivity() {

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

        val grade = intent.getStringExtra("SELECTED_GRADE")
        val subject = intent.getStringExtra("SELECTED_SUBJECT")
        val chapter = intent.getStringExtra("SELECTED_CHAPTER")
        val activity_identifier = intent.getStringExtra("SELECTED_ACTIVITY")

        loadPage(grade, subject, chapter, activity_identifier)
    }

    fun loadPage(grade: String, subject: String, chapter: String, activity_identifier: String) {
        val webView = findViewById<WebView>(R.id.curriculum_page)
        val contentStartPage = ContentInteractor().activity_page(grade, subject, chapter, activity_identifier)
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

class ActivityInterface(val curriculum_context: CurriculumActivity) {
    @JavascriptInterface
    fun activityResult(datapoint: String) {
        println(datapoint)
        val webView = curriculum_context.findViewById<WebView>(R.id.curriculum_page)
        webView.post { webView.goBack() }
    }
}
