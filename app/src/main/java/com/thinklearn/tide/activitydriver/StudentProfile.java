package com.thinklearn.tide.activitydriver;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class StudentProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_student_profile);

        TextView inputJson = findViewById(R.id.InputJson);

        Intent intent = getIntent();
        if(intent.hasExtra("STUDENT_PROFILE")) {
            String studentProfileJson = intent.getStringExtra("STUDENT_PROFILE");
            inputJson.setText(studentProfileJson);
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
