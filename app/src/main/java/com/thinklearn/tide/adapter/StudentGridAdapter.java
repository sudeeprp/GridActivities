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

    public StudentGridAdapter(Context mContext, List<Student> studentList) {
        this.mContext = mContext;
        this.studentList = studentList;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridView;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            gridView = new View(mContext);

            gridView = inflater.inflate(R.layout.student_grid_item,null);

            TextView studentName = gridView.findViewById(R.id.tvStudentName);

            studentName.setText(studentList.get(position).getFirstName()+" "+studentList.get(position).getSurname());

            // set image based on selected text
            ImageView studentImage =  gridView
                    .findViewById(R.id.ivStudentImage);

            if(studentList.get(position).getThumbnail() != null) {
                byte[] decodedString = Base64.decode(studentList.get(position).getThumbnail(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(Resources.getSystem(),decodedByte);
                roundedBitmapDrawable.setCircular(true);
                studentImage.setImageDrawable(roundedBitmapDrawable);
            } else {
                Bitmap decodedByte = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.student);
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(Resources.getSystem(),decodedByte);
                roundedBitmapDrawable.setCircular(true);
                studentImage.setImageDrawable(roundedBitmapDrawable);
            }
        } else {
            gridView = convertView;
        }


        return gridView;
    }

   /* public Integer[] mThumbIds = {
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid, R.drawable.attendance_grid,
            R.drawable.attendance_grid
    };*/
}
