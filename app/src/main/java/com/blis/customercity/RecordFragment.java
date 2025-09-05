package com.blis.customercity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.blis.customercity.data.DataAPI;
import com.blis.customercity.data.OnlineRecord;

import java.util.ArrayList;
import java.util.HashMap;

public class RecordFragment extends Fragment {
    private Toast savedToast;
    private boolean isSavedOnline = false;
    private void showToast(String text){
        if(savedToast != null){
            savedToast.cancel();
        }
        savedToast = Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT);
        savedToast.show();
    }

    public void updateUI(boolean loggedIn){
        if(!loggedIn){
            if(saveOnlineButton != null) {
                FirebaseHandler.logButtonClick(requireContext(), this, saveOnlineButton);
                saveOnlineButton.setOnClickListener(v -> showToast("請先登入"));
                saveOnlineButton.setText("儲存");
            }
        }
    }
    private Button saveOnlineButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ConstraintLayout constraintLayout = (ConstraintLayout) inflater.inflate(R.layout.record_view, container, false);
        Bundle bundle = getArguments();
        OnlineRecord selectedRecord;
        if(bundle == null) return constraintLayout;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            selectedRecord = bundle.getSerializable("selected_record", OnlineRecord.class);
        }else{
            selectedRecord = (OnlineRecord) bundle.getSerializable("selected_record");
        }
        if(selectedRecord == null)return constraintLayout;

//        Intent intent = getActivity().getIntent();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            selectedRecord = intent.getSerializableExtra("selected_record", OnlineRecord.class);
//        } else {
//            selectedRecord = (OnlineRecord) intent.getSerializableExtra("selected_record");
//        }
//        if(selectedRecord == null){
//            return constraintLayout;
//        }
        LinearLayout recordView = constraintLayout.findViewById(R.id.record_view);
        TextView recordViewCompany = constraintLayout.findViewById(R.id.record_view_company);
        TextView recordViewCategory = constraintLayout.findViewById(R.id.record_view_category);
        TextView recordViewDetails = constraintLayout.findViewById(R.id.record_view_details);
        new Thread(()->{
            String subCategory, category;
            if(selectedRecord.getCompany_id() != null){
                selectedRecord.setCompany_name_cn(DataConverter.companyIDToCompany(selectedRecord.getCompany_id(), getResources().openRawResource(R.raw.companies)));
                subCategory = DataConverter.companyIDToSubCategory(selectedRecord.getCompany_id(), getResources().openRawResource(R.raw.sub_categories));
                category = DataConverter.companyIDToCategory(selectedRecord.getCompany_id(), getResources().openRawResource(R.raw.categories));
            } else {
                category = selectedRecord.getCategory();
                subCategory = selectedRecord.getSubCategory();
            }

            ArrayList<LinearLayout> recordItems = selectedRecord.formatToLayouts(requireContext());

            if(getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(()->{
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
        Button backButton = constraintLayout.findViewById(R.id.back_button);
        backButton.setOnClickListener(v->{
            FirebaseHandler.logButtonClick(requireContext(), this, backButton);
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        });

        // share button
        Button shareButton = constraintLayout.findViewById(R.id.share_button);
        shareButton.setOnClickListener(v->{
            FirebaseHandler.logButtonClick(requireContext(), this, shareButton);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, selectedRecord.getShareString());
            Intent chooser = Intent.createChooser(shareIntent, "分享给:");
            startActivity(chooser);
        });

        // Save online button
        SharedPreferences loginInfo = requireActivity().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        saveOnlineButton = constraintLayout.findViewById(R.id.save_button);
        if(selectedRecord.getCompany_id() == null){
            saveOnlineButton.setVisibility(View.GONE);
            return constraintLayout;
        }
        if(!loggedIn) {
            saveOnlineButton.setOnClickListener(v -> {
                FirebaseHandler.logButtonClick(requireContext(), this, saveOnlineButton);
                showToast("請先登入");
            });
            saveOnlineButton.setText("儲存");
        }else{
            String idToken = loginInfo.getString("idToken", null);
            new Thread(()->{
                HashMap<String, ArrayList<OnlineRecord>> savedRecords = DataAPI.getSavedRecords(idToken);
                requireActivity().runOnUiThread(()->{
                    if(savedRecords.containsKey(selectedRecord.getId())){
                        saveOnlineButton.setText("取消儲存");
                        isSavedOnline = true;
                    }
                });
            }).start();
            saveOnlineButton.setOnClickListener(v -> {
                FirebaseHandler.logButtonClick(requireContext(), this, saveOnlineButton);

                if(selectedRecord.getCompany_id() == null){
                    return;
                }
                new Thread(()->{
                    boolean result = DataAPI.updateHistory(idToken, selectedRecord.getId());
                    requireActivity().runOnUiThread(()->{
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
        return constraintLayout;
    }
}