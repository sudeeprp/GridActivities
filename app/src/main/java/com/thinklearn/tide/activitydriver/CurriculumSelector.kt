package com.thinklearn.tide.activitydriver

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.*
import com.thinklearn.tide.dto.Student
import com.thinklearn.tide.interactor.ContentInteractor
import android.graphics.drawable.LayerDrawable
import com.thinklearn.tide.interactor.ClassroomProgressInteractor


class CurriculumSelector : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getSupportActionBar()?.hide();

        if (intent.hasExtra("selectedStudent")) {
            val selectedStudent: Student = intent.getParcelableExtra("selectedStudent")
            showSubjects(selectedStudent.grade)
        } else {
            showGrades()
        }
    }
    fun make_drawableWithStatus(backgroundPath: String, defaultResource: Int, statusResource: Int): LayerDrawable {
        var backgroundDrawable: Drawable? = null
        var statusDrawable: Drawable? = null
        val bk_bitmap = BitmapFactory.decodeFile(backgroundPath)
        if(bk_bitmap != null) {
            backgroundDrawable = BitmapDrawable(resources, bk_bitmap)
        } else {
            backgroundDrawable = resources.getDrawable(defaultResource, null)
        }
        lateinit var finalDrawable: LayerDrawable
        if(statusResource != 0) {
            statusDrawable = resources.getDrawable(statusResource, null)
            finalDrawable = LayerDrawable(arrayOf(backgroundDrawable, statusDrawable))
        } else {
            finalDrawable = LayerDrawable(arrayOf(backgroundDrawable))
        }
        return finalDrawable
    }
    fun tabledButton(background: LayerDrawable, displayName: String, statusSize: Int): Button {
        val button = Button(this)

        button.background = background
        button.text = displayName
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32.toFloat())
        button.transformationMethod = null
        button.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.toFloat())

        button.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val rightPad = 40
            val bottomPad = 8
            //Put the status icon on bottom-right
            if(background.numberOfLayers > 1) {
                background.setLayerInset(1,
                        v.width - statusSize - rightPad, v.height - statusSize - bottomPad,
                        rightPad, bottomPad)
            }
        }
        return button
    }
    fun gradeStatusResource(grade: String): Int {
        if(ClassroomProgressInteractor.grade_has_assessment_pending(grade)) {
            return R.drawable.chapter_assessment_ready2
        } else {
            return 0
        }
    }
    fun subjectStatusResource(grade: String, subjectID: String): Int {
        if(ClassroomProgressInteractor.subject_has_assessment_pending(grade, subjectID)) {
            return R.drawable.chapter_assessment_ready2
        } else {
            return 0
        }
    }
    fun showGrades() {
        setContentView(R.layout.activity_curriculum_selector)

        val curriculumTable = findViewById<LinearLayout>(R.id.curriculum_table)
        val grades = ContentInteractor().get_grades()
        grades.forEach {
            val gradeDisplayName = ContentInteractor().get_grade_display_name(it, this, packageName)
            val gradeButton = tabledButton(
                    make_drawableWithStatus(
                            ContentInteractor().get_grade_background_path(it),
                            resources.getIdentifier("g" + it, "drawable", packageName),
                            gradeStatusResource(it)
                    ),
                    gradeDisplayName,
                    48
            )
            gradeButton.tag = it
            gradeButton.setOnClickListener { showSubjects(it.tag.toString()) }
            curriculumTable.addView(gradeButton)
        }
    }
    fun showSubjects(grade: String) {
        setContentView(R.layout.activity_subject_selector)

        val subjectTable = findViewById<LinearLayout>(R.id.subject_table)
        val subjects = ContentInteractor().get_subjects(grade)
        subjects.forEachIndexed { index, subject ->
            val subjectDisplayName = ContentInteractor().get_subject_display_name(subject, this, packageName)
            val subjectButton = tabledButton(
                    make_drawableWithStatus(
                            ContentInteractor().get_subject_background_path(grade, subject),
                            resources.getIdentifier("g" + grade + "_" + subject, "drawable", packageName),
                            subjectStatusResource(grade, subject)
                    ),
                    subjectDisplayName,
                    48
            )
            subjectButton.gravity = Gravity.BOTTOM or Gravity.CENTER
            subjectButton.setPadding(20, index * 40, 20, 20)
            subjectButton.tag = subject
            subjectButton.setOnClickListener {
                val chapterSelectorIntent = Intent(this, ChapterSelector::class.java)
                chapterSelectorIntent.putExtra("SELECTED_GRADE", grade)
                chapterSelectorIntent.putExtra("SELECTED_SUBJECT", it.tag.toString())
                startActivity(chapterSelectorIntent)
            }
            subjectTable.addView(subjectButton)
        }
    }
}
