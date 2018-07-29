package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class StudentIdentification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_student_identification);

        TextView inputJson = findViewById(R.id.InputJson);

        Intent intent = getIntent();
        if(intent.hasExtra("STUDENT_LIST")) {
            String teacherIdentJson = intent.getStringExtra("STUDENT_LIST");
            inputJson.setText(teacherIdentJson);
        }
    }
}
