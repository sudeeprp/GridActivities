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
import com.thinklearn.tide.interactor.ClassroomInteractor;
import com.thinklearn.tide.interactor.ContentInteractor;

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

        if(getIntent().hasExtra("TEACHER_IDENTIFICATION")) {
            Teacher input = getIntent().getParcelableExtra("TEACHER_IDENTIFICATION");
            tvTeacherName.setText(input.getTeacherName());
        }
        studentInputList = ClassroomInteractor.students;
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
                    //AttendanceInput attendance = new AttendanceInput();
                    //Date weekStartDate = new Date(118, 7, 12);
                    //attendance.setWeekStartDate(weekStartDate);
                    AttendanceInput attendance = ClassroomInteractor.get_current_week_attendance();
                    //Map<String, List<String>> absentees = new HashMap<String, List<String>>();
                    //List<String> studentIds = new ArrayList<String>();
                    //studentIds.add("2");
                    //studentIds.add("7");
                    //absentees.put("2018-08-13", studentIds);
                    //attendance.setAbsentees(absentees);
                    //attendance.setHolidayList(new ArrayList<Date>());
                    //attendance.setStudentList(studentInputList);
                    intent.putExtra("attendance", attendance);
                    startActivityForResult(intent, 3);
                } else if(v == ivStudents) {
                    Intent intent = new Intent(TeacherWelcomeActivity.this, GradeSelectionActivity.class);
                    intent.putParcelableArrayListExtra("studentInputList", (ArrayList<? extends Parcelable>) studentInputList);
                    intent.putExtra("purpose", "PROFILE_EDIT");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                } else if(v == ivDashboard) {
                    Intent curriculumIntent = new Intent(TeacherWelcomeActivity.this,
                            CurriculumSelector.class);
                    //TODO: Remove: curriculumIntent.putExtra("selection", output);
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
