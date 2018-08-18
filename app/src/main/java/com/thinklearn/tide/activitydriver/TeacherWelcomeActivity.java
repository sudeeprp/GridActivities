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

import com.thinklearn.tide.dto.AttendanceInput;
import com.thinklearn.tide.dto.Student;
import com.thinklearn.tide.dto.Teacher;
import com.thinklearn.tide.dto.TeacherWelcomeOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TeacherWelcomeActivity extends AppCompatActivity {

    private List<Student> studentInputList = new ArrayList<>();
    private ArrayList<String> gradeList;


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
            studentInput.setId("" + i);
            studentInput.setFirstName(i + "Tom");
            studentInput.setSurname(i + "Hanks");
            studentInput.setGender("Male");
            studentInput.setGrade("1");
            studentInput.setThumbnail("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");
            studentInputList.add(studentInput);
        }
        for (int i=11;i<20;i++){
            Student studentInput = new Student();
            studentInput.setId("" + i);
            studentInput.setFirstName(i + "Jack");
            studentInput.setSurname(i + "Nicholson");
            studentInput.setGender("Male");
            studentInput.setGrade("2");
            studentInputList.add(studentInput);
        }
        for (int i=11;i<20;i++){
            Student studentInput = new Student();
            studentInput.setId("" + i);
            studentInput.setFirstName(i + "Sean");
            studentInput.setSurname(i + "Penn");
            studentInput.setGender("Male");
            studentInput.setGrade("1");
            studentInputList.add(studentInput);
        }

        for (int i=21;i<30;i++){
            Student studentInput = new Student();
            studentInput.setId("" + i);
            studentInput.setFirstName(i + "Daniel Day");
            studentInput.setSurname(i + "Lewis");
            studentInput.setGender("Male");
            studentInput.setGrade("2");
            studentInputList.add(studentInput);
        }

        for (int i=31;i<40;i++){
            Student studentInput = new Student();
            studentInput.setId("" + i);
            studentInput.setFirstName(i + "Jodie");
            studentInput.setSurname(i + "Foster");
            studentInput.setGender("Female");
            studentInput.setGrade("1");
            studentInputList.add(studentInput);
        }

        for (int i=41;i<50;i++){
            Student studentInput = new Student();
            studentInput.setId("" + i);
            studentInput.setFirstName(i + "Uma");
            studentInput.setSurname(i + "Thurman");
            studentInput.setGender("Female");
            studentInput.setGrade("2");
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
                if(v == ivAttendance) {
                    output = new TeacherWelcomeOutput("Attendance");
                    Intent intent = new Intent(TeacherWelcomeActivity.this, AttendenceManagementActivity.class);
                    AttendanceInput attendance = new AttendanceInput();
                    Date weekStartDate = new Date(118, 7, 12);
                    attendance.setWeekStartDate(weekStartDate);
                    Map<String, List<String>> absentees = new HashMap<String, List<String>>();
                    List<String> studentIds = new ArrayList<String>();
                    studentIds.add("2");
                    studentIds.add("7");
                    absentees.put("2018-08-13", studentIds);
                    attendance.setAbsentees(absentees);
                    attendance.setHolidayList(new ArrayList<Date>());
                    attendance.setStudentList(studentInputList);
                    intent.putExtra("attendance", attendance);
                    startActivityForResult(intent, 3);
                } else if(v == ivStudents) {
                    Intent intent = new Intent(TeacherWelcomeActivity.this, GradeSelectionActivity.class);
                    intent.putParcelableArrayListExtra("studentInputList", (ArrayList<? extends Parcelable>) studentInputList);
                    intent.putExtra("purpose", "STUDENT_ACTIVITY");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                } else if(v == ivDashboard) {
                    Intent curriculumIntent = new Intent(TeacherWelcomeActivity.this,
                            CurriculumSelector.class);
                    curriculumIntent.putExtra("selection", output);
                    startActivityForResult(curriculumIntent, 3);
                }
            }
        };
        ivAttendance.setOnClickListener(listener);
        ivStudents.setOnClickListener(listener);
        ivDashboard.setOnClickListener(listener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RESULT_OK){
            if(requestCode==1){

            }
        }
    }
}
