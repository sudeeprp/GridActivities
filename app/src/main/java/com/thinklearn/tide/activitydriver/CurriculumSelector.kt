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
    fun tabledButton( backgroundPath: String, displayName: String): Button? {
        var button: Button? = null
        val bk_bitmap = BitmapFactory.decodeFile(backgroundPath)
        if(bk_bitmap != null) {
            val background = BitmapDrawable(resources, bk_bitmap)
            button = tabledButton(background, displayName)
        }
        return button
    }
    fun tabledButton(background: Drawable, displayName: String): Button {
        val button = Button(this)
        button.background = background
        button.text = displayName
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32.toFloat())
        button.transformationMethod = null
        button.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.toFloat())
        return button
    }
    fun showGrades() {
        setContentView(R.layout.activity_curriculum_selector)

        val curriculumTable = findViewById<LinearLayout>(R.id.curriculum_table)
        val grades = ContentInteractor().get_grades()
        grades.forEach {
            var gradeDisplayName = ContentInteractor().get_grade_display_name(it)
            if(gradeDisplayName.isEmpty()) {
                gradeDisplayName = resources.getString(resources.getIdentifier("grade" + it, "string", packageName))
            }
            var gradeButton = tabledButton(ContentInteractor().get_grade_background_path(it),gradeDisplayName)
            if(gradeButton == null) {
                val defaultDrawableID = resources.getIdentifier("g" + it, "drawable", packageName)
                gradeButton = tabledButton(resources.getDrawable(defaultDrawableID, null), gradeDisplayName)
            }
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
            var subjectDisplayName = ContentInteractor().get_subject_display_name(subject)
            if(subjectDisplayName.isEmpty()) {
                subjectDisplayName = resources.getString(resources.getIdentifier(subject, "string", packageName))
            }
            var subjectButton =
                    tabledButton(ContentInteractor().get_subject_background_path(grade, subject), subjectDisplayName)
            if(subjectButton == null) {
                val defaultDrawableID = resources.getIdentifier("g" + grade + "_" + subject, "drawable", packageName)
                subjectButton = tabledButton(resources.getDrawable(defaultDrawableID, null), subjectDisplayName)
            }
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
