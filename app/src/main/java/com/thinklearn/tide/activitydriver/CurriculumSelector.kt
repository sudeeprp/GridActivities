package com.thinklearn.tide.activitydriver

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button

class CurriculumSelector : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getSupportActionBar()?.hide();
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

        val chapterSelectorIntent = Intent(this, ChapterSelector::class.java)
        chapterSelectorIntent.putExtra("SELECTED_GRADE", grade)

        french_button.setOnClickListener {
            chapterSelectorIntent.putExtra("SELECTED_SUBJECT", "French")
            startActivityForResult(chapterSelectorIntent, 3)
        }
        math_button.setOnClickListener {
            chapterSelectorIntent.putExtra("SELECTED_SUBJECT", "Mathematics")
            startActivityForResult(chapterSelectorIntent, 3)
        }
    }
}
