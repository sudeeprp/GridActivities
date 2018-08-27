package com.thinklearn.tide.activitydriver;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.thinklearn.tide.dto.AttendanceInput;
import com.thinklearn.tide.dto.Student;
import com.thinklearn.tide.interactor.ClassroomInteractor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//TODO: Remove
import java.util.HashSet;
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

    private AttendanceInput attendance = new AttendanceInput();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendence_management);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        attendance = getIntent().getParcelableExtra("attendance");
        TableRow.LayoutParams wrapWrapTableRowParams = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        int[] fixedColumnWidths = new int[]{30, 10, 10, 10, 10, 10, 10, 10};
        int fixedRowHeight = 50;
        int fixedHeaderHeight = 60;
        final SimpleDateFormat dbFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        final String todayStr = dbFormatter.format(today);

        findViewById(R.id.SaveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClassroomInteractor.set_day_presents(todayStr, attendance.getPresentStudents());
            }
        });

        TableRow row = new TableRow(this);
        TableRow clickableRow = null;
        //header (fixed vertically)
        TableLayout header = (TableLayout) findViewById(R.id.table_header);
        row.setLayoutParams(wrapWrapTableRowParams);
        row.setGravity(Gravity.CENTER);
        row.setBackgroundColor(Color.YELLOW);
        //add student title row
        TextView studentNameHeader = makeTableRowWithText(getString(R.string.student_name), fixedColumnWidths[0], fixedHeaderHeight);
        studentNameHeader.setPadding(10, 10, 10, 10);
        studentNameHeader.setGravity(Gravity.START);
        row.addView(studentNameHeader);

        Date weekStartDate = attendance.getWeekStartDate();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            TextView dateTextView = makeTableRowWithText(dbFormatter.format(weekStartDate), fixedColumnWidths[i + 1], fixedHeaderHeight);
            dateTextView.setPadding(6, 6, 6, 6);
            row.addView(dateTextView);
            calendar.setTime(weekStartDate);
            calendar.add(Calendar.DATE, 1);
            weekStartDate = calendar.getTime();
        }
        header.addView(row);

        List<Student> students = attendance.getStudentList();
        //header (fixed horizontally)
        TableLayout fixedColumn = (TableLayout) findViewById(R.id.fixed_column);
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            TextView fixedView = makeTableRowWithText(student.getFirstName() + " " + student.getSurname(), fixedColumnWidths[0], fixedRowHeight);
            fixedView.setGravity(Gravity.START);
            fixedView.setPadding(10, 0, 10, 0);
            clickableRow = new TableRow(this);
            clickableRow.setLayoutParams(wrapWrapTableRowParams);
            clickableRow.setGravity(Gravity.CENTER);
            clickableRow.addView(fixedView);
            weekStartDate = attendance.getWeekStartDate();
            calendar = Calendar.getInstance();
            for (int j = 0; j < 7; j++) {
                String weekStartDateStr = dbFormatter.format(weekStartDate);
                if (attendance.getHolidayList() != null && attendance.getHolidayList().contains(weekStartDate)) {
                    TextView textView = makeTableRowWithText("", fixedColumnWidths[j + 1], fixedRowHeight);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    textView.setTextColor(getResources().getColor(R.color.colorAccent));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    textView.setText(R.string.holiday);
                    clickableRow.addView(textView);
                } else if (todayStr.equals(weekStartDateStr)) {
                    Button clickableArea = makeTableRowWithButton(" ", fixedColumnWidths[j + 1], fixedRowHeight, wrapWrapTableRowParams);
                    clickableArea.setOnClickListener(this);
                    clickableArea.setTag(i);
                    clickableRow.addView(clickableArea);
                    if (attendance.getPresentStudents() != null && attendance.getPresentStudents().contains(student.getId())) {
                        clickableArea.setText("✓");
                        clickableArea.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    }
                    //TODO: Remove<<clickableArea.setTag(students.get(i).getId());
                    
                    //TODO: Remove<<clickableRow.addView(clickableArea);
                } else {
                    TextView textView = makeTableRowWithText("", fixedColumnWidths[j + 1], fixedRowHeight);
                    clickableRow.addView(textView);
                    List<String> absenteeIds = attendance.getAbsentees().get(weekStartDateStr);
                    if (weekStartDateStr.compareTo(todayStr) < 0 && (absenteeIds == null || !absenteeIds.contains(student.getId()))) {
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
        int width = (fixedColumnWidth * screenWidth / 100);
        Button button = new Button(this);
        button.setLayoutParams(wrapWrapTableRowParams);
        button.setText(text);
        button.setHeight(fixedRowHeight);
        button.setWidth(width);
        button.setBackgroundColor(getResources().getColor(R.color.colorLimeGreen));
        return button;
    }

    public TextView makeTableRowWithText(String text, int widthInPercentOfScreenWidth, int fixedHeightInPixels) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int width = (widthInPercentOfScreenWidth * screenWidth / 100);
        TextView recyclableTextView = new TextView(this);
        recyclableTextView.setText(text);
        recyclableTextView.setTextColor(Color.BLACK);
        recyclableTextView.setTextSize(15);
        recyclableTextView.setWidth(width);
        recyclableTextView.setHeight(fixedHeightInPixels);
        recyclableTextView.setGravity(Gravity.CENTER);
        return recyclableTextView;
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        int position = (int) button.getTag();
        if (button.getText().equals(" ")) {
            button.setText("✓");
            if (attendance.getPresentStudents() == null)
                attendance.setPresentStudents(new ArrayList<String>());
            attendance.getPresentStudents().add(attendance.getStudentList().get(position).getId());
        } else {
            button.setText(" ");
            if (attendance.getPresentStudents() == null)
                attendance.setPresentStudents(new ArrayList<String>());
            attendance.getPresentStudents().remove(attendance.getStudentList().get(position).getId());
        }
    }
}
