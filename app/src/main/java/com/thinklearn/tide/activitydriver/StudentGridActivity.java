package com.thinklearn.tide.activitydriver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.GridView;
import android.widget.TextView;

import com.thinklearn.tide.adapter.StudentGridAdapter;
import com.thinklearn.tide.dto.Student;

import java.util.List;

public class StudentGridActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_grid);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        String selectedGrade = getIntent().getStringExtra("selectedGrade");
        String selectedGender = getIntent().getStringExtra("selectedGender");

        ((TextView) findViewById(R.id.selectedClass)).setText(": " +selectedGrade);
        ((TextView) findViewById(R.id.selectedGender)).setText(": " +selectedGender);

        List<Student> studentInputList = getIntent().getParcelableArrayListExtra("studentInputList");
        GridView gridView = findViewById(R.id.gvStudentGrid);

        StudentGridAdapter studentGridAdapter = new StudentGridAdapter(StudentGridActivity.this,studentInputList);
        gridView.setAdapter(studentGridAdapter);
    }
}
