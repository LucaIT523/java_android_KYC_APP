package com.miniai.miniai.confirmfragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.miniai.miniai.TextData;
import com.miniai.miniai.R;

import java.util.ArrayList;

public class ConfirmTextDataFragment extends Fragment {

    private AdapterConfirmTextData adapter;
    private ArrayList<TextData> textDataList = new ArrayList<TextData>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_confirm_text_data, container, false);
        ListView simpleList = (ListView) rootView.findViewById(R.id.textDataListView);
        adapter = new AdapterConfirmTextData(getContext(), textDataList);
        simpleList.setAdapter(adapter);
        return rootView;
    }
    public void setTextData(ArrayList<TextData> textData) {
        textDataList = textData;
        adapter.setData(textDataList);
        adapter.notifyDataSetChanged();
    }
}