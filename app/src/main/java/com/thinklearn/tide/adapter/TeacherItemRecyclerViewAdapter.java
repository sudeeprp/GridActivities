package com.thinklearn.tide.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thinklearn.tide.activitydriver.R;
import com.thinklearn.tide.dto.Teacher;

import java.util.List;

public class TeacherItemRecyclerViewAdapter extends RecyclerView.Adapter<TeacherItemRecyclerViewAdapter.TeacherviewHolder>  {

    private int selectedPosition = -1;

    private final Context context;

    private final List<Teacher> teacherWelcomeInputList;

    private TeacherSelectedClickListener teacherSelectedClickListener;

    public TeacherItemRecyclerViewAdapter(Context context, List<Teacher> teacherWelcomeInputList, TeacherSelectedClickListener teacherSelectedClickListener) {
        this.context = context;
        this.teacherWelcomeInputList = teacherWelcomeInputList;
        this.teacherSelectedClickListener = teacherSelectedClickListener;
    }

    @NonNull
    @Override
    public TeacherviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View teacherViewholder = LayoutInflater.from(parent.getContext()).inflate(R.layout.teacher_list_content,parent,false);
        TeacherviewHolder holder = new TeacherviewHolder(teacherViewholder);
        if(teacherWelcomeInputList.size() == 1) {
            selectItem(holder, 0);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final TeacherviewHolder holder, int position) {
        holder.teacherName.setText(teacherWelcomeInputList.get(position).getTeacherName());
        if(teacherWelcomeInputList.get(position).getThumbnail() != null) {
            byte[] decodedString = Base64.decode(teacherWelcomeInputList.get(position).getThumbnail(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(Resources.getSystem(),decodedByte);
            roundedBitmapDrawable.setCircular(true);
            holder.imageView.setImageDrawable(roundedBitmapDrawable);
        } else {
            holder.imageView.setImageResource(R.drawable.student);
        }
        if(selectedPosition==position)
            holder.itemView.setBackgroundColor(Color.parseColor("#cccccc"));
        else
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if(position == selectedPosition)
                    return;
                selectItem(holder, position);
                notifyDataSetChanged();
            }
        });
    }
    private void selectItem(TeacherviewHolder holder, int position) {
        selectedPosition = position;
        holder.itemView.setTag(teacherWelcomeInputList.get(position));
        teacherSelectedClickListener.onTeacherSelected(holder.itemView,position);
    }
    @Override
    public int getItemCount() {
        return teacherWelcomeInputList.size();
    }

    static class TeacherviewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView teacherName;

        public TeacherviewHolder(View itemView) {
            super(itemView);
            imageView= itemView.findViewById(R.id.ivTeacherImage);
            teacherName = itemView.findViewById(R.id.tvTeacherName);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public interface TeacherSelectedClickListener {
        void onTeacherSelected(View v,int position);
    }
}
