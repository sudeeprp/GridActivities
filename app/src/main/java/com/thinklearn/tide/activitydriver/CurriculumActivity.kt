package com.thinklearn.tide.activitydriver

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import com.thinklearn.tide.interactor.ContentInteractor
import java.io.File

val border_html_piece: HashMap<String, String> = hashMapOf(
"1_french_x" to """url('data:image/png;base64,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')""",
"1_french_y" to """url('data:image/png;base64,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')"""
)

val html_before_border_images =
"""
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<title>Learn</title>
<style>
html, body {
height: 100%;
}
body {
overflow: hidden;
}
.nomargins {
    margin-top: 0px;
    margin-left: 0px;
    margin-right: 0px;
    margin-bottom: 0px;
}
.in-margins {
    margin-top: 20px;
    margin-left: 20px;
    margin-bottom: 20px;
    margin-right: 20px;
}
.bigborder {
    background-image:""".trimIndent()

val html_border_to_iframe_src = """
    background-repeat: repeat-x, repeat-y, repeat-x, repeat-y;
    background-attachment: fixed;
    background-position: top, left, bottom, right;
}
</style>
</head>
<body class="nomargins">
<div class="bigborder" style="height:100%;width=100%;">
<iframe src="""".trimIndent()

val html_after_iframe_src = """" frameborder="0" style="overflow:hidden;height:90%;width:90%;position:absolute;top:20px;left:20px;right:20px;bottom:20px"></iframe>
</div>
</body>
</html> """.trimIndent()

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
        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE
        }
        grade = intent.getStringExtra("SELECTED_GRADE")
        subject = intent.getStringExtra("SELECTED_SUBJECT")
        chapter = intent.getStringExtra("SELECTED_CHAPTER")
        activity_identifier = intent.getStringExtra("SELECTED_ACTIVITY")

        loadCurriculumActivity(grade, subject, chapter, activity_identifier)
    }
    fun loadCurriculumActivity(grade: String, subject: String, chapter: String, activity_identifier: String) {
        val contentStartPage = ContentInteractor().activity_page(grade, subject, chapter, activity_identifier)
        if(contentStartPage.toLowerCase().endsWith("pdf")) {
            loadPDF(contentStartPage)
        } else if(contentStartPage.toLowerCase().endsWith("mp4")) {
            loadVideo(contentStartPage)
        } else {
            loadIframe(contentStartPage)
        }
    }
    fun loadVideo(contentStartPage: String) {
        setContentView(R.layout.activity_curriculum_video)
        val videoView = findViewById<VideoView>(R.id.videoView)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        val uri = Uri.parse(contentStartPage)
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
        videoView.setOnCompletionListener { endActivity("") }
    }
    fun loadPDF(contentStartPage: String) {
        try {
            val pdfIntent: Intent = Intent(Intent.ACTION_VIEW)
            pdfIntent.setDataAndType(Uri.fromFile(File(contentStartPage)), "application/pdf")
            pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(pdfIntent)
        }
        catch(e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_pdf_viewer, Toast.LENGTH_LONG).show()
        }
    }
    fun get_border_html(grade: String, subject: String): String {
        var border_html = "";
        val border_key = grade + "_" + subject
        border_html += border_html_piece[border_key + "_x"] + ",\n"
        border_html += border_html_piece[border_key + "_y"] + ",\n"
        border_html += border_html_piece[border_key + "_x"] + ",\n"
        border_html += border_html_piece[border_key + "_y"] + ";\n"
        return border_html
    }
    fun loadIframe(contentStartPage: String) {
        setContentView(R.layout.activity_curriculum_html)
        val webView = findViewById<WebView>(R.id.curriculum_page)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccessFromFileURLs = true
        //needed to enable autoplay
        webView.settings.mediaPlaybackRequiresUserGesture = false
        //not sure what the following is for... trying to avoid black pages
        webView.settings.mixedContentMode = MIXED_CONTENT_ALWAYS_ALLOW
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        webView.addJavascriptInterface(ActivityInterface(this), "Android")
        webView.webViewClient = WebViewClient()
        val baseUrl = ContentInteractor().activity_directory(grade, subject, chapter, activity_identifier)
        val pageData = html_before_border_images + get_border_html(grade, subject) + html_border_to_iframe_src +
                "file://" + contentStartPage + html_after_iframe_src
        webView.loadDataWithBaseURL("file://" + baseUrl, pageData, null, null, null)
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
