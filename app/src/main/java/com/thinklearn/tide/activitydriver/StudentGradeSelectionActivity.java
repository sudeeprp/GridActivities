package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;


import com.thinklearn.tide.dto.Student;
import com.thinklearn.tide.interactor.AppInfo;
import com.thinklearn.tide.interactor.ClassroomInteractor;
import com.thinklearn.tide.interactor.ConfigKeys;

import java.util.ArrayList;
import java.util.List;

public class StudentGradeSelectionActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_studentlogin_grade);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.studentGradeSelectionBar) ;
        setSupportActionBar(myToolbar);

        ImageButton switchtoteacher = (ImageButton)findViewById(R.id.switchtoteacher);

        String purpose = getIntent().getStringExtra("purpose");
        if (purpose.equals("STUDENT_ACTIVITY")) {
            setTitle(AppInfo.appVersion(this));
            ClassroomInteractor.setTabMode(ConfigKeys.student_mode_value);
            switchtoteacher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(StudentGradeSelectionActivity.this,TeacherLoginActivity.class);
                    //TODO: nobig remove passing big intent
                    //intent.putParcelableArrayListExtra("TEACHER_LIST", ClassroomInteractor.teachers);
                    finish();
                    startActivity(intent);
                }
            });
        }else {
            setTitle(R.string.select_grade);
            myToolbar.setVisibility(View.GONE);
            switchtoteacher.setVisibility(View.GONE);
        }


        Button grade1button = (Button)findViewById(R.id.grade1button);
        grade1button.setOnClickListener(this);
        grade1button.setTag("1");

        Button grade2button = (Button)findViewById(R.id.grade2button);
        grade2button.setOnClickListener(this);
        grade2button.setTag("2");

    }

    @Override
    public void onClick(View v) {
        String selectedGrade = (String) v.getTag();
        Intent intent = new Intent(StudentGradeSelectionActivity.this,GenderSelectionActivity.class);
        intent.putExtra("selectedGrade",selectedGrade);
        String purpose = getIntent().getStringExtra("purpose");
        intent.putExtra("purpose", purpose);
        if(purpose.equals("STUDENT_ACTIVITY")) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        }
        //TODO: nobig remove passing big intent
        //List<Student> students = getIntent().getParcelableArrayListExtra("studentInputList");
        //intent.putParcelableArrayListExtra("studentInputList", (ArrayList<Student>) gradeFilter(students, selectedGrade));
        startActivity(intent);
    }

    //TODO: nobig remove passing big intent
    //private List<Student> gradeFilter(List<Student> students, String selectedGrade) {
    //    List<Student> filteredStudents = new ArrayList<>();
    //    for (Student student : students) {
    //        if(student.getGrade().equals(selectedGrade))
    //            filteredStudents.add(student);
    //    }
    //    return filteredStudents;
    //}
}
