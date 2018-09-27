package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
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
import com.thinklearn.tide.interactor.AppInfo;
import com.thinklearn.tide.interactor.ClassroomContext;
import com.thinklearn.tide.interactor.ClassroomInteractor;
import com.thinklearn.tide.interactor.ConfigKeys;

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
        ((TextView)findViewById(R.id.tvVersion)).setText(AppInfo.appVersion(this));

        ClassroomInteractor.setTabMode(ConfigKeys.teacher_mode_value);

        recyclerView = findViewById(R.id.rvHorizantalTeacherList);
        selectedTeacherName = findViewById(R.id.tvSSelectedteacherName);
        findViewById(R.id.login).setEnabled(false);
        selectedTeacherName.setFactory(this);
        selectedTeacherName.setCurrentText(getText(R.string.unknown_teacher));

        TextView className = findViewById(R.id.class_name);
        className.setText(ClassroomInteractor.class_name);
        TextView schoolName = findViewById(R.id.school_name);
        schoolName.setText(ClassroomInteractor.school_name);

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
                ClassroomContext.selectedTeacher = teacherWelcomeInputList.get(teacherItemRecyclerViewAdapter.getSelectedPosition());
                ClassroomContext.selectedStudent = null;
                Intent intent  = new Intent(TeacherLoginActivity.this,TeacherWelcomeActivity.class);
                intent.putExtra("TEACHER_IDENTIFICATION", ClassroomContext.selectedTeacher);
                startActivity(intent);
            }
        });
        findViewById(R.id.switchtostudent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherLoginActivity.this, StudentGradeSelectionActivity.class);
                intent.putParcelableArrayListExtra("studentInputList", ClassroomInteractor.students);
                intent.putExtra("purpose", "STUDENT_ACTIVITY");
                finish();
                startActivity(intent);
            }
        });
    }

    @Override
    public void onTeacherSelected(View v, int position) {
        Teacher selectedTeacher = teacherWelcomeInputList.get(position);
        selectedTeacherName.setText(selectedTeacher.getTeacherName());
        findViewById(R.id.login).setEnabled(true);
    }

    @Override
    public View makeView() {
        TextView t = new TextView(this);
        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        return t;

    }
}
