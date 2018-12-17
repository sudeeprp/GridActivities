package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.thinklearn.tide.dto.Student;

import javax.xml.transform.Result;


public class AssessmentRecordActivity extends AppCompatActivity {


    static class AssessmentRecord {
        String subject = "";
        String chapter = "";
        String activity_id = "";
        int time = 0;
        int max_score = 0;
        int actual_score = 0;
        String[] evaluation = {};
        String status = "";
    }

    private AssessmentRecord[] getAssessmentRecords(String studentId) {

        AssessmentRecord[] records = new AssessmentRecord[4];
        records[0] = new AssessmentRecord();
        records[0].subject = "French";
        records[0].chapter = "Chapter 2";
        records[0].activity_id = "Assessment 1";
        records[0].actual_score = 3;
        records[0].max_score = 4;
        records[0].time = 120;
        String[] evaluation0 = {"Correct","Wrong","not attempted"};
        records[0].evaluation = evaluation0;
        records[0].status = "done";

        records[1] = new AssessmentRecord();
        records[1].subject = "French";
        records[1].chapter = "Chapter 2";
        records[0].activity_id = "Assessment 2";
        records[1].actual_score = 4;
        records[1].max_score = 6;
        records[1].time = 180;
        String[] evaluation1 = {"Correct","Wrong","not attempted","Mistake"};
        records[1].evaluation = evaluation1;
        records[1].status = "inprogress";

        records[2] = new AssessmentRecord();
        records[2].subject = "French";
        records[2].chapter = "Chapter 2";
        records[2].activity_id = "Assessment 2";
        records[2].actual_score = 6;
        records[2].max_score = 10;
        records[2].time = 100;
        String[] evaluation2 = {"Correct","Wrong","not attempted","Mistake"};
        records[2].evaluation = evaluation2;
        records[2].status = "none";

        records[3] = new AssessmentRecord();
        records[3].subject = "Maths";
        records[3].chapter = "Chapter 2";
        records[3].activity_id = "Assessment 2";
        records[3].actual_score = 3;
        records[3].max_score = 15;
        records[3].time = 200;
        String[] evaluation3 = {"Wrong","Correct","Wrong","not attempted","Mistake"};
        records[3].evaluation = evaluation3;
        records[3].status = "assessment_ready";

        return records;
    }

    private String evaluation_to_symbols (String [] evaluation){
        String eval_display = "";
        for (String str: evaluation) {
            if (str.equalsIgnoreCase("correct")){
                eval_display = eval_display.concat("✔️");
            }
            if (str.equalsIgnoreCase("wrong")){
                eval_display = eval_display.concat("❌");

            }
            if (str.equalsIgnoreCase("not attempted")){
                eval_display = eval_display.concat("⭕");
            }

        }
        return eval_display;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_record);


        Toolbar toolbar = findViewById(R.id.assessment_record_activity);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        Intent intent = getIntent();
        Student student  = intent.getParcelableExtra(StudentDetailFragment.STUDENT);
        getSupportActionBar().setTitle(" " + student.getFirstName() + " " + student.getSurname());
        AssessmentRecord[] records = getAssessmentRecords(StudentDetailFragment.STUDENT);

        for (AssessmentRecord assessmentRecord: records){
            String eval_display = evaluation_to_symbols(assessmentRecord.evaluation);
            String combine = assessmentRecord.subject + ", " + assessmentRecord.chapter + "\n  " + "Time in secs:\t" +  assessmentRecord.time + "\n  " + "Maximum score:\t" + assessmentRecord.max_score + "\n  " + "Actual score:\t" + assessmentRecord.actual_score +"\n  " + "Data Points:\t" + eval_display ;
            TableLayout assessment = findViewById(R.id.assessment_table);
            TableRow clickableRow;
            clickableRow = new TableRow(this);
            TableRow.LayoutParams wrapWrapTableRowParams = new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            clickableRow.setLayoutParams(wrapWrapTableRowParams);
            TextView datapointView = new TextView(this);
            datapointView.setText(combine);
            datapointView.setTextSize(20);
            clickableRow.addView(datapointView);
            assessment.addView(clickableRow);
            ImageView statusView = new ImageView(this);

            if (assessmentRecord.status.equals("done")){
                statusView.setImageResource(R.drawable.chapter_done2);
                statusView.setPadding(20,20,0,20);
            }
            if ( assessmentRecord.status.equals("inprogress")){
                statusView.setImageResource(R.drawable.chapter_inprogress2);
                statusView.setPadding(20,20,0,20);
            }
            if ( assessmentRecord.status.equals("none")){
                statusView.setImageResource(R.drawable.chapter_none2);
                statusView.setPadding(20,20,0,20);
            }
            if( assessmentRecord.status.equals("assessment_ready")) {
                statusView.setImageResource(R.drawable.chapter_assessment_ready2);
                statusView.setPadding(20,20,0,20);
            }
            clickableRow.addView(statusView);
        }
    }
}
