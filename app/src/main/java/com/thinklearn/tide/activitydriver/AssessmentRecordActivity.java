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
import com.thinklearn.tide.interactor.ClassroomDBInteractor;
import com.thinklearn.tide.interactor.ClassroomInteractor;
import com.thinklearn.tide.interactor.ClassroomProgressInteractor;
import com.thinklearn.tide.interactor.ContentInteractor;
import com.thinklearn.tide.interactor.ProgressInteractor;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class AssessmentRecordActivity extends AppCompatActivity {
    private Student selectedStudent;
    private ArrayList<ActivityRecord> activityRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_record);

        fetchRefresh();
    }
    private void fetchRefresh() {
        Student student = getSelectedStudent();
        if(student != null) {
            selectedStudent = student;
            Toolbar toolbar = findViewById(R.id.assessment_record_activity);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle
                        (" " + selectedStudent.getFirstName() + " " + selectedStudent.getSurname());
            }
            activityRecords = ClassroomProgressInteractor.getActivityRecords(selectedStudent);
            if(activityRecords != null) {
                TableLayout assessment = findViewById(R.id.assessment_table);
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView statusView = (ImageView)v;
                        int i = (int)statusView.getTag();
                        String newStatus = toggleActivityStatus(activityRecords.get(i));
                        activityRecords.get(i).assessment_status = newStatus;
                        statusView.setImageResource(assessmentStatus_to_imageID(newStatus));
                    }
                };
                for(int i = 0; i < activityRecords.size(); i++) {
                    TableRow clickableRow = new TableRow(this);
                    TableRow.LayoutParams wrapWrapTableRowParams = new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    clickableRow.setLayoutParams(wrapWrapTableRowParams);
                    clickableRow.addView(make_datapointHeading(activityRecords.get(i)));
                    clickableRow.addView(make_datapointView(activityRecords.get(i)));
                    clickableRow.addView(make_statusView(activityRecords.get(i), listener, i));
                    assessment.addView(clickableRow);
                }
            }
        }
    }
    private String toggleActivityStatus(ActivityRecord activityRecord) {
        String currentAssessmentStatus = activityRecord.assessment_status;
        String newStatus = currentAssessmentStatus;
        if(currentAssessmentStatus != null) {
            if(currentAssessmentStatus.equals(ProgressInteractor.assessment_ready_status)) {
                newStatus = ProgressInteractor.approved_status;
            } else if(currentAssessmentStatus.equals(ProgressInteractor.approved_status)) {
                newStatus = ProgressInteractor.assessment_ready_status;
            }
            ClassroomDBInteractor.set_student_activity_status(selectedStudent.getId(),
                    activityRecord.getSubjectID(), activityRecord.getChapterID(), activityRecord.getActivityID(),
                    newStatus);
        }
        return newStatus;
    }
    private int assessmentStatus_to_imageID(@Nullable String assessmentStatus) {
        int imageResourceID = R.drawable.chapter_none2;
        if(assessmentStatus != null) {
            switch(assessmentStatus) {
                case ProgressInteractor.done_status:
                case ProgressInteractor.approved_status:
                    imageResourceID = R.drawable.chapter_done2;
                    break;
                case ProgressInteractor.inprogress_status:
                    imageResourceID = R.drawable.chapter_inprogress2;
                    break;
                case ProgressInteractor.assessment_ready_status:
                    imageResourceID = R.drawable.chapter_assessment_ready2;
                    break;
            }
        }
        return imageResourceID;
    }
    private String evaluation_to_symbols (ArrayList<String> evaluation){
        String eval_display = "";
        if(evaluation != null) {
            for (String str : evaluation) {
                if(str.equalsIgnoreCase(ProgressInteractor.correct_result)) {
                    eval_display = eval_display.concat("✔️");
                } else if (str.equalsIgnoreCase(ProgressInteractor.incorrect_result)) {
                    eval_display = eval_display.concat("❌");
                } else if (str.equalsIgnoreCase(ProgressInteractor.not_attempted)) {
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
        TableLayout datapointTable = new TableLayout(this);

        add_datapointRow(datapointTable, R.string.activity_duration_label, activityRecord.getTime_in_sec());
        add_datapointRow(datapointTable, R.string.activity_max_score_label, activityRecord.getMax_score());
        add_datapointRow(datapointTable, R.string.activity_actual_score_label, activityRecord.getActual_score());
        String eval_display = evaluation_to_symbols(activityRecord.getEvaluation());
        add_datapointRow(datapointTable, R.string.activity_datapoints_label, eval_display);

        return datapointTable;
    }
    private View make_datapointHeading(ActivityRecord activityRecord) {
        TextView datapointHeading = new TextView(this);
        ContentInteractor contentInteractor = new ContentInteractor();

        datapointHeading.setText(
                contentInteractor.get_subject_display_name(activityRecord.getSubjectID(), this, getPackageName()) +
                ", " + contentInteractor.get_chapter_display_name(selectedStudent.getGrade(),
                                                                activityRecord.getSubjectID(),
                                                                activityRecord.getChapterID()) +
                "\n" + activityRecord.getActivityID());
        datapointHeading.setTextSize(20);
        datapointHeading.setPadding(8, 8, 8, 8);
        return datapointHeading;
    }
    private void add_datapointRow(TableLayout table, int labelResourceID, String value) {
        if(!value.isEmpty()) {
            TableRow datapointRow = new TableRow(this);

            TextView labelView = new TextView(this);
            labelView.setText(labelResourceID);
            labelView.setTextSize(20);
            labelView.setPadding(8, 8, 8, 0);
            datapointRow.addView(labelView);

            TextView valueView = new TextView(this);
            valueView.setText(value);
            valueView.setTextSize(20);
            valueView.setPadding(8, 8, 8, 0);
            datapointRow.addView(valueView);

            datapointRow.setPadding(8, 0, 8, 4);
            table.addView(datapointRow);
        }
    }
    private View make_statusView(ActivityRecord activityRecord, View.OnClickListener listener, Object tag) {
        ImageView statusView = new ImageView(this);
        statusView.setTag(tag);
        statusView.setOnClickListener(listener);
        statusView.setPadding(20, 20, 0, 20);
        String assessmentStatus = activityRecord.assessment_status;
        statusView.setImageResource(assessmentStatus_to_imageID(assessmentStatus));
        return statusView;
    }
}
