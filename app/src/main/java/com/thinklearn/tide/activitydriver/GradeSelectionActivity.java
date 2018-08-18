package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


import com.thinklearn.tide.dto.Student;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GradeSelectionActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_selection);
        setTitle(R.string.select_grade);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.llClass);
        List<Student> studentInputList = getIntent().getParcelableArrayListExtra("studentInputList");
        ArrayList<String> gradeList = getUniqueGrades(studentInputList);
        float scale = getResources().getDisplayMetrics().density;
        for(int i=0;i<gradeList.size();i++){
            Button gradeButton = new Button(this);
            gradeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
            int padding = (int) (10*scale + 0.5f);
            gradeButton.setPadding(padding, padding, padding, padding);
            gradeButton.setText(gradeList.get(i));
            gradeButton.setTag(gradeList.get(i));
            gradeButton.setOnClickListener(this);
            linearLayout.addView(gradeButton);
        }
    }

    private ArrayList<String> getUniqueGrades(List<Student> studentInputList) {
        Set<String> uniqueGrades = new TreeSet<>();
        for (Student student : studentInputList ) {
            uniqueGrades.add(student.getGrade());
        }
        return new ArrayList<String>(uniqueGrades);
    }

    @Override
    public void onClick(View v) {
        String selectedGrade = (String) v.getTag();
        Intent intent = new Intent(GradeSelectionActivity.this,GenderSelectionActivity.class);
        intent.putExtra("selectedGrade",selectedGrade);
        intent.putExtra("purpose", getIntent().getStringExtra("purpose"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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
