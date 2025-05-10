package com.miniai.miniai.confirmfragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ListView;

import com.miniai.miniai.FaceConfirmActivity;
import com.miniai.miniai.R;
import com.miniai.miniai.TextData;

import java.util.ArrayList;

public class ConfirmFaceFragment extends Fragment {

    private Context context = null;
    private int faceID = 0;
    private AdapterConfirmTextData adapter;
    private ArrayList<TextData> textDataList = new ArrayList<TextData>();
    private ImageView sourceImageView;
    private int layoutWidth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_confirm_face, container, false);
        sourceImageView = (ImageView) rootView.findViewById(R.id.confirmImageView);
        ListView simpleList = (ListView) rootView.findViewById(R.id.textDataListView);
        adapter = new AdapterConfirmTextData(getContext(), textDataList);
        simpleList.setAdapter(adapter);
        layoutWidth = container.getWidth();
        sourceImageView.setOnClickListener(v -> {
            if (context != null) {
                ((FaceConfirmActivity)context).setBaseData(faceID);
            }
        });
        return rootView;
    }
    public void setTextData(ArrayList<TextData> textData) {
        textDataList = textData;
        adapter.setData(textDataList);
        adapter.notifyDataSetChanged();
    }

    // Method to set the image in the ImageView
    public void setImage(Bitmap bitmap) {
        if (bitmap != null) {
            sourceImageView.setImageBitmap(bitmap);   // Display the image in the ImageView
            ViewGroup.LayoutParams layoutParams = sourceImageView.getLayoutParams();
            int imageHeight = bitmap.getHeight();
            int imageWidth = bitmap.getWidth();
            if (layoutWidth <= imageWidth) {
                layoutParams.height = imageHeight;
            } else {
                layoutParams.height = imageHeight * layoutWidth / imageWidth;
            }
            if(layoutParams.height > 500)
                layoutParams.height = 500;
            sourceImageView.setLayoutParams(layoutParams);
        }
    }
    public void setContext(Context context, int faceID){
        this.context = context;
        this.faceID =faceID;
    }
}