package com.miniai.miniai.confirmfragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.miniai.miniai.ImageData;
import com.miniai.miniai.R;

import java.util.ArrayList;

public class ConfirmImageFragment extends Fragment {
    private AdapterConfirmImages adapter;
    private ArrayList<ImageData> imageDataList = new ArrayList<ImageData>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_confirm_image, container, false);
        ListView simpleList = (ListView) rootView.findViewById(R.id.imageListView);
        adapter = new AdapterConfirmImages(getContext(), imageDataList);
        simpleList.setAdapter(adapter);
        return rootView;
    }
    public void setImageData(ArrayList<ImageData> data) {
        imageDataList = data;
        adapter.setData(imageDataList);
        adapter.notifyDataSetChanged();
    }
    public ArrayList<ImageData> getImageDataList(){
        return imageDataList;
    }
    public Bitmap getImage(String keyType){
        for(int i = 0; i < imageDataList.size(); i++){
            String imageType = imageDataList.get(i).getType();
            if(imageType.equals(keyType)){
                return imageDataList.get(i).getImage();
            }
        }
        return null;
    }
}