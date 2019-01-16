package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.thinklearn.tide.dto.Student;
import com.thinklearn.tide.interactor.ActivityRecord;
import com.thinklearn.tide.interactor.ClassroomInteractor;
import com.thinklearn.tide.interactor.ClassroomProgressInteractor;

import java.util.ArrayList;

public class AssessmentRecordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_record);

        Student student = getSelectedStudent();
        if(student != null) {
            Toolbar toolbar = findViewById(R.id.assessment_record_activity);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(" " + student.getFirstName() + " " + student.getSurname());
            }
            ArrayList<ActivityRecord> activityRecords = ClassroomProgressInteractor.getActivityRecords(student);
            if(activityRecords != null) {
                TableLayout assessment = findViewById(R.id.assessment_table);
                for (ActivityRecord activityRecord : activityRecords) {
                    TableRow clickableRow = new TableRow(this);
                    TableRow.LayoutParams wrapWrapTableRowParams = new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    clickableRow.setLayoutParams(wrapWrapTableRowParams);
                    clickableRow.addView(make_datapointView(activityRecord));
                    clickableRow.addView(make_statusView(activityRecord));
                    assessment.addView(clickableRow);
                }
            }
        }
    }
    private String evaluation_to_symbols (ArrayList<String> evaluation){
        String eval_display = "";
        if(evaluation != null) {
            for (String str : evaluation) {
                if(str.equalsIgnoreCase("correct")) {
                    eval_display = eval_display.concat("✔️");
                } else if (str.equalsIgnoreCase("wrong")) {
                    eval_display = eval_display.concat("❌");
                } else if (str.equalsIgnoreCase("not attempted")) {
                    eval_display = eval_display.concat("⭕");
                }
            }
        }
        return eval_display;
    }
    private Student getSelectedStudent() {
        Student selectedStudent = null;
        Intent intent = getIntent();
        String studentID = intent.getStringExtra(StudentDetailFragment.STUDENT_ID);
        if(studentID != null) {
            selectedStudent = ClassroomInteractor.get_student(studentID);
        }
        return selectedStudent;
    }
    private View make_datapointView(ActivityRecord activityRecord) {
        TextView datapointView = new TextView(this);
        String eval_display = evaluation_to_symbols(activityRecord.getEvaluation());
        String combine = activityRecord.getSubjectID() +
                ", " + activityRecord.getChapterID() +
                ",  " + activityRecord.getActivityID() +
                "\n  Time in secs:\t" +  activityRecord.getTime_in_sec() +
                "\n  Maximum score:\t" + activityRecord.getMax_score() +
                "\n  Actual score:\t" + activityRecord.getActual_score() +
                "\n  Data Points:\t" + eval_display ;
        datapointView.setText(combine);
        datapointView.setTextSize(20);
        return datapointView;
    }
    private View make_statusView(ActivityRecord activityRecord) {
        ImageView statusView = new ImageView(this);
        String assessmentStatus = activityRecord.getAssessment_status();
        if(assessmentStatus != null) {
            if(assessmentStatus.equals("done")) {
                statusView.setImageResource(R.drawable.chapter_done2);
                statusView.setPadding(20, 20, 0, 20);
            } else if (assessmentStatus.equals("inprogress")) {
                statusView.setImageResource(R.drawable.chapter_inprogress2);
                statusView.setPadding(20, 20, 0, 20);
            } else if (assessmentStatus.equals("none")) {
                statusView.setImageResource(R.drawable.chapter_none2);
                statusView.setPadding(20, 20, 0, 20);
            } else if (assessmentStatus.equals("assessment_ready")) {
                statusView.setImageResource(R.drawable.chapter_assessment_ready2);
                statusView.setPadding(20, 20, 0, 20);
            }
        }
        return statusView;
    }
}
