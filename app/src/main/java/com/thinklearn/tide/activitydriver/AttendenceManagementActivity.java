package com.thinklearn.tide.activitydriver;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.thinklearn.tide.dto.AttendanceInput;
import com.thinklearn.tide.dto.Student;
import com.thinklearn.tide.interactor.ClassroomInteractor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

class AttendanceRecord {
    HashSet<String> absentees = new HashSet<String>();
    Date date;
    AttendanceRecord(List<Student> students, Date recordDate) {
        date = recordDate;
        for(Student student: students) {
            absentees.add(student.getId());
        }
    }
    void set_present(String id) {
        absentees.remove(id);
    }
    void set_absent(String id) {
        absentees.add(id);
    }
}

public class AttendenceManagementActivity extends AppCompatActivity implements View.OnClickListener {
    AttendanceRecord record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendence_management);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        final AttendanceInput attendance = getIntent().getParcelableExtra("attendance");
        TableRow.LayoutParams wrapWrapTableRowParams = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        int[] fixedColumnWidths = new int[]{30, 10, 10, 10, 10, 10, 10, 10};
        int fixedRowHeight = 50;
        int fixedHeaderHeight = 60;

        findViewById(R.id.SaveButton).setOnClickListener(this);

        TableRow row = new TableRow(this);
        TableRow clickableRow=null;
        //header (fixed vertically)
        TableLayout header = (TableLayout) findViewById(R.id.table_header);
        row.setLayoutParams(wrapWrapTableRowParams);
        row.setGravity(Gravity.CENTER);
        row.setBackgroundColor(Color.YELLOW);
        //add student title row
        TextView studentNameHeader = makeTableRowWithText(getString(R.string.student_name), fixedColumnWidths[0], fixedHeaderHeight);
        studentNameHeader.setPadding(10,10,10,10);
        row.addView(studentNameHeader);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date weekStartDate = attendance.getWeekStartDate();
        Date today = new Date();
        String todayStr = formatter.format(today);
        Calendar calendar = Calendar.getInstance();
        for(int i=0;i < 7;i++) {
            TextView dateTextView =makeTableRowWithText(formatter.format(weekStartDate), fixedColumnWidths[i+1], fixedHeaderHeight) ;
            dateTextView.setPadding(6,6,6,6);
            row.addView(dateTextView);
            calendar.setTime(weekStartDate);
            calendar.add(Calendar.DATE, 1);
            weekStartDate = calendar.getTime();
        }
        header.addView(row);

        List<Student> students = attendance.getStudentList();
        record = new AttendanceRecord(students, today);
        //header (fixed horizontally)
        TableLayout fixedColumn = (TableLayout) findViewById(R.id.fixed_column);
        for(int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            TextView fixedView = makeTableRowWithText(student.getFirstName()+" "+student.getSurname(), fixedColumnWidths[0], fixedRowHeight);
            fixedView.setBackgroundColor(Color.WHITE);
            fixedView.setPadding(10,10,10,10);
            clickableRow = new TableRow(this);
            clickableRow.setLayoutParams(wrapWrapTableRowParams);
            clickableRow.setGravity(Gravity.CENTER);
            clickableRow.setBackgroundColor(Color.WHITE);
            clickableRow.addView(fixedView);
            weekStartDate = attendance.getWeekStartDate();
            calendar = Calendar.getInstance();
            for(int j=0;j < 7;j++) {
                String weekStartDateStr = formatter.format(weekStartDate);
                if (todayStr.equals(weekStartDateStr)) {
                    Button clickableArea = makeTableRowWithButton(" ", fixedColumnWidths[j + 1], fixedRowHeight, wrapWrapTableRowParams);
                    clickableArea.setOnClickListener(this);
                    clickableArea.setTag(students.get(i).getId());
                    clickableArea.setPadding(8, 8, 8, 8);
                    clickableRow.addView(clickableArea);
                } else {
                    TextView textView = makeTableRowWithText("", fixedColumnWidths[j+1], fixedRowHeight);
                    clickableRow.addView(textView);
                    List<String> absenteeIds = attendance.getAbsentees().get(weekStartDateStr);
                    if(weekStartDateStr.compareTo(todayStr) < 0 && (absenteeIds == null || !absenteeIds.contains(student.getId()))) {
                        textView.setText("✓");
                        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    }
                }
                calendar.setTime(weekStartDate);
                calendar.add(Calendar.DATE, 1);
                weekStartDate = calendar.getTime();
            }
            fixedColumn.addView(clickableRow);
        }
    }

    private Button makeTableRowWithButton(String text, int fixedColumnWidth, int fixedRowHeight, TableRow.LayoutParams wrapWrapTableRowParams) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int width=(fixedColumnWidth * screenWidth / 100);
        Button button = new Button(this);
        button.setLayoutParams(wrapWrapTableRowParams);
        button.setText(text);
        button.setHeight(fixedRowHeight);
        button.setWidth(width);
        return button;
    }

    //util method
    private TextView recyclableTextView;

    public TextView makeTableRowWithText(String text, int widthInPercentOfScreenWidth, int fixedHeightInPixels) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int width=(widthInPercentOfScreenWidth * screenWidth / 100);
        recyclableTextView = new TextView(this);
        recyclableTextView.setText(text);
        recyclableTextView.setTextColor(Color.BLACK);
        recyclableTextView.setTextSize(15);
        recyclableTextView.setWidth(width);
        recyclableTextView.setHeight(fixedHeightInPixels);
        return recyclableTextView;
    }

    @Override
    public void onClick(View v) {
        if(v == findViewById(R.id.SaveButton)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            ClassroomInteractor.set_day_attendance
                    (formatter.format(record.date), new ArrayList<String>(record.absentees));
        } else {
            Button button = (Button) v;
            if (button.getText().equals(" ")) {
                button.setText("✓");
                record.set_present((String)(button.getTag()));
            } else {
                button.setText(" ");
                record.set_absent((String)(button.getTag()));
            }
        }
    }
}
