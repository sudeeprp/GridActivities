package com.thinklearn.tide.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.HashMap;

public class Student implements Parcelable {

    private String id;

    private String firstName;

    private String surname;

    private Date birthDate;

    private String gender;

    private String grade;

    private String thumbnail;

    private String qualifier;

    private AcademicRecords academicRecords;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getQualifier() { return qualifier; }

    public void setQualifier(String qualifier) { this.qualifier = qualifier; }

    public void clearActivityRecords() {
        academicRecords = null;
    }
    public void setActivityAttribute(String subjectID, String chapterID, String activityID, String activityKey, String activityValue) {
        if(academicRecords == null) {
            academicRecords = new AcademicRecords();
        }
        academicRecords.setActivityAttribute(subjectID, chapterID, activityID, activityKey, activityValue);
    }

    public AcademicRecords getAcademicRecords() {
        return academicRecords;
    }

    public Student() { }

    protected Student(Parcel in) {
        id = in.readString();
        firstName = in.readString();
        surname = in.readString();
        long tmpBirthDate = in.readLong();
        birthDate = tmpBirthDate != -1 ? new Date(tmpBirthDate) : null;
        gender = in.readString();
        grade = in.readString();
        thumbnail = in.readString();
        qualifier = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(firstName);
        dest.writeString(surname);
        dest.writeLong(birthDate != null ? birthDate.getTime() : -1L);
        dest.writeString(gender);
        dest.writeString(grade);
        dest.writeString(thumbnail);
        dest.writeString(qualifier);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Student> CREATOR = new Parcelable.Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel in) {
            return new Student(in);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };
}