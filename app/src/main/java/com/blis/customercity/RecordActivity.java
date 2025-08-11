package com.blis.customercity;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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
        Record selectedRecord;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            selectedRecord = intent.getSerializableExtra("selected_record", Record.class);
        } else {
            selectedRecord = (Record) intent.getSerializableExtra("selected_record");
        }
        if(selectedRecord == null){
            return;
        }
//        if(FindFragment.selectedRecords.isEmpty()){
//            return;
//        }
//        Record selectedRecord = FindFragment.selectedRecords.get(ID);

        TextView recordViewCompany = findViewById(R.id.record_view_company);
        recordViewCompany.setText(selectedRecord.company);
        TextView recordViewCategory = findViewById(R.id.record_view_category);
        recordViewCategory.setText(String.format("%s/%s", selectedRecord.category, selectedRecord.sub_category));
        TextView recordViewDetails = findViewById(R.id.record_view_details);
        recordViewDetails.setText(selectedRecord.formatToString());

        // save button
        Button saveButton = findViewById(R.id.save_button);

        Object savedObject = FileHandler.loadObjectFromFile(this, "saved_list");
        ArrayList<Record> savedArraylist;
        if(savedObject != null){
            savedArraylist = (ArrayList<Record>) savedObject;
        } else {
            savedArraylist = new ArrayList<>();
        }
        if(savedArraylist.contains(selectedRecord)){
            saveButton.setText("Saved");
        }
        saveButton.setOnClickListener(v -> {
            Object savedObject2 = FileHandler.loadObjectFromFile(this, "saved_list");
            ArrayList<Record> savedArraylist2;
            if(savedObject2 != null){
                savedArraylist2 = (ArrayList<Record>) savedObject2;
            } else {
                savedArraylist2 = new ArrayList<>();
            }
            String displayText;
            if(savedArraylist2.contains(selectedRecord)){
                savedArraylist2.remove(selectedRecord);
                displayText = "Record removed";
                saveButton.setText("Save");
            }else{
                savedArraylist2.add(selectedRecord);
                displayText = "Record saved";
                saveButton.setText("Saved");
            }
            FileHandler.saveObjectToFile(this, "saved_list", savedArraylist2);
            Toast toast = new Toast(this);
            toast.setText(displayText);
            toast.show();
        });

        // back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v->{
            Intent resultIntent = new Intent();
            resultIntent.putExtra("key", "value");
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        // share button
        Button shareButton = findViewById(R.id.share_button);
        shareButton.setOnClickListener(v->{
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, selectedRecord.getShareString());
            Intent chooser = Intent.createChooser(shareIntent, "Share with...");
            startActivity(chooser);
        });
    }
}