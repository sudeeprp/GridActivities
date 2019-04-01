package com.thinklearn.tide.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.thinklearn.tide.activitydriver.R;
import com.thinklearn.tide.dto.Student;

import java.util.List;

public class StudentGridAdapter extends BaseAdapter {

    private Context mContext;

    private List<Student> studentList;

    private Student selectedStudent;

    private StudentSelectedListener studentSelecttedListener;

    public StudentGridAdapter(Context mContext, List<Student> studentList,StudentSelectedListener studentSelectedListener) {
        this.mContext = mContext;
        this.studentList = studentList;
        this.studentSelecttedListener = studentSelectedListener;
    }

    @Override
    public int getCount() {
        return studentList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View gridView;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            gridView = inflater.inflate(R.layout.student_grid_item, null);
        } else {
            gridView = convertView;
        }
        Student currentStudent = studentList.get(position);
        if (selectedStudent != null && selectedStudent.getId().equals(currentStudent.getId())) {
            gridView.setBackgroundColor(mContext.getResources().getColor(R.color.colorLimeGreen));
        } else {
            gridView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }
        TextView studentName = gridView.findViewById(R.id.tvStudentName);
        studentName.setText(currentStudent.getFirstName()+" "+currentStudent.getSurname());

        // set image based on selected text
        ImageView studentImage =  gridView
                .findViewById(R.id.ivStudentImage);
        Bitmap decodedByte = null;
        String thumbnail = studentList.get(position).getThumbnail();
        if( thumbnail != null && !thumbnail.isEmpty()) {
            byte[] decodedString = Base64.decode(currentStudent.getThumbnail(), Base64.DEFAULT);
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } else {
            decodedByte = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.student);
        }
        Bitmap scaledPicture = Bitmap.createScaledBitmap(decodedByte, 100, 100, true);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(Resources.getSystem(),scaledPicture);
        roundedBitmapDrawable.setCircular(true);
        roundedBitmapDrawable.setCornerRadius(10);
        studentImage.setImageDrawable(roundedBitmapDrawable);

        gridView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectedStudent = studentList.get(position);
                studentSelecttedListener.onStudentSelected(v,position);
                notifyDataSetChanged();
            }
        });
        return gridView;
    }

    public interface StudentSelectedListener {
        void onStudentSelected(View v,int position);
    }

    public Student getSelectedStudent() {
        return selectedStudent;
    }
}
