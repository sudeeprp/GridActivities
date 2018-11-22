package com.thinklearn.tide.activitydriver;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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


public class AttendenceManagementActivity extends AppCompatActivity implements View.OnClickListener {

    private AttendanceInput attendance = new AttendanceInput();
    private String todayStr = "today";
    private Boolean isAttendanceCaptured = false;
    class AttendancePosition{
        public int position;
        public int am_or_pm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendence_management);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        attendance = getIntent().getParcelableExtra("attendance");
        TableRow.LayoutParams wrapWrapTableRowParams = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        int[] fixedColumnWidths = new int[]{20, 10, 10, 10, 10, 10, 10, 10};
        int fixedRowHeight = 80;
        int fixedHeaderHeight = 60;
        final SimpleDateFormat dbFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        todayStr = dbFormatter.format(today);

        findViewById(R.id.SaveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAttendance();
                finish();
            }
        });

        TableRow row = new TableRow(this);
        TableRow clickableRow;
        //header (fixed vertically)
        TableLayout header = findViewById(R.id.table_header);
        row.setLayoutParams(wrapWrapTableRowParams);
        row.setGravity(Gravity.CENTER);
        row.setBackgroundColor(Color.YELLOW);
        //add student title row
        TextView studentNameHeader = makeTableRowWithText(getString(R.string.student_name), fixedColumnWidths[0], fixedHeaderHeight);
        studentNameHeader.setPadding(10, 10, 10, 10);
        studentNameHeader.setGravity(Gravity.START);
        row.addView(studentNameHeader);

        SimpleDateFormat shortFormatter = new SimpleDateFormat("dd MMM");
        Date weekStartDate = attendance.getWeekStartDate();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            String weekStartDateStr = dbFormatter.format(weekStartDate);
            if (todayStr.equals(weekStartDateStr))
                fixedColumnWidths[i + 1] = 20;
            TextView dateTextView = makeTableRowWithText(shortFormatter.format(weekStartDate), fixedColumnWidths[i + 1], fixedHeaderHeight);
            dateTextView.setPadding(6, 6, 6, 6);
            row.addView(dateTextView);
            calendar.setTime(weekStartDate);
            calendar.add(Calendar.DATE, 1);
            weekStartDate = calendar.getTime();
        }
        header.addView(row);

        List<Student> students = attendance.getStudentList();
        //header (fixed horizontally)
        TableLayout fixedColumn = findViewById(R.id.fixed_column);
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            TextView fixedView = makeTableRowWithText(student.getFirstName() + " " + student.getSurname(), fixedColumnWidths[0], fixedRowHeight, 18);
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
                    Button clickableAreaAM = makeTableRowWithButton(R.string.am, fixedColumnWidths[j + 1]/2, fixedRowHeight, wrapWrapTableRowParams);
                    setButtonAttributes(clickableAreaAM, R.string.am, i);
                    clickableRow.addView(clickableAreaAM);
                    if (attendance.getPresentStudentsAM() != null && attendance.getPresentStudentsAM().contains(student.getId())) {
                        clickableAreaAM.setText("✓");
                    }
                    Button clickableAreaPM = makeTableRowWithButton(R.string.pm, fixedColumnWidths[j + 1]/2, fixedRowHeight, wrapWrapTableRowParams);
                    setButtonAttributes(clickableAreaPM, R.string.pm,i);
                    clickableRow.addView(clickableAreaPM);
                    if (attendance.getPresentStudentsPM() != null && attendance.getPresentStudentsPM().contains(student.getId())) {
                        clickableAreaPM.setText("✓");
                    }
                } else {
                    TextView textViewAM = makeTableRowWithText("", fixedColumnWidths[j + 1]/2, fixedRowHeight);
                    textViewAM.setBackgroundColor(0xFFDDDDDD);
                    clickableRow.addView(textViewAM);
                    List<String> presentIdsAM = attendance.getPresentAM().get(weekStartDateStr);
                    textViewAM.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    if (weekStartDateStr.compareTo(todayStr) < 0&& (presentIdsAM != null && presentIdsAM.contains(student.getId()))) {
                        textViewAM.setText("✓");
                    }
                    TextView textViewPM = makeTableRowWithText("", fixedColumnWidths[j + 1]/2, fixedRowHeight);
                    clickableRow.addView(textViewPM);
                    List<String> presentIdsPM = attendance.getPresentPM().get(weekStartDateStr);
                    textViewPM.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    if (weekStartDateStr.compareTo(todayStr) < 0 && (presentIdsPM != null && presentIdsPM.contains(student.getId()))) {
                        textViewPM.setText("✓");
                    }
                }
                calendar.setTime(weekStartDate);
                calendar.add(Calendar.DATE, 1);
                weekStartDate = calendar.getTime();
            }
            fixedColumn.addView(clickableRow);
        }
    }

    private void saveAttendance() {
        ClassroomInteractor.set_day_presents (todayStr, attendance.getPresentStudentsAM(), attendance.getPresentStudentsPM());
        finish();
    }
    private void setButtonAttributes(Button clickableArea,int am_or_pm, int position){
        clickableArea.setOnClickListener(this);
        AttendancePosition position_tag_am = new AttendancePosition();
        position_tag_am.position = position;
        position_tag_am.am_or_pm = am_or_pm;
        clickableArea.setTag(position_tag_am);
        clickableArea.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }

    private Button makeTableRowWithButton(int text, int fixedColumnWidth, int fixedRowHeight, TableRow.LayoutParams wrapWrapTableRowParams) {
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
        return makeTableRowWithText(text, widthInPercentOfScreenWidth, fixedHeightInPixels, 15);
    }
    public TextView makeTableRowWithText(String text, int widthInPercentOfScreenWidth, int fixedHeightInPixels, int fontSize) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int width = (widthInPercentOfScreenWidth * screenWidth / 100);
        TextView recyclableTextView = new TextView(this);
        recyclableTextView.setText(text);
        recyclableTextView.setTextColor(Color.BLACK);
        recyclableTextView.setTextSize(fontSize);
        recyclableTextView.setWidth(width);
        recyclableTextView.setHeight(fixedHeightInPixels);
        recyclableTextView.setGravity(Gravity.CENTER);
        return recyclableTextView;
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        AttendancePosition  attendancePosition = (AttendancePosition) button.getTag();
        isAttendanceCaptured=true;
        if (attendance.getPresentStudentsAM() == null) {
            attendance.setPresentStudentsAM(new ArrayList<String>());
        }
        if (attendance.getPresentStudentsPM() == null) {
            attendance.setPresentStudentsPM(new ArrayList<String>());
        }
        String student_id = attendance.getStudentList().get(attendancePosition.position).getId();
        List<String> presentList = null;
        if(attendancePosition.am_or_pm == R.string.am) {
            presentList = attendance.getPresentStudentsAM();
        } else {
            presentList = attendance.getPresentStudentsPM();
        }
        if (!presentList.contains(student_id)) {
            button.setText("✓");
            presentList.add(student_id);
        } else {
            button.setText(attendancePosition.am_or_pm);
            presentList.remove(student_id);
        }
    }

    @Override
    public void onBackPressed() {
        if (isAttendanceCaptured) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.unsaved_attendance_warning);
            builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    saveAttendance();
                    finish();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            builder.create().show();
        }else{
            this.finish();
        }
    }
}
