package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.thinklearn.tide.dto.Student;
import com.thinklearn.tide.interactor.ClassroomInteractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GenderSelectionActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gender_selection);
        String selectedGrade = getIntent().getStringExtra("selectedGrade");
        setTitle(getString(R.string.select_gender) + " (" + getString(R.string.current_grade) + ":" + selectedGrade + ")");

        List<Student> studentInputList = ClassroomInteractor.filterStudents(selectedGrade, false);
        if(studentInputList.size() > 24) {
            ArrayList<String> genderList = getUniqueGenders(studentInputList);
            if (genderList.size() == 1) {
                startNextActivity(genderList.get(0));
            }
            Button boyButton = (Button) findViewById(R.id.boy_button);
            boyButton.setTag("boy");
            boyButton.setOnClickListener(this);

            Button girlButton = (Button) findViewById(R.id.girl_button);
            girlButton.setTag("girl");
            girlButton.setOnClickListener(this);
        } else {
            startNextActivity("");
            finish();
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
        startNextActivity(selectedGender);
    }

    private void startNextActivity(String selectedGender) {
        Intent intent = null;
        if(getIntent().getStringExtra("purpose").equals("PROFILE_EDIT")) {
            intent = new Intent(GenderSelectionActivity.this, StudentListActivity.class);
        } else if(getIntent().getStringExtra("purpose").equals("STUDENT_ACTIVITY")) {
            intent = new Intent(GenderSelectionActivity.this, StudentGridActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        } else {
            Toast.makeText(this,"unknown purpose", Toast.LENGTH_LONG).show();;
            return;
        }
        intent.putExtra("selectedGrade",getIntent().getStringExtra("selectedGrade"));
        intent.putExtra("selectedGender",selectedGender);
        startActivity(intent);
    }
}
