package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import com.thinklearn.tide.interactor.AppInfo;
import com.thinklearn.tide.interactor.ClassroomConfig;
import com.thinklearn.tide.interactor.ClassroomInteractor;

public class StudentGradeSelectionActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_studentlogin_grade);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.studentGradeSelectionBar) ;
        setSupportActionBar(myToolbar);

        ImageButton switchtoteacher = (ImageButton)findViewById(R.id.switchtoteacher);
        ImageButton studentSettings = (ImageButton)findViewById(R.id.student_settings);

        String purpose = getIntent().getStringExtra("purpose");
        if (purpose.equals("STUDENT_ACTIVITY")) {
            setTitle(AppInfo.appVersion(this));
            ClassroomInteractor.setTabMode(ClassroomConfig.student_mode_value);
            switchtoteacher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(StudentGradeSelectionActivity.this,TeacherLoginActivity.class);
                    finish();
                    startActivity(intent);
                }
            });
            studentSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(StudentGradeSelectionActivity.this, SettingsUtilActivity.class);
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
        startActivity(intent);
    }
}
