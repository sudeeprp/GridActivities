package com.thinklearn.tide.activitydriver

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.thinklearn.tide.interactor.ClassroomInteractor
import com.thinklearn.tide.interactor.ContentInteractor

class CurriculumActivity : AppCompatActivity() {
    lateinit var grade: String
    lateinit var subject: String
    lateinit var chapter: String
    lateinit var activity_identifier: String

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

        grade = intent.getStringExtra("SELECTED_GRADE")
        subject = intent.getStringExtra("SELECTED_SUBJECT")
        chapter = intent.getStringExtra("SELECTED_CHAPTER")
        activity_identifier = intent.getStringExtra("SELECTED_ACTIVITY")

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
            endActivity("")
        }
    }
    fun endActivity(datapoint: String) {
        val studentActivityResult = Intent()
        studentActivityResult.putExtra("SELECTED_GRADE", grade)
        studentActivityResult.putExtra("SELECTED_SUBJECT", subject)
        studentActivityResult.putExtra("SELECTED_CHAPTER", chapter)
        studentActivityResult.putExtra("SELECTED_ACTIVITY", activity_identifier)
        studentActivityResult.putExtra("DATAPOINT", datapoint)
        setResult(Activity.RESULT_OK, studentActivityResult)
        finish()
    }
}

class ActivityInterface(val curriculum_context: CurriculumActivity) {
    @JavascriptInterface
    fun activityResult(datapoint: String) {
        Toast.makeText(curriculum_context, datapoint, Toast.LENGTH_LONG).show()
        curriculum_context.endActivity(datapoint)
    }
}
