package com.thinklearn.tide.activitydriver

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    lateinit var grade: String
    lateinit var subject: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getSupportActionBar()?.hide();
        setContentView(R.layout.activity_chapter_selector)

        val curriculumIntent = getIntent()
        grade = curriculumIntent.getStringExtra("SELECTED_GRADE")
        subject = curriculumIntent.getStringExtra("SELECTED_SUBJECT")

        val chaptersPage = findViewById<WebView>(R.id.chapters_page)
        chaptersPage.settings.javaScriptEnabled = true
        chaptersPage.settings.allowFileAccessFromFileURLs = true
        chaptersPage.addJavascriptInterface(ChapterSelectorInterface(this, grade, subject), "Android")
        chaptersPage.webViewClient = WebViewClient()

        chaptersPage.loadUrl("file://" + ContentInteractor().chapters_page(grade, subject))

        if(ClassroomContext.selectedTeacher == null && ClassroomContext.selectedStudent != null) {
            chaptersPage.loadUrl("file://" +
                    ClassroomInteractor.current_chapter_page(ClassroomContext.selectedStudent!!, subject))
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data == null) {
            Log.e("ChapterSelector", "ActivityResult with null intent")
            return
        }
        if(ClassroomContext.selectedStudent != null) {
            val activity_subject = data.getStringExtra("SELECTED_SUBJECT")
            val activity_chapter = data.getStringExtra("SELECTED_CHAPTER")
            val activity_identifier = data.getStringExtra("SELECTED_ACTIVITY")
            val activity_datapoint = data.getStringExtra("DATAPOINT")
            ClassroomInteractor.set_student_activity_status(ClassroomContext.selectedStudent!!.id, activity_subject,
                    activity_chapter, activity_identifier, activity_datapoint)
        }
    }
    //fun refresh_screen() {
    //    val webView = findViewById<WebView>(R.id.chapters_page)
    //    webView.post() {
    //        webView.evaluateJavascript("refresh_screen();") { println("refresh_screen() done!") }
    //    }
    //}
}

class ChapterSelectorInterface(val chapterContext: ChapterSelector, val grade: String, val subject: String) {
    var chapter_shown: String = ""

    @JavascriptInterface
    fun getChapterStatus(chapterIdent: String): String {
        //TODO: Get status based on completion from ClassroomInteractor. Pack all students together and send, like getStudentsInSubject
        var status = "pending"
        val current_chapter = ClassroomInteractor.get_active_chapter(grade, subject)
        if(current_chapter == chapterIdent) {
            status = "current"
        }
        return status
    }
    @JavascriptInterface
    fun setChapterActive(chapterIdent: String) {
        ClassroomInteractor.set_active_chapter(grade, subject, chapterIdent)
        //TODO: finally after specific events (student-id-based) are implemented, we can trigger refresh here
        //chapterContext.refresh_screen()
    }
    @JavascriptInterface
    fun getStudentsInSubject(): String {
        val studentsInChapters = ClassroomInteractor.students_and_chapters(grade, subject)
        val studentsInChaptersJSON = JSONArray()
        for(chapter in studentsInChapters) {
            val chapterJSON = JSONObject()
            chapterJSON.put("chapter_name", chapter.key)
            val studentsJSON = JSONArray()
            for(student in chapter.value) {
                val studentJSON = JSONObject()
                studentJSON.put("name", student.firstName + " " + student.surname)
                studentJSON.put("thumbnail", student.thumbnail)
                studentsJSON.put(studentJSON)
            }
            chapterJSON.put("students", studentsJSON)
            studentsInChaptersJSON.put(chapterJSON)
        }
        return studentsInChaptersJSON.toString(2)
    }
    //@JavascriptInterface
    //fun getStudentIDsInChapter(chapterIdent: String): String {
    //    val students_in_chapter = ClassroomInteractor.get_students_in_chapter(grade, subject, chapterIdent)
    //    println("**giving back student IDs")
    //    val studentIDs_in_chapter_json = student_ids_json(students_in_chapter)
    //    return studentIDs_in_chapter_json
    //}
    //@JavascriptInterface
    //fun getStudentName(id: String): String {
    //    val student = ClassroomInteractor.get_student(id)
    //    return student?.firstName + " " + student?.surname
    //}
    //@JavascriptInterface
    //fun getStudentThumbnail(id: String): String {
    //    return ClassroomInteractor.get_student(id)?.thumbnail + ""
    //}
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
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        chapterContext.startActivityForResult(activityIntent, 3)
    }
    //fun student_ids_json(students: ArrayList<Student>): String {
    //    val student_ids_array = JSONArray()
    //    for(student in students) {
    //        student_ids_array.put(student.id)
    //    }
    //    return student_ids_array.toString()
    //}
}
