package com.thinklearn.tide.activitydriver;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class TeacherIdentification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_teacher_identification);

        TextView inputJson = findViewById(R.id.InputJson);

        Intent intent = getIntent();
        if(intent.hasExtra("TEACHER_IDENTIFICATION")) {
            String teacherIdentJson = intent.getStringExtra("TEACHER_IDENTIFICATION");
            inputJson.setText(teacherIdentJson);
        }

        findViewById(R.id.DoneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", "\"under construction\"");
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}
