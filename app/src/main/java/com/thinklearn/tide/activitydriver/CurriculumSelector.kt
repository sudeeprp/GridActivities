package com.thinklearn.tide.activitydriver

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
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
    fun showGrades() {
        setContentView(R.layout.activity_curriculum_selector)

        val curriculumTable = findViewById<LinearLayout>(R.id.curriculum_table)
        val grades = ContentInteractor().get_grades()
        grades.forEach {
            val gradeButton = Button(this)
            val bk_bitmap = BitmapFactory.decodeFile(ContentInteractor().get_grade_background_path(it))
            val background = BitmapDrawable(resources, bk_bitmap)
            gradeButton.background = background
            gradeButton.text = resources.getString(resources.getIdentifier("grade" + it, "string", packageName))
            gradeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32.toFloat())
            gradeButton.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.toFloat())
            curriculumTable.addView(gradeButton)
        }
    }

    fun showSubjects(grade: String) {
        setContentView(R.layout.activity_subject_selector)
        val french_button = findViewById<Button>(R.id.french_button)
        val math_button = findViewById<Button>(R.id.math_button)

        val chapterSelectorIntent = Intent(this, ChapterSelector::class.java)
        chapterSelectorIntent.putExtra("SELECTED_GRADE", grade)
        if(grade == "1") {
            french_button.background = resources.getDrawable(R.drawable.g1_french_background, null)
        } else if(grade == "2") {
            french_button.background = resources.getDrawable(R.drawable.g2_french_background, null)
        }
        french_button.setOnClickListener {
            chapterSelectorIntent.putExtra("SELECTED_SUBJECT", "french")
            startActivityForResult(chapterSelectorIntent, 3)
        }

        if(grade == "1") {
            math_button.background = resources.getDrawable(R.drawable.g1_math_background, null)
        } else if(grade == "2") {
            math_button.background = resources.getDrawable(R.drawable.g2_math_background, null)
        }
        math_button.setOnClickListener {
            chapterSelectorIntent.putExtra("SELECTED_SUBJECT", "math")
            startActivityForResult(chapterSelectorIntent, 3)
        }
    }
}
