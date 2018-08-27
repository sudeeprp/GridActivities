package com.thinklearn.tide.activitydriver;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
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
import com.thinklearn.tide.interactor.ClassroomInteractor;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;

/**
 * A fragment representing a single Student detail screen.
 * This fragment is either contained in a {@link StudentListActivity}
 * in two-pane mode (on tablets) or a {@link StudentDetailActivity}
 * on handsets.
 */
public class StudentDetailFragment extends Fragment {

    private static final int REQUEST_CAMERA = 0;
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String STUDENT = "student";
    private Student mItem;
    private ImageView imageView;
    private RecyclerView.Adapter adapter;
    private Bitmap newBitmap,oldBitmap;
    private Button btSave,btCancel;


    public StudentDetailFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(STUDENT)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = getArguments().getParcelable(STUDENT);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getFirstName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.student_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView)rootView.findViewById(R.id.tvStudentName)).setText(" " + mItem.getFirstName() + " " + mItem.getSurname());
            ((TextView) rootView.findViewById(R.id.tvId)).setText("" + mItem.getId());
            ((TextView) rootView.findViewById(R.id.tvFirstName)).setText(mItem.getFirstName());
            ((TextView) rootView.findViewById(R.id.tvSurname)).setText(mItem.getSurname());
            ((TextView) rootView.findViewById(R.id.tvClass)).setText(mItem.getGrade());
            ((TextView) rootView.findViewById(R.id.tvYear)).setText(""); //TODO: Figure out academic year
            ((TextView) rootView.findViewById(R.id.tvGender)).setText(mItem.getGender());
            btSave=rootView.findViewById(R.id.save);
            btSave.setEnabled(false);
            btCancel=rootView.findViewById(R.id.cancel);
            btCancel.setEnabled(false);
            imageView = rootView.findViewById(R.id.ivStudentCapturedImage);
             rootView.findViewById(R.id.btchangePhoto).setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                     startActivityForResult(intent, REQUEST_CAMERA);
                 }
             });


             rootView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     if(newBitmap!=null) {
                         setNewThumbnail(newBitmap);
                         ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         newBitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                         byte[] b = baos.toByteArray();
                         ClassroomInteractor.set_student_thumbnail
                                 (mItem.getId(), Base64.encodeToString(b, Base64.DEFAULT));
                     }
                     btCancel.setEnabled(false);
                 }
             });

             rootView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     if(oldBitmap!=null) {
                         setNewThumbnail(oldBitmap);
                         imageView.setImageBitmap(oldBitmap);
                     }else{
                         imageView.setImageResource(R.drawable.student);
                     }
                     newBitmap=null;
                 }
             });

            if(mItem.getThumbnail() != null) {
                byte[] decodedString = Base64.decode(mItem.getThumbnail(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                oldBitmap= decodedByte;
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
        newBitmap = (Bitmap) data.getExtras().get("data");
        imageView.setImageBitmap(newBitmap);
        btSave.setEnabled(true);
        btCancel.setEnabled(true);
    }

    private void setNewThumbnail(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        mItem.setThumbnail(encoded);
        adapter.notifyDataSetChanged();
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }
}
