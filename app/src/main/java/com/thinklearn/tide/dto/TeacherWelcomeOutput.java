package com.thinklearn.tide.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class TeacherWelcomeOutput implements Parcelable {

    private String selection;

    public TeacherWelcomeOutput(String selection) {
        this.selection = selection;
    }

    protected TeacherWelcomeOutput(Parcel in) {
        selection = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(selection);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TeacherWelcomeOutput> CREATOR = new Parcelable.Creator<TeacherWelcomeOutput>() {
        @Override
        public TeacherWelcomeOutput createFromParcel(Parcel in) {
            return new TeacherWelcomeOutput(in);
        }

        @Override
        public TeacherWelcomeOutput[] newArray(int size) {
            return new TeacherWelcomeOutput[size];
        }
    };
}