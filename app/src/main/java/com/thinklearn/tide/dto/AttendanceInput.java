package com.thinklearn.tide.dto;

import android.os.Parcel;
import android.os.Parcelable;

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

    private Map<String, List<String>> presentAM;
    private Map<String, List<String>> presentPM;

    private List<String> presentStudentsAM;
    private List<String> presentStudentsPM;

    public AttendanceInput() {

    }

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
        if (in.readByte() == 0x01) {
            studentList = new ArrayList<Student>();
            in.readList(studentList, Student.class.getClassLoader());
        } else {
            studentList = null;
        }
        if (in.readByte() == 0x01) {
            presentAM = readPresent(in);
        } else {
            presentAM = null;
        }
        if (in.readByte() == 0x01) {
            presentPM = readPresent(in);
        } else {
            presentPM = null;
        }
        if (in.readByte() == 0x01) {
            presentStudentsAM = new ArrayList<String>();
            in.readList(presentStudentsAM, String.class.getClassLoader());
        } else {
            presentStudentsAM = null;
        }
        if (in.readByte() == 0x01) {
            presentStudentsPM = new ArrayList<String>();
            in.readList(presentStudentsPM, String.class.getClassLoader());
        } else {
            presentStudentsPM = null;
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
        if (presentAM == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            writePresentToParcel(presentAM,dest, flags);
        }
        if (presentPM == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            writePresentToParcel(presentPM, dest, flags);
        }
        if (presentStudentsAM == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(presentStudentsAM);
        }
        if (presentStudentsPM == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(presentStudentsPM);
        }
    }

    // For writing to a Parcel
    public void writePresentToParcel(
            Map<String, List<String>> present, Parcel parcel, int flags) {
        parcel.writeInt(present.size());
        for(Map.Entry<String, List<String>> e : present.entrySet()){
            parcel.writeString(e.getKey());
            parcel.writeStringList(e.getValue());
        }
    }

    // For reading from a Parcel
    public Map<String, List<String>> readPresent(
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

    public Map<String, List<String>> getPresentAM() {
        return presentAM;
    }
    public Map<String, List<String>> getPresentPM() {
        return presentPM;
    }

    public void setPresentAM(Map<String, List<String>> presentAM) { this.presentAM = presentAM; }
    public void setPresentPM(Map<String, List<String>> presentPM) { this.presentPM = presentPM; }

    public List<String> getPresentStudentsAM() {

        return presentStudentsAM;
    }
    public List<String> getPresentStudentsPM() {
        return presentStudentsPM;
    }
    public void setPresentStudentsAM(List<String> presentStudentsAM) { this.presentStudentsAM = presentStudentsAM; }
    public void setPresentStudentsPM(List<String> presentStudentsPM) { this.presentStudentsPM = presentStudentsPM; }
}
