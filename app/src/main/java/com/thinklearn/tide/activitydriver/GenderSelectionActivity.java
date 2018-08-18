package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.thinklearn.tide.dto.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GenderSelectionActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender_selection);
        setTitle(R.string.select_gender);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.llGender);
        List<Student> studentInputList = getIntent().getParcelableArrayListExtra("studentInputList");
        String selectedGrade = getIntent().getStringExtra("selectedGrade");
        setTitle(getString(R.string.select_gender) + "(" + getString(R.string.current_grade) + ":" + selectedGrade + ")");
        ArrayList<String> genderList = getUniqueGenders(studentInputList);
        float scale = getResources().getDisplayMetrics().density;
        for(int i=0;i<genderList.size();i++){
            Button genderButton = new Button(this);
            genderButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
            int padding = (int) (10*scale + 0.5f);
            genderButton.setPadding(padding, padding, padding, padding);
            genderButton.setText(genderList.get(i));
            genderButton.setTag(genderList.get(i));
            genderButton.setOnClickListener(this);
            linearLayout.addView(genderButton);
        }
    }

    private ArrayList<String> getUniqueGenders(List<Student> studentInputList) {
        Set<String> uniqueGenders = new TreeSet<>();
        for (Student student : studentInputList ) {
            uniqueGenders.add(student.getGender());
        }
        return new ArrayList<String>(uniqueGenders);
    }

    @Override
    public void onClick(View v) {
        String selectedGender = (String) v.getTag();
        Intent intent = null;
        if(getIntent().getStringExtra("purpose").equals("PROFILE_EDIT")) {
            intent = new Intent(GenderSelectionActivity.this, StudentListActivity.class);
        } else if(getIntent().getStringExtra("purpose").equals("STUDENT_ACTIVITY")) {
            intent = new Intent(GenderSelectionActivity.this, StudentGridActivity.class);
        } else {
            Toast.makeText(this,"unknown purpose", Toast.LENGTH_LONG).show();;
            return;
        }
        intent.putExtra("selectedGrade",getIntent().getStringExtra("selectedGrade"));
        intent.putExtra("selectedGender",selectedGender);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        List<Student> students = getIntent().getParcelableArrayListExtra("studentInputList");
        intent.putParcelableArrayListExtra("studentInputList", (ArrayList<Student>) genderFilter(students, selectedGender));
        startActivity(intent);
    }

    private List<Student> genderFilter(List<Student> students, String selectedGender) {
        List<Student> filteredStudents = new ArrayList<>();
        for (Student student : students) {
            if(student.getGender().equals(selectedGender))
                filteredStudents.add(student);
        }
        return filteredStudents;
    }
}
