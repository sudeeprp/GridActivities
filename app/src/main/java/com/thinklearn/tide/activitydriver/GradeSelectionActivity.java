package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


import com.thinklearn.tide.dto.Student;
import com.thinklearn.tide.interactor.ClassroomInteractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GradeSelectionActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_curriculum_selector);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar) ;
        setSupportActionBar(myToolbar);

        Button switchtoteacher = (Button)findViewById(R.id.switchtoteacher);

        String purpose = getIntent().getStringExtra("purpose");
        if (purpose.equals("STUDENT_ACTIVITY")) {
            switchtoteacher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(GradeSelectionActivity.this,TeacherLoginActivity.class);
                    intent.putParcelableArrayListExtra("TEACHER_LIST", ClassroomInteractor.teachers);
                    startActivity(intent);
                }
            });
        }else {
            switchtoteacher.setVisibility(View.GONE);
        }
        setTitle(R.string.select_grade);


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
        Intent intent = new Intent(GradeSelectionActivity.this,GenderSelectionActivity.class);
        intent.putExtra("selectedGrade",selectedGrade);
        String purpose = getIntent().getStringExtra("purpose");
        intent.putExtra("purpose", purpose);
        if(purpose.equals("STUDENT_ACTIVITY")) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        }
        List<Student> students = getIntent().getParcelableArrayListExtra("studentInputList");
        intent.putParcelableArrayListExtra("studentInputList", (ArrayList<Student>) gradeFilter(students, selectedGrade));
        startActivity(intent);
    }

    private List<Student> gradeFilter(List<Student> students, String selectedGrade) {
        List<Student> filteredStudents = new ArrayList<>();
        for (Student student : students) {
            if(student.getGrade().equals(selectedGrade))
                filteredStudents.add(student);
        }
        return filteredStudents;
    }
}
