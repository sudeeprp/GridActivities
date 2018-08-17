package com.thinklearn.tide.activitydriver

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.thinklearn.tide.dto.Student
import com.thinklearn.tide.interactor.ClassroomInteractor
import com.thinklearn.tide.interactor.ContentInteractor

class ChapterSelector : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getSupportActionBar()?.hide();
        setContentView(R.layout.activity_chapter_selector)

        val curriculumIntent = getIntent()
        val grade = curriculumIntent.getStringExtra("SELECTED_GRADE")
        val subject = curriculumIntent.getStringExtra("SELECTED_SUBJECT")

        val chaptersPage = findViewById<WebView>(R.id.chapters_page)
        chaptersPage.settings.javaScriptEnabled = true
        chaptersPage.settings.allowFileAccessFromFileURLs = true
        chaptersPage.addJavascriptInterface(ChapterSelectorInterface(grade, subject), "Android")
        chaptersPage.webViewClient = WebViewClient()

        chaptersPage.loadUrl("file://" + ContentInteractor().chapters_path(grade, subject))
    }
}

class ChapterSelectorInterface(val grade: String, val subject: String) {
    @JavascriptInterface
    fun getChapterStatus(chapterIdent: String): String {
        println(chapterIdent)
        var status = "pending"
        val current_chapter = ClassroomInteractor.get_current_chapter(grade_subject())
        if(current_chapter == chapterIdent) {
            status = "current"
        }
        return status
    }
    @JavascriptInterface
    fun setChapterActive(chapterIdent: String) {
        ClassroomInteractor.set_active_chapter(grade_subject(), chapterIdent)
    }
    @JavascriptInterface
    fun getStudentsInChapter(chapterIdent: String): String {
        val students_in_chapter = ClassroomInteractor.get_students_in_chapter(grade, subject, chapterIdent)
        println("**giving back students")
        println(students_in_chapter.toString())
        return students_in_chapter.toString()
    }
    fun grade_subject(): String {
        return grade + "_" + subject.toLowerCase()
    }
}
