package com.miniai.miniai.confirmfragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.miniai.miniai.R;
import com.miniai.miniai.TextData;

import java.util.ArrayList;

public class AdapterConfirmTextData extends BaseAdapter {
    private Context context;
    private LayoutInflater inflter;

    private ArrayList<TextData> textDataList;

    public AdapterConfirmTextData(Context ctx, ArrayList<TextData> textDataList) {
        this.context = ctx;
        inflter = (LayoutInflater.from(ctx));
        this.textDataList = textDataList;
    }

    @Override
    public int getCount() {
        return textDataList.size();
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
        View view = inflter.inflate(R.layout.list_item_text, null);
        TextView keyTextView = (TextView) view.findViewById(R.id.keyTextView);
        TextView valueTextView = (TextView) view.findViewById(R.id.valueTextView);
        TextData textData = textDataList.get(position);
        keyTextView.setText(textData.getKeyField());
        valueTextView.setText(textData.getValue());
        return view;
    }
    public void setItem(TextData textData){
        textDataList.add(textData);
    }
    public void setData(ArrayList<TextData> textDataList){
        this.textDataList = textDataList;
    }
}