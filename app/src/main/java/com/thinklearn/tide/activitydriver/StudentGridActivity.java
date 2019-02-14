package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.thinklearn.tide.adapter.StudentGridAdapter;
import com.thinklearn.tide.dto.Student;
import com.thinklearn.tide.interactor.ClassroomContext;
import com.thinklearn.tide.interactor.ClassroomInteractor;

import java.util.ArrayList;
import java.util.List;

import static com.thinklearn.tide.activitydriver.R.id.loginButton;
import static com.thinklearn.tide.activitydriver.R.id.selected;

public class StudentGridActivity extends AppCompatActivity implements StudentGridAdapter.StudentSelectedListener{


    List<Student> studentInputList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_grid);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        String selectedGrade = getIntent().getStringExtra("selectedGrade");
        String selectedGender = getIntent().getStringExtra("selectedGender");
        String displayGrade = getString(
                getResources().getIdentifier("grade" + selectedGrade,"string", getPackageName()));
        ((TextView) findViewById(R.id.selectedClass)).setText(displayGrade);
        if(!selectedGender.isEmpty()) {
            String displayGender = getString(
                    getResources().getIdentifier(selectedGender, "string", getPackageName()));
            ((TextView) findViewById(R.id.selectedGender)).setText(displayGender);
        }
        findViewById(R.id.loginButton).setEnabled(false);

        studentInputList = ClassroomInteractor.filterStudents(selectedGrade, selectedGender, true);
        GridView gridView = findViewById(R.id.gvStudentGrid);

        final StudentGridAdapter studentGridAdapter = new StudentGridAdapter(StudentGridActivity.this,studentInputList,StudentGridActivity.this);
        gridView.setAdapter(studentGridAdapter);

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ClassroomContext.selectedStudent = studentGridAdapter.getSelectedStudent();
                ClassroomContext.selectedTeacher = null;
                Intent curriculumIntent =
                        new Intent(StudentGridActivity.this, CurriculumSelector.class);
                curriculumIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                curriculumIntent.putExtra("selectedStudent", ClassroomContext.selectedStudent);
                startActivityForResult(curriculumIntent, 3);
            }
        });
    }

    @Override
    public void onStudentSelected(View v, int position) {
        Student selectedStudent = studentInputList.get(position);
        ((TextView)findViewById(R.id.selectedStudent)).setText(selectedStudent.getFirstName()+" "+selectedStudent.getSurname());
        findViewById(R.id.loginButton).setEnabled(true);
    }
}
