package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.thinklearn.tide.adapter.StudentGridAdapter;
import com.thinklearn.tide.dto.Student;
import com.thinklearn.tide.interactor.ClassroomContext;

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

        final List<Student> studentInputList = getIntent().getParcelableArrayListExtra("studentInputList");
        GridView gridView = findViewById(R.id.gvStudentGrid);

        StudentGridAdapter studentGridAdapter = new StudentGridAdapter(StudentGridActivity.this,studentInputList);
        gridView.setAdapter(studentGridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //TODO: Do this only on login button (the other flow is anyway in StudentListActivity, not here
                ClassroomContext.selectedStudent = studentInputList.get(position);
                ClassroomContext.selectedTeacher = null;
                Intent curriculumIntent =
                        new Intent(StudentGridActivity.this, CurriculumSelector.class);
                curriculumIntent.putExtra("selectedStudent", ClassroomContext.selectedStudent);
                startActivityForResult(curriculumIntent, 3);
            }
        });
    }
}
