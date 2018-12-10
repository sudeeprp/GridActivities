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
import com.thinklearn.tide.interactor.AppInfo;
import com.thinklearn.tide.interactor.ClassroomInteractor;

import java.util.ArrayList;
import java.util.List;


public class TeacherWelcomeActivity extends AppCompatActivity {

    private List<Student> studentInputList = new ArrayList<>();
    private ArrayList<String> gradeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_teacher_start_screen);
        ((TextView)findViewById(R.id.teacher_start_version)).setText(AppInfo.appVersion(this));
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
                    //TODO: nobig remove passing big intent
                    //AttendanceInput attendance = ClassroomInteractor.get_current_week_attendance();
                    //intent.putExtra("attendance", attendance);
                    startActivityForResult(intent, 3);
                } else if(v == ivStudents) {
                    Intent intent = new Intent(TeacherWelcomeActivity.this, StudentGradeSelectionActivity.class);
                    //TODO: nobig remove passing big intent
                    //intent.putParcelableArrayListExtra("studentInputList", (ArrayList<? extends Parcelable>) studentInputList);
                    intent.putExtra("purpose", "PROFILE_EDIT");
                    startActivity(intent);
                } else if(v == ivDashboard) {
                    Intent curriculumIntent = new Intent(TeacherWelcomeActivity.this,
                            CurriculumSelector.class);
                    curriculumIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivityForResult(curriculumIntent, 3);
                }
            }
        };
        ivAttendance.setOnClickListener(listener);
        ivStudents.setOnClickListener(listener);
        ivDashboard.setOnClickListener(listener);
    }
}
