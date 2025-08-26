package com.blis.customercity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.blis.customercity.data.OnlineRecord;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlin.jvm.internal.TypeReference;
import okhttp3.OkHttpClient;

public class AddFragment extends Fragment {

    private HashMap<String, String> recordFields = new HashMap<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView linearLayout = (ScrollView) inflater.inflate(R.layout.fragment_add, container, false);

        // Find views
        TextInputEditText companyNameInput = linearLayout.findViewById(R.id.company_name);
        ImageButton addItemButton = linearLayout.findViewById(R.id.add_item_button);
        LinearLayout itemInputFields = linearLayout.findViewById(R.id.item_input_fields);

        // Add item fields
        addItemButton.setOnClickListener(v -> {
            if(itemInputFields.getChildCount() >= 6) return;
            View newView = getLayoutInflater().inflate(R.layout.add_item_field, itemInputFields, false);

            // delete button
            ImageButton deleteButton = newView.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(v2 -> {
                if(itemInputFields.getChildCount() <= 1) return;
                itemInputFields.removeView(newView);
            });

            // Spinner
            Spinner itemSpinner = newView.findViewById(R.id.item_spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.default_item_names,
                    android.R.layout.simple_spinner_item
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            itemSpinner.setAdapter(adapter);

            // Input field hint
            TextInputLayout textInputLayout = newView.findViewById(R.id.text_input_layout);
            itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    textInputLayout.setHint(adapter.getItem(itemSpinner.getSelectedItemPosition()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });


            itemInputFields.addView(newView);
        });
        addItemButton.callOnClick();

        // back button
        Button backButton = linearLayout.findViewById(R.id.back_button);
        backButton.setOnClickListener( v -> {
            Main main = (Main) getActivity();
            if(main == null || !isAdded())return;
            main.goToCurrent();
        });

        // On save record
        Button saveButton = linearLayout.findViewById(R.id.save_button);
        TextView errorTextView = linearLayout.findViewById(R.id.error_text_view);
        saveButton.setOnClickListener(v -> {
            recordFields.clear();
            errorTextView.setText("");

            for (int i = 0; i < itemInputFields.getChildCount(); i++) {
                View thisView = itemInputFields.getChildAt(i);
                if(!(thisView instanceof LinearLayout)) continue;
                LinearLayout thisField = (LinearLayout) thisView;

                Spinner itemSpinner = thisField.findViewById(R.id.item_spinner);
                String fieldName = itemSpinner.getSelectedItem().toString();
                TextInputEditText textInput = thisField.findViewById(R.id.text_input_field);
                String inputString = textInput.getText().toString();
                if(recordFields.containsKey(fieldName)){
                    errorTextView.setText("不能使用重複項目");
                    return;
                }
                if(inputString.isEmpty()){
                    errorTextView.setText("不能留空白");
                    return;
                }
                recordFields.put(fieldName, inputString);
            }

            String companyName = companyNameInput.getText().toString();
            if(companyName.isEmpty()){
                errorTextView.setText("不能留空白");
                return;
            }
            OnlineRecord onlineRecord = new OnlineRecord();
            onlineRecord.setCompany_name_cn(companyName);
            onlineRecord.setServices_scope_cn(recordFields.getOrDefault("服務範圍", ""));
            onlineRecord.setService_hotline(recordFields.getOrDefault("電話號碼", ""));
            onlineRecord.setEmail(recordFields.getOrDefault("電郵地址", ""));
            onlineRecord.setAddress_cn(recordFields.getOrDefault("地址", ""));
            onlineRecord.setAdded_detail_cn(recordFields.getOrDefault("其他", ""));
            onlineRecord.setTips_cn(recordFields.getOrDefault("提示", ""));

            String addedRecords = FileHandler.loadFromFile(requireContext(), "addedRecords");

            ArrayList<OnlineRecord> onlineRecords = new ArrayList<>();
            if(!addedRecords.isEmpty()){
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<OnlineRecord>>() {}.getType();
                onlineRecords = gson.fromJson(addedRecords, listType);
            }

            onlineRecords.add(onlineRecord);

            Gson gson = new Gson();
            String jsonString = gson.toJson(onlineRecords);
            FileHandler.saveToFile(requireContext(), "addedRecords", jsonString);

            Toast.makeText(requireContext(),"儲存成功", Toast.LENGTH_SHORT).show();
        });

        return linearLayout;
    }
}
