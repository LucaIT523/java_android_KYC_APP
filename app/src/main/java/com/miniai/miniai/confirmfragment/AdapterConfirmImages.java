package com.miniai.miniai.confirmfragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.miniai.miniai.ImageData;
import com.miniai.miniai.R;

import java.util.ArrayList;

public class AdapterConfirmImages extends BaseAdapter {
    private Context context;
    private LayoutInflater inflter;
    private ArrayList<ImageData> imageDataList;
    public AdapterConfirmImages(Context ctx, ArrayList<ImageData> data) {
        this.context = ctx;
        inflter = (LayoutInflater.from(ctx));
        imageDataList = data;
    }

    @Override
    public int getCount() {
        return imageDataList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflter.inflate(R.layout.list_item_image, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.itemImageView);
        TextView typeTextView = (TextView) view.findViewById(R.id.typeTextView);
        ImageData imageData = imageDataList.get(position);
        imageView.setImageBitmap(imageData.getImage());
        // Set the height dynamically based on the image and layout width
        int layoutWidth = parent.getWidth(); // Or view.getWidth(), depends on your layout
        int imageHeight = imageData.getImage().getHeight();
        int imageWidth = imageData.getImage().getWidth();

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        if (layoutWidth >= imageWidth) {
            layoutParams.height = imageHeight;
        } else {
            layoutParams.height = imageHeight * layoutWidth / imageWidth;
        }
        imageView.setLayoutParams(layoutParams);

        typeTextView.setText(imageData.getType());
        return view;
    }
    public void setItem(ImageData imageData){
        imageDataList.add(imageData);
    }
    public void setData(ArrayList<ImageData> imageDataList){
        this.imageDataList = imageDataList;
    }
}