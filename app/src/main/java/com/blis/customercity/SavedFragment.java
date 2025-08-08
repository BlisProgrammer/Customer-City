package com.blis.customercity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
        @SuppressWarnings("unchecked")
        ArrayList<Record> savedList = (ArrayList<Record>) savedObject;
        if(savedList.isEmpty()) return linearLayout;

        TextView textView = linearLayout.findViewById(R.id.saved_view_tips);
        textView.setText(R.string.long_click_item_to_remove);

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
                            Toast toast = new Toast(requireContext());
                            toast.setText("Item removed");
                            toast.show();
                            dialog.dismiss();
                        },
                        (dialog, which) -> dialog.dismiss()

                );
                return false;
            }
        });
        return linearLayout;
    }
}