package com.blis.customercity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;

public class DirectoryFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_directory, container, false);
        ListView mainListView = linearLayout.findViewById(R.id.directory_list_view);

        ArrayList<String> allCompanies = DataConverter.getAllCompanies(getResources().openRawResource(R.raw.companies));
        Collections.sort(allCompanies);

        MySectionedAdapter adapter = new MySectionedAdapter(requireContext(), android.R.layout.simple_list_item_1, allCompanies);
        mainListView.setAdapter(adapter);

        return linearLayout;
    }
}
