package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.thinklearn.tide.adapter.TeacherItemRecyclerViewAdapter;
import com.thinklearn.tide.dto.Teacher;

import java.util.ArrayList;
import java.util.List;

public class TeacherLoginActivity extends AppCompatActivity implements TeacherItemRecyclerViewAdapter.TeacherSelectedClickListener,
                                ViewSwitcher.ViewFactory {

    private List<Teacher> teacherWelcomeInputList = new ArrayList<>();
    RecyclerView recyclerView;
    TextSwitcher selectedTeacherName;
    TeacherItemRecyclerViewAdapter teacherItemRecyclerViewAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_teacher_login);

        recyclerView = findViewById(R.id.rvHorizantalTeacherList);
        selectedTeacherName = findViewById(R.id.tvSSelectedteacherName);
        selectedTeacherName.setFactory(this);
        selectedTeacherName.setCurrentText(getText(R.string.unknown_teacher));

        Intent teacherLoginIntent = getIntent();
        teacherWelcomeInputList = teacherLoginIntent.getParcelableArrayListExtra("TEACHER_LIST");

        teacherItemRecyclerViewAdapter = new TeacherItemRecyclerViewAdapter(TeacherLoginActivity.this,teacherWelcomeInputList,TeacherLoginActivity.this);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(TeacherLoginActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));
        recyclerView.setAdapter(teacherItemRecyclerViewAdapter);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(TeacherLoginActivity.this,TeacherWelcomeActivity.class);
                intent.putExtra("TEACHER_IDENTIFICATION", teacherWelcomeInputList.get(teacherItemRecyclerViewAdapter.getSelectedPosition()));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onTeacherSelected(View v, int position) {
         Teacher teacherWelcomeInput = teacherWelcomeInputList.get(position);
        selectedTeacherName.setText(teacherWelcomeInput.getTeacherName());
    }

    @Override
    public View makeView() {
        TextView t = new TextView(this);
        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        return t;

    }
}
