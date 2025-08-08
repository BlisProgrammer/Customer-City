package com.blis.customercity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class SavedFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_saved, container, false);
        Object savedObject = FileHandler.loadObjectFromFile(requireContext(), "saved_list");

        if(savedObject != null) {
            ArrayList<Record> savedList = (ArrayList<Record>) savedObject;
            ListView listView = linearLayout.findViewById(R.id.saved_view_list);
            TwoLineAdapter adapter = new TwoLineAdapter(requireContext(), savedList);
            listView.setAdapter(adapter);
        }

//        if(savedObject != null){
//            ArrayList<Record> savedList = (ArrayList<Record>) savedObject;
//            if(!savedList.isEmpty()){
//                TextView textView = linearLayout.findViewById(R.id.saved_view);
//                textView.setText(savedList.get(0).formatToString());
//            }
//        }
        return linearLayout;
    }
}