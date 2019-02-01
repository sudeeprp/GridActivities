package com.thinklearn.tide.activitydriver

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.thinklearn.tide.dto.Student
import com.thinklearn.tide.interactor.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class ChapterSelector : AppCompatActivity() {
    lateinit var grade: String
    lateinit var subject: String
    var selectorInterface = ChapterSelectorInterface(this)

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
        selectorInterface.grade = grade
        selectorInterface.subject = subject
        chaptersPage.addJavascriptInterface(selectorInterface, "Android")
        chaptersPage.webViewClient = WebViewClient()

        if(ClassroomContext.selectedTeacher != null) {
            chaptersPage.loadUrl("file://" + ContentInteractor().chapters_page(grade, subject))
        }
        if(ClassroomContext.selectedStudent != null) {
            val student: Student = ClassroomContext.selectedStudent!!
            val student_current_chapter = ClassroomProgressInteractor
                    .chapter_id_of_student(student, subject)
            val chapter_page = ContentInteractor().chapters_directory(student.grade, subject) + "/" +
                    student_current_chapter + "/index.html"
            chaptersPage.loadUrl("file://" + chapter_page)
        }
    }
    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.chapters_page)
        if (selectorInterface.need_to_stay && webView.canGoBack()) {
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
            ClassroomDBInteractor.set_student_activity_data(ClassroomContext.selectedStudent!!.id, activity_subject,
                    activity_chapter, activity_identifier, activity_datapoint)
        }
    }
    override fun onResume() {
        super.onResume()
        val chaptersPage = findViewById<WebView>(R.id.chapters_page)
        if(chaptersPage != null) {
            chaptersPage.evaluateJavascript("refresh_screen();", null)
        }
    }
}

class ChapterSelectorInterface(val chapterContext: ChapterSelector) {
    var chapter_shown: String = ""
    var grade: String = ""
    var subject: String = ""
    var need_to_stay = false

    @JavascriptInterface
    fun getCurrentGrade(): String {
        return ContentInteractor().get_grade_display_name(grade, chapterContext, chapterContext.packageName)
    }
    @JavascriptInterface
    fun getCurrentSubject(): String {
        return ContentInteractor().get_subject_display_name(subject, chapterContext, chapterContext.packageName)
    }
    @JavascriptInterface
    fun getChapterStatus(chapterIdent: String): String {
        var status = "pending"
        val current_chapter_id = ClassroomInteractor.get_active_chapter_id(grade, subject)
        if(current_chapter_id == chapterIdent) {
            status = "current"
        }
        return status
    }
    @JavascriptInterface
    fun setChapterActive(chapterIdent: String) {
        ClassroomDBInteractor.set_active_chapter(grade, subject, chapterIdent)
    }
    @JavascriptInterface
    fun getStudentsInSubject(): String {
        val studentsInChapters = ClassroomProgressInteractor.students_and_chapters(grade, subject)
        val studentsInChaptersJSON = JSONArray()
        for(chapter in studentsInChapters) {
            val chapterJSON = JSONObject()
            chapterJSON.put("chapter_name", chapter.key)
            val studentsJSON = JSONArray()
            for(student in chapter.value) {
                val studentJSON = JSONObject()
                studentJSON.put("id", student.id)
                //Names are too long when surname is included. Try with first names first
                studentJSON.put("name", student.firstName) // + " " + student.surname)
                var thumbnail = student.thumbnail
                if(thumbnail == null) {
                    val bitmap = BitmapFactory.decodeResource(chapterContext.resources, R.drawable.student)
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos) //bm is the bitmap object
                    val thumbByteArray = baos.toByteArray()
                    thumbnail = Base64.encodeToString(thumbByteArray, Base64.DEFAULT)
                }
                studentJSON.put("thumbnail", thumbnail)
                studentJSON.put("status", ClassroomProgressInteractor
                        .chapter_status_of_student(student, subject, chapter.key))
                studentsJSON.put(studentJSON)
            }
            chapterJSON.put("students", studentsJSON)
            studentsInChaptersJSON.put(chapterJSON)
        }
        return studentsInChaptersJSON.toString(2)
    }
    @JavascriptInterface
    fun getActivitiesStatus(): String {
        val activitiesStatusJSON = JSONObject()
        if(ClassroomContext.selectedStudent != null) {
            val activitiesStatus = ClassroomProgressInteractor.getActivitiesStatus(
                                      ClassroomContext.selectedStudent!!, subject, chapter_shown)
            for(activityStatus in activitiesStatus) {
                activitiesStatusJSON.put(activityStatus.activityID, activityStatus.status)
            }
        }
        return activitiesStatusJSON.toString(2)
    }
    @JavascriptInterface
    fun chapterEntered(chapterName: String) {
        chapter_shown = chapterName
        need_to_stay = true
    }
    @JavascriptInterface
    fun selectorEntered() {
        need_to_stay = false
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
    @JavascriptInterface
    fun subActivity(activity_identifier: String) {
        need_to_stay = true
        val chaptersPage = chapterContext.findViewById<WebView>(R.id.chapters_page)
        val subActivityUrl = ContentInteractor().activity_page(grade, subject, chapter_shown, activity_identifier)
        chaptersPage.post(Runnable {
            chaptersPage.loadUrl("file://" + subActivityUrl)
        })
    }
    @JavascriptInterface
    fun chapterSelector() {
        val chaptersPage = chapterContext.findViewById<WebView>(R.id.chapters_page)
        val chaptersUrl = ContentInteractor().chapters_page(grade, subject)
        chaptersPage.post(Runnable {
            chaptersPage.loadUrl("file://" + chaptersUrl)
        })
    }
    @JavascriptInterface
    fun diveToStudent(studentID: String) {
        val intent = Intent(chapterContext, AssessmentRecordActivity::class.java)
        intent.putExtra(StudentDetailFragment.STUDENT_ID, studentID)
        chapterContext.startActivity(intent)
    }
}
