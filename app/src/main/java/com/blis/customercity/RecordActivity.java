package com.blis.customercity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.blis.customercity.data.DataAPI;
import com.blis.customercity.data.OnlineRecord;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class RecordActivity extends AppCompatActivity {
    private Toast savedToast;
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
        LinearLayout recordView = findViewById(R.id.record_view);
        TextView recordViewCompany = findViewById(R.id.record_view_company);
        TextView recordViewCategory = findViewById(R.id.record_view_category);
        TextView recordViewDetails = findViewById(R.id.record_view_details);
        new Thread(()->{
            String subCategory, category;
            if(selectedRecord.getCompany_id() != null){
                selectedRecord.setCompany_name_cn(DataConverter.companyIDToCompany(selectedRecord.getCompany_id(), getResources().openRawResource(R.raw.companies)));
                subCategory = DataConverter.companyIDToSubCategory(selectedRecord.getCompany_id(), getResources().openRawResource(R.raw.sub_categories));
                category = DataConverter.companyIDToCategory(selectedRecord.getCompany_id(), getResources().openRawResource(R.raw.categories));
            } else {
                category = null;
                subCategory = null;
            }

            ArrayList<LinearLayout> recordItems = selectedRecord.formatToLayouts(this);

            runOnUiThread(()->{
                recordViewCompany.setText(selectedRecord.getCompany_name_cn());
                if(subCategory == null || category == null){
                    recordViewCategory.setVisibility(View.GONE);
                }
                recordViewCategory.setText(String.format(
                        "%s/%s",
                        subCategory,
                        category
                ));
                recordViewDetails.setText(selectedRecord.getServices_scope_cn());
                if(!selectedRecord.getServices_scope_cn().isEmpty()){
                    recordViewDetails.setVisibility(TextView.VISIBLE);
                }else{
                    recordViewDetails.setVisibility(TextView.GONE);
                }
                for(LinearLayout item : recordItems){
                    recordView.addView(item);
                }
            });
        }).start();

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
            Intent chooser = Intent.createChooser(shareIntent, "分享给:");
            startActivity(chooser);
        });

        // Save online button
        SharedPreferences loginInfo = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        Button saveOnlineButton = findViewById(R.id.save_button);
        if(selectedRecord.getCompany_id() == null){
            saveOnlineButton.setVisibility(View.GONE);
            return;
        }
        if(!loggedIn) {
            saveOnlineButton.setOnClickListener(v -> showToast("請先登入"));
        }else{
            String idToken = loginInfo.getString("idToken", null);
            new Thread(()->{
                HashMap<String, ArrayList<OnlineRecord>> savedRecords = DataAPI.getSavedRecords(idToken);
                runOnUiThread(()->{
                    if(savedRecords.containsKey(selectedRecord.getId())){
                        saveOnlineButton.setText("取消儲存");
                        isSavedOnline = true;
                    }
                });
            }).start();
            saveOnlineButton.setOnClickListener(v -> {
                if(selectedRecord.getCompany_id() == null){
                    return;
                }
                new Thread(()->{
                    boolean result = DataAPI.updateHistory(idToken, selectedRecord.getId());
                    runOnUiThread(()->{
                        if (!result){
                            showToast("發生錯誤，請稍後嘗試");
                            return;
                        }
                        isSavedOnline = !isSavedOnline;
                        if(isSavedOnline){
                            saveOnlineButton.setText("取消儲存");
                            showToast("儲存成功");
                        }else{
                            saveOnlineButton.setText("儲存");
                            showToast("成功移除記錄");
                        }
                    });
                }).start();

            });
        }
    }
}