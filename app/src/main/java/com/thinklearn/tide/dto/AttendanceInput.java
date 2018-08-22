package com.thinklearn.tide.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AttendanceInput implements Parcelable {

    private Long id;

    private Date weekStartDate;

    private List<Date> holidayList;

    private List<Student> studentList;

    private Map<String, List<String>> absentees;

    public AttendanceInput() {

    }

    protected AttendanceInput(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readLong();
        long tmpWeekStartDate = in.readLong();
        weekStartDate = tmpWeekStartDate != -1 ? new Date(tmpWeekStartDate) : null;
        holidayList = new ArrayList<Date>();
        if (in.readByte() == 0x01) {
            in.readList(holidayList, Date.class.getClassLoader());
        }
        studentList = new ArrayList<Student>();
        if (in.readByte() == 0x01) {
            in.readList(studentList, Student.class.getClassLoader());
        }
        if (in.readByte() == 0x01) {
            absentees = readAbsentees(in);
        } else {
            absentees = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeLong(id);
        }
        dest.writeLong(weekStartDate != null ? weekStartDate.getTime() : -1L);
        if (holidayList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(holidayList);
        }
        if (studentList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(studentList);
        }
        if (absentees == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            writeAbsenteesToParcel(dest, flags);
        }
    }

    // For writing to a Parcel
    public void writeAbsenteesToParcel(
            Parcel parcel, int flags) {
        parcel.writeInt(absentees.size());
        for(Map.Entry<String, List<String>> e : absentees.entrySet()){
            parcel.writeString(e.getKey());
            parcel.writeStringList(e.getValue());
        }
    }

    // For reading from a Parcel
    public Map<String, List<String>> readAbsentees(
            Parcel parcel)
    {
        int size = parcel.readInt();
        Map<String, List<String>> map = new HashMap<String, List<String>>(size);
        for(int i = 0; i < size; i++){
            String dateStr = parcel.readString();
            List<String> students = new ArrayList<>();
            parcel.readStringList(students);
            map.put(dateStr, students);
        }
        return map;
    }



    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AttendanceInput> CREATOR = new Parcelable.Creator<AttendanceInput>() {
        @Override
        public AttendanceInput createFromParcel(Parcel in) {
            return new AttendanceInput(in);
        }

        @Override
        public AttendanceInput[] newArray(int size) {
            return new AttendanceInput[size];
        }
    };

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(Date weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public List<Date> getHolidayList() {
        return holidayList;
    }

    public void setHolidayList(List<Date> holidayList) {
        this.holidayList = holidayList;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }

    public Map<String, List<String>> getAbsentees() {
        return absentees;
    }

    public void setAbsentees(Map<String, List<String>> absentees) {
        this.absentees = absentees;
    }
}
