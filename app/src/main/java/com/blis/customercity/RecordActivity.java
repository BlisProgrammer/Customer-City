package com.blis.customercity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blis.customercity.Data.DataAPI;
import com.blis.customercity.Data.OnlineRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecordActivity extends AppCompatActivity {
    private Toast savedToast;
    private final OkHttpClient client = new OkHttpClient();
    private boolean isSavedOnline = false;

    private void showToast(String text){
        if(savedToast != null){
            savedToast.cancel();
        }
        savedToast = Toast.makeText(RecordActivity.this, text, Toast.LENGTH_SHORT);
        savedToast.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_view);

        Intent intent = getIntent();
        OnlineRecord selectedRecord;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            selectedRecord = intent.getSerializableExtra("selected_record", OnlineRecord.class);
        } else {
            selectedRecord = (OnlineRecord) intent.getSerializableExtra("selected_record");
        }
        if(selectedRecord == null){
            return;
        }
        TextView recordViewCompany = findViewById(R.id.record_view_company);
        recordViewCompany.setText(DataConverter.companyIDToCompany(selectedRecord.getCompany_id(), getResources().openRawResource(R.raw.companies)));
        TextView recordViewCategory = findViewById(R.id.record_view_category);
        recordViewCategory.setText(String.format(
                "%s/%s",
                DataConverter.companyIDToSubCategory(selectedRecord.getCompany_id(), getResources().openRawResource(R.raw.sub_categories)),
                DataConverter.companyIDToCategory(selectedRecord.getCompany_id(), getResources().openRawResource(R.raw.categories))
        ));
        TextView recordViewDetails = findViewById(R.id.record_view_details);
        recordViewDetails.setText(selectedRecord.formatToString());

        // back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v->{
            Intent resultIntent = new Intent();
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

        // Save online button
        SharedPreferences loginInfo = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        Button saveOnlineButton = findViewById(R.id.save_button);
        if(!loggedIn) {
            saveOnlineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showToast("Login to save");
                }
            });
        }else{
            String idToken = loginInfo.getString("idToken", null);
            new Thread(()->{
                HashMap<String, ArrayList<OnlineRecord>> savedRecords = DataAPI.getSavedRecords(idToken);
                runOnUiThread(()->{
                    if(savedRecords.containsKey(selectedRecord.getId())){
                        saveOnlineButton.setText("Saved");
                        isSavedOnline = true;
                    }
                });
            }).start();
            saveOnlineButton.setOnClickListener(v -> {
                new Thread(()->{
                    boolean result = DataAPI.updateHistory(idToken, selectedRecord.getId());
                    runOnUiThread(()->{
                        if (result){
                            isSavedOnline = !isSavedOnline;
                        }
                        if(isSavedOnline){
                            saveOnlineButton.setText("Saved");
                            showToast("Saved Record");
                        }else{
                            saveOnlineButton.setText("Save");
                            showToast("Removed Record");
                        }
                    });
                }).start();
            });
        }
    }
}