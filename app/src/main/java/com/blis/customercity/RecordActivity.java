package com.blis.customercity;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RecordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_view);

        Intent intent = getIntent();
        int ID = intent.getIntExtra("selected_record_id", -1);
        if(FindFragment.selectedRecords.isEmpty()){
            return;
        }
        Record selectedRecord = FindFragment.selectedRecords.get(ID);

        TextView recordViewCompany = findViewById(R.id.record_view_company);
        recordViewCompany.setText(selectedRecord.company);
        TextView recordViewCategory = findViewById(R.id.record_view_category);
        recordViewCategory.setText(String.format("%s/%s", selectedRecord.sub_category, selectedRecord.sub_category));
        TextView recordViewDetails = findViewById(R.id.record_view_details);
        recordViewDetails.setText(selectedRecord.formatToString());

        // save button
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            Object savedObject = FileHandler.loadObjectFromFile(this, "saved_list");
            ArrayList<Record> savedArraylist = new ArrayList<>();

            if(savedObject != null){
                savedArraylist = (ArrayList<Record>) savedObject;
            }
            savedArraylist.add(selectedRecord);
            FileHandler.saveObjectToFile(this, "saved_list", savedArraylist);
            Toast toast = new Toast(this);
            toast.setText("Record saved");
            toast.show();
        });

        // back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v->{
            RecordActivity.this.finish();
        });
    }
}