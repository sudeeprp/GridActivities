package com.thinklearn.tide.activitydriver;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.thinklearn.tide.dto.Student;
import com.thinklearn.tide.dto.Teacher;
import com.thinklearn.tide.dto.TeacherWelcomeOutput;

import java.util.ArrayList;
import java.util.List;


public class TeacherWelcomeActivity extends AppCompatActivity {

    private List<Student> studentInputList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_teacher_start_screen);
        TextView tvTeacherName = findViewById(R.id.tvTeacherName);

        for (int i=1;i<10;i++){
            Student studentInput = new Student();
            studentInput.setId(i);
            studentInput.setFirstName(i + "Elvin D'Souza");
            studentInput.setSurname(i + "RajKumar");
            studentInput.setGender(i + "Male");
            studentInput.setGrade(i + "5");
            studentInputList.add(studentInput);
        }

        if(getIntent().hasExtra("TEACHER_IDENTIFICATION")) {
            Teacher input = getIntent().getParcelableExtra("TEACHER_IDENTIFICATION");
            tvTeacherName.setText(input.getTeacherName());
        }
        final ImageView ivAttendance =  findViewById(R.id.ivAttendanceImage);
        final ImageView ivStudents =  findViewById(R.id.ivStudentImage);
        final ImageView ivDashboard =  findViewById(R.id.ivDashboard);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                TeacherWelcomeOutput output = null;
                if(v == ivAttendance)
                    output = new TeacherWelcomeOutput("Attendance");
                else if(v == ivStudents) {
                    output = new TeacherWelcomeOutput("Attendance");
                    Intent intent = new Intent(TeacherWelcomeActivity.this, StudentListActivity.class);
                    intent.putParcelableArrayListExtra("studentInputList", (ArrayList<? extends Parcelable>) studentInputList);
                    startActivityForResult(intent, 2);
                } else if(v == ivDashboard) {
                    Intent curriculumIntent = new Intent(TeacherWelcomeActivity.this, Curriculum.class);
                    curriculumIntent.putExtra("selection", output);
                    startActivityForResult(curriculumIntent, 3);
                }
            }
        };
        ivAttendance.setOnClickListener(listener);
        ivStudents.setOnClickListener(listener);
        ivDashboard.setOnClickListener(listener);
    }
}
