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
        TextView recordViewCompany = findViewById(R.id.record_view_company);
        recordViewCompany.setText(selectedRecord.company);
        TextView recordViewCategory = findViewById(R.id.record_view_category);
        recordViewCategory.setText(String.format("%s/%s", selectedRecord.category, selectedRecord.sub_category));
        TextView recordViewDetails = findViewById(R.id.record_view_details);
        recordViewDetails.setText(selectedRecord.formatToString());

        // save button
        Button saveButton = findViewById(R.id.save_button);

        new Thread(()->{
            Object savedObject = FileHandler.loadObjectFromFile(this, "saved_list");
            ArrayList<Record> savedArraylist;
            if(savedObject != null){
                savedArraylist = (ArrayList<Record>) savedObject;
            } else {
                savedArraylist = new ArrayList<>();
            }
            if(savedArraylist.contains(selectedRecord)){
                saveButton.post(()->{
                    saveButton.setText("Saved");
                });
            }
        }).start();
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
            if(savedToast != null){
                savedToast.cancel();
            }
            savedToast = Toast.makeText(this, displayText, Toast.LENGTH_SHORT);
            savedToast.show();
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

        SharedPreferences loginInfo = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        if(!loggedIn) return;
        String idToken = loginInfo.getString("idToken", null);
        Button saveOnlineButton = findViewById(R.id.save_online_button);
        saveOnlineButton.setVisibility(Button.VISIBLE);
        Request request = new Request.Builder()
                .url("https://www.customer.city/api/getHistory/")
                .addHeader("Cookie", "token=" + idToken)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if(savedToast != null){
                    savedToast.cancel();
                }
                savedToast = Toast.makeText(RecordActivity.this, "Load Error", Toast.LENGTH_SHORT);
                savedToast.show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    System.out.println("Response: " + responseBody);
                    Gson gson = new Gson();
                    Type type = new TypeToken<HashMap<String, HashMap<String, List<OnlineRecord>>>>() {}.getType();
                    HashMap<String, HashMap<String, List<OnlineRecord>>> hashMap = gson.fromJson(responseBody, type);
                    HashMap<String, List<OnlineRecord>> allData = hashMap.get("data");
                    if(allData == null) return;

                    runOnUiThread(() -> {
                        if(allData.containsKey(selectedRecord.id)){
                            saveOnlineButton.setText("Saved Online");
                            isSavedOnline = true;
                        }
                    });
                } else {
                    System.out.println("Unsuccessful response: " + response.code());
                }
                response.body().close();
            }
        });
        saveOnlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUrl originalUrl = HttpUrl.parse("https://www.customer.city/api/editHistory/");
                assert originalUrl != null;
                HttpUrl.Builder urlBuilder = originalUrl.newBuilder();
                urlBuilder.addQueryParameter("id", selectedRecord.id);

                Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Cookie", "token=" + idToken)
                    .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        if (savedToast != null) {
                            savedToast.cancel();
                        }
                        savedToast = Toast.makeText(RecordActivity.this, "Error Occurred", Toast.LENGTH_SHORT);
                        savedToast.show();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            runOnUiThread(() -> {
                                if (savedToast != null) {
                                    savedToast.cancel();
                                }
                                savedToast = Toast.makeText(RecordActivity.this, "Updated Online Record", Toast.LENGTH_SHORT);
                                savedToast.show();
                                isSavedOnline = !isSavedOnline;
                                if(isSavedOnline){
                                    saveOnlineButton.setText("Saved Online");
                                }else{
                                    saveOnlineButton.setText("Save Online");
                                }
                            });
                        } else {
                            if (savedToast != null) {
                                savedToast.cancel();
                            }
                            savedToast = Toast.makeText(RecordActivity.this, "Error Occurred", Toast.LENGTH_SHORT);
                            savedToast.show();
                        }
                        response.body().close();
                    }
                });
            }
        });
    }
}