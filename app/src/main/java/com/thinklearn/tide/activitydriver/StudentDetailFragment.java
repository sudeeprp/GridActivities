package com.thinklearn.tide.activitydriver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.thinklearn.tide.dto.Student;
import com.thinklearn.tide.interactor.ClassroomDBInteractor;
import com.thinklearn.tide.interactor.ClassroomInteractor;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static java.lang.Math.min;

/**
 * A fragment representing a single Student detail screen.
 * This fragment is either contained in a {@link StudentListActivity}
 * in two-pane mode (on tablets) or a {@link StudentDetailActivity}
 * on handsets.
 */
public class StudentDetailFragment extends Fragment {

    private static final int REQUEST_CAMERA = 0;
    public static final String STUDENT_ID = "student_id";
    private Student mItem;
    private ImageView imageView;
    private RecyclerView.Adapter adapter;

    public StudentDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(STUDENT_ID)) {
            String studentID = getArguments().getString(STUDENT_ID);
            if(studentID != null) {
                mItem = ClassroomInteractor.get_student(studentID);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.student_detail, container, false);

        if (mItem != null) {
            ((TextView)rootView.findViewById(R.id.tvStudentName)).setText(" " + mItem.getFirstName() + " " + mItem.getSurname());
            ((TextView) rootView.findViewById(R.id.tvId)).setText("" + mItem.getId());
            ((TextView) rootView.findViewById(R.id.tvFirstName)).setText(mItem.getFirstName());
            ((TextView) rootView.findViewById(R.id.tvSurname)).setText(mItem.getSurname());
            ((TextView) rootView.findViewById(R.id.tvClass)).setText(mItem.getGrade());
            Context context = getActivity();
            if(context != null) {
                ((TextView) rootView.findViewById(R.id.tvDateOfBirth)).setText
                        (getLocalDateOfBirth(context, mItem.getBirthDate()));
                ((TextView) rootView.findViewById(R.id.tvGender)).setText
                        (getLocalGender(context, mItem.getGender()));
            }
            imageView = rootView.findViewById(R.id.ivStudentCapturedImage);
             rootView.findViewById(R.id.btchangePhoto).setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                     startActivityForResult(intent, REQUEST_CAMERA);
                 }
                 });

            Button assessButton = rootView.findViewById(R.id.button);
            assessButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AssessmentRecordActivity.class);
                    intent.putExtra(StudentDetailFragment.STUDENT_ID, mItem.getId());
                    startActivity(intent);
                }
            });

            if(mItem.getThumbnail() != null) {
                byte[] decodedString = Base64.decode(mItem.getThumbnail(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(decodedByte);
            }
        }
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        String IMAGE_DATA_KEY = "data";
        if(data.getExtras().containsKey(IMAGE_DATA_KEY)) {
            Bitmap newBitmap = (Bitmap) data.getExtras().get(IMAGE_DATA_KEY);
            imageView.setImageBitmap(newBitmap);
            setNewThumbnail(newBitmap);
        }
    }

    private String getLocalDateOfBirth(Context context, Date birthDate) {
        String localBirthDate = "";
        if(birthDate != null) {
            DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
            localBirthDate = dateFormat.format(birthDate);
        }
        return localBirthDate;
    }
    private String getLocalGender(Context context, String gender) {
        String localGender = "";
        int genderResId = getResources().getIdentifier(gender, "string", context.getPackageName());
        if(genderResId != 0) {
            localGender = getString(genderResId);
        }
        return localGender;
    }
    private Bitmap getThumbnail(Bitmap bitmap) {
        int thumb_height = 100;
        double aspect_ratio = (double)bitmap.getWidth() / (double)bitmap.getHeight();
        int thumb_width = (int)(aspect_ratio * thumb_height);
        return Bitmap.createScaledBitmap(bitmap, thumb_width, thumb_height, true);
    }

    private void setNewThumbnail(Bitmap bitmap) {
        Bitmap thumbnail = getThumbnail(bitmap);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] byteArray = stream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        ClassroomDBInteractor.set_student_thumbnail(mItem.getId(), encoded);
        mItem.setThumbnail(encoded);
        adapter.notifyDataSetChanged();
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }
}
