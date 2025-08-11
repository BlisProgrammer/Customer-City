package com.blis.customercity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class SavedFragment extends Fragment {
    private static ArrayList<Record> savedList = new ArrayList<>();
    private static TwoLineAdapter adapter;
    private ActivityResultLauncher<Intent> recordActivityResultLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Object savedObject = FileHandler.loadObjectFromFile(requireContext(), "saved_list");
                            if (savedObject == null) return;

                            assert getView() != null;
                            ListView listView = getView().findViewById(R.id.saved_view_list);
                            if (savedObject == null) return;
                            savedList = (ArrayList<Record>) savedObject;
                            adapter = new TwoLineAdapter(requireContext(), savedList);
                            listView.setAdapter(adapter);
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            // Handle cancellation
                        }
                    }
                });

    }

    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_saved, container, false);
        Object savedObject = FileHandler.loadObjectFromFile(requireContext(), "saved_list");

        ListView listView = linearLayout.findViewById(R.id.saved_view_list);
        if (savedObject == null) return linearLayout;
        savedList = (ArrayList<Record>) savedObject;
        if(savedList.isEmpty()) return linearLayout;

        TextView textView = linearLayout.findViewById(R.id.saved_view_tips);
        textView.setText(R.string.long_click_item_to_remove);

        adapter = new TwoLineAdapter(requireContext(), savedList);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
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
            return true;
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent recordIntent = new Intent(getActivity(), RecordActivity.class);
            recordIntent.putExtra("selected_record", savedList.get(position));
            recordActivityResultLauncher.launch(recordIntent);
        });
        return linearLayout;
    }
}