package com.thinklearn.tide.activitydriver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;




import com.thinklearn.tide.dto.Student;
import com.thinklearn.tide.interactor.ClassroomInteractor;

import java.util.List;

/**
 * An activity representing a list of Students. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link StudentDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StudentListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        String selectedGrade = getIntent().getStringExtra("selectedGrade");
        String displayGrade = getString(getResources().getIdentifier("grade" + selectedGrade,
                        "string", getPackageName()));
        String selectedGender = getIntent().getStringExtra("selectedGender");
        String displayGender = getString(
                getResources().getIdentifier(selectedGender, "string", getPackageName()));

        Toolbar toolbar = (Toolbar) findViewById(R.id.studentGradeSelectionBar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        ((TextView) findViewById(R.id.selectedClass)).setText(": " +displayGrade);
        ((TextView) findViewById(R.id.selectedGender)).setText(": " +displayGender);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (findViewById(R.id.assessment_record_activity) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.student_list);
        assert recyclerView != null;
        List<Student> studentInputList = ClassroomInteractor.filterStudents(selectedGrade, selectedGender, false);
        setupRecyclerView((RecyclerView) recyclerView, studentInputList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, List<Student> students) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, students, mTwoPane));
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final StudentListActivity mParentActivity;
        private final List<Student> mValues;
        private final boolean mTwoPane;
        private int selectedPosition;

        SimpleItemRecyclerViewAdapter(StudentListActivity parent,
                                      List<Student> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.student_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Student studentInput = mValues.get(position);
            holder.tvStudentName.setText(studentInput.getFirstName() + " " + mValues.get(position).getSurname());
            if(studentInput.getThumbnail() != null) {
                byte[] decodedString = Base64.decode(studentInput.getThumbnail(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.tvStudentImage.setImageBitmap(decodedByte);
            } else {
                holder.tvStudentImage.setImageResource(R.drawable.student);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedPosition = position;
                    notifyDataSetChanged();
                    Student item = (Student) view.getTag();
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(StudentDetailFragment.STUDENT_ID, item.getId());
                        StudentDetailFragment fragment = new StudentDetailFragment();
                        fragment.setAdapter(SimpleItemRecyclerViewAdapter.this);
                        fragment.setArguments(arguments);
                        mParentActivity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.assessment_record_activity, fragment)
                                .commit();
                    } else {
                        Context context = view.getContext();
                        Intent intent = new Intent(context, StudentDetailActivity.class);
                        intent.putExtra(StudentDetailFragment.STUDENT_ID, item.getId());

                        context.startActivity(intent);
                    }
                }
            });
            holder.itemView.setTag(mValues.get(position));
            if(selectedPosition==position)
                holder.itemView.setBackgroundColor(Color.parseColor("#cccccc"));
            else
                holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

           final TextView tvStudentName;
           final ImageView tvStudentImage;

            ViewHolder(View view) {
                super(view);
                tvStudentName =  view.findViewById(R.id.tvStudentname);
                tvStudentImage = view.findViewById(R.id.ivStudentImage);

            }
        }
    }
}
