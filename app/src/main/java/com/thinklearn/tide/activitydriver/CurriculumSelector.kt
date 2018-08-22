package com.thinklearn.tide.activitydriver

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import com.thinklearn.tide.dto.Student

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

        val grade1button = findViewById<Button>(R.id.grade1button)
        val grade2button = findViewById<Button>(R.id.grade2button)

        grade1button.setOnClickListener {
            showSubjects("1")
        }
        grade2button.setOnClickListener {
            showSubjects("2")
        }
    }

    fun showSubjects(grade: String) {
        setContentView(R.layout.activity_subject_selector)
        val french_button = findViewById<Button>(R.id.french_button)
        val math_button = findViewById<Button>(R.id.math_button)

        //TODO: <<context is a teacher or student... this will decide rest of the curriculum behaior (whether to activityResult or not...)
        val chapterSelectorIntent = Intent(this, ChapterSelector::class.java)
        chapterSelectorIntent.putExtra("SELECTED_GRADE", grade)

        french_button.setOnClickListener {
            chapterSelectorIntent.putExtra("SELECTED_SUBJECT", "french")
            startActivityForResult(chapterSelectorIntent, 3)
        }
        math_button.setOnClickListener {
            chapterSelectorIntent.putExtra("SELECTED_SUBJECT", "Mathematics")
            startActivityForResult(chapterSelectorIntent, 3)
        }
    }
}
