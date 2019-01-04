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
import com.thinklearn.tide.interactor.ClassroomContext
import com.thinklearn.tide.interactor.ClassroomDBInteractor
import com.thinklearn.tide.interactor.ClassroomInteractor
import com.thinklearn.tide.interactor.ContentInteractor
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
            chaptersPage.loadUrl("file://" +
                    ClassroomInteractor.current_chapter_page(ClassroomContext.selectedStudent!!, subject))
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
            ClassroomDBInteractor.set_student_activity_status(ClassroomContext.selectedStudent!!.id, activity_subject,
                    activity_chapter, activity_identifier, activity_datapoint)
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
        val resId = chapterContext.resources.getIdentifier("grade" + grade, "string", chapterContext.packageName)
        return chapterContext.getString(resId)
    }
    @JavascriptInterface
    fun getCurrentSubject(): String {
        val resId = chapterContext.resources.getIdentifier(subject, "string", chapterContext.packageName)
        return chapterContext.getString(resId)
    }
    @JavascriptInterface
    fun getChapterStatus(chapterIdent: String): String {
        //TODO: Get status based on completion from ClassroomInteractor.
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
        val studentsInChapters = ClassroomInteractor.students_and_chapters(grade, subject)
        val studentsInChaptersJSON = JSONArray()
        for(chapter in studentsInChapters) {
            val chapterJSON = JSONObject()
            chapterJSON.put("chapter_name", chapter.key)
            val studentsJSON = JSONArray()
            for(student in chapter.value) {
                val studentJSON = JSONObject()
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
                studentsJSON.put(studentJSON)
            }
            chapterJSON.put("students", studentsJSON)
            studentsInChaptersJSON.put(chapterJSON)
        }
        return studentsInChaptersJSON.toString(2)
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
}
