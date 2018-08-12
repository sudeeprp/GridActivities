package com.thinklearn.tide.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AttendanceInput implements Parcelable {

    private Long id;

    private Date weekStartDate;

    private List<Date> holidayList;

    private Date attendanceDate;

    private List<Long> absentList;

    private List<Student> studentList;

    protected AttendanceInput(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readLong();
        long tmpWeekStartDate = in.readLong();
        weekStartDate = tmpWeekStartDate != -1 ? new Date(tmpWeekStartDate) : null;
        if (in.readByte() == 0x01) {
            holidayList = new ArrayList<Date>();
            in.readList(holidayList, Date.class.getClassLoader());
        } else {
            holidayList = null;
        }
        long tmpAttendanceDate = in.readLong();
        attendanceDate = tmpAttendanceDate != -1 ? new Date(tmpAttendanceDate) : null;
        if (in.readByte() == 0x01) {
            absentList = new ArrayList<Long>();
            in.readList(absentList, Long.class.getClassLoader());
        } else {
            absentList = null;
        }
        if (in.readByte() == 0x01) {
            studentList = new ArrayList<Student>();
            in.readList(studentList, Student.class.getClassLoader());
        } else {
            studentList = null;
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
        dest.writeLong(attendanceDate != null ? attendanceDate.getTime() : -1L);
        if (absentList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(absentList);
        }
        if (studentList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(studentList);
        }
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

    public Date getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(Date attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public List<Long> getAbsentList() {
        return absentList;
    }

    public void setAbsentList(List<Long> absentList) {
        this.absentList = absentList;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }
}
