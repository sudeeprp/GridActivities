package com.thinklearn.tide.activitydriver

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.thinklearn.tide.dto.Student
import com.thinklearn.tide.interactor.ClassroomContext
import com.thinklearn.tide.interactor.ClassroomInteractor
import com.thinklearn.tide.interactor.ContentInteractor
import org.json.JSONArray
import org.json.JSONObject

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
        chaptersPage.addJavascriptInterface(ChapterSelectorInterface(this, grade, subject), "Android")
        chaptersPage.webViewClient = WebViewClient()

        chaptersPage.loadUrl("file://" + ContentInteractor().chapters_page(grade, subject))

        if(ClassroomContext.selectedTeacher == null && ClassroomContext.selectedStudent != null) {
            chaptersPage.loadUrl("file://" +
                    ContentInteractor().current_chapter_page(ClassroomContext.selectedStudent!!, subject))
        }
    }
    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.chapters_page)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

class ChapterSelectorInterface(val chapterContext: ChapterSelector, val grade: String, val subject: String) {
    var chapter_shown: String = ""

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
        val students_json = JSONArray()
        for(student in students_in_chapter) {
            val student_json = JSONObject()
            student_json.put("name", student.firstName + " " + student.surname)
            student_json.put("thumbnail", student.thumbnail)
            students_json.put(student_json)
        }
        return students_json.toString()
    }
    @JavascriptInterface
    fun getStudentIDsInChapter(chapterIdent: String): String {
        val students_in_chapter = ClassroomInteractor.get_students_in_chapter(grade, subject, chapterIdent)
        println("**giving back student IDs")
        val studentIDs_in_chapter_json = student_ids_json(students_in_chapter)
        return studentIDs_in_chapter_json
    }
    @JavascriptInterface
    fun getStudentName(id: String): String {
        val student = ClassroomInteractor.get_student(id)
        return student?.firstName + " " + student?.surname
    }
    @JavascriptInterface
    fun getStudentThumbnail(id: String): String {
        return ClassroomInteractor.get_student(id)?.thumbnail + ""
    }
    @JavascriptInterface
    fun chapterEntered(chapterName: String) {
        chapter_shown = chapterName
    }
    @JavascriptInterface
    fun startActivity(activity_identifier: String) {
        val activityIntent = Intent(chapterContext, CurriculumActivity::class.java)
        activityIntent.putExtra("SELECTED_GRADE", grade)
        activityIntent.putExtra("SELECTED_SUBJECT", subject)
        activityIntent.putExtra("SELECTED_CHAPTER", chapter_shown)
        activityIntent.putExtra("SELECTED_ACTIVITY", activity_identifier)
        chapterContext.startActivityForResult(activityIntent, 3)
    }
    fun grade_subject(): String {
        return grade + "_" + subject.toLowerCase()
    }
    fun student_ids_json(students: ArrayList<Student>): String {
        val student_ids_array = JSONArray()
        for(student in students) {
            student_ids_array.put(student.id)
        }
        return student_ids_array.toString()
    }
}
