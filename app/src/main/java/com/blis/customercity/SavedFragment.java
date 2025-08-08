package com.blis.customercity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

        ListView listView = linearLayout.findViewById(R.id.saved_view_list);
        if (savedObject == null) return linearLayout;
        ArrayList<Record> savedList = (ArrayList<Record>) savedObject;
        TwoLineAdapter adapter = new TwoLineAdapter(requireContext(), savedList);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ConfirmationDialog.showConfirmationDialog(
                        requireContext(),
                        "Confirm Action",
                        "Delete saved record? ",
                        (dialog, which) -> {
                            savedList.remove(position);
                            FileHandler.saveObjectToFile(requireContext(), "saved_list", savedList);
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        },
                        (dialog, which) -> {
                            dialog.dismiss();
                        }

                );
                return false;
            }
        });
        return linearLayout;
    }
}