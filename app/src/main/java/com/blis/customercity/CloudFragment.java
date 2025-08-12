package com.blis.customercity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;


public class CloudFragment extends Fragment {
    public static CloudFragment newInstance(String param1, String param2) {
        CloudFragment fragment = new CloudFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private final OkHttpClient client = new OkHttpClient();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_cloud, container, false);
        assert getContext() != null;
        SharedPreferences loginInfo = getContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        String idToken = loginInfo.getString("idToken", null);

        LinearLayout loginLayout = linearLayout.findViewById(R.id.login_layout);
        LinearLayout logoutLayout = linearLayout.findViewById(R.id.logout_layout);
        TextView loginTitleView = linearLayout.findViewById(R.id.login_title_view);
        if(loggedIn && idToken != null){
            loginLayout.setVisibility(View.GONE);
            logoutLayout.setVisibility(View.VISIBLE);
            loginTitleView.setText("Logged In");
            updateOnlineList(linearLayout, loginLayout, logoutLayout);
        }
        Button loginButton = linearLayout.findViewById(R.id.login_button);
        TextInputEditText emailInput = linearLayout.findViewById(R.id.login_email_text);
        TextInputEditText passwordInput = linearLayout.findViewById(R.id.login_password_text);
        TextView errorTextView = linearLayout.findViewById(R.id.error_text_view);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword").newBuilder();
                urlBuilder.addQueryParameter("key", "AIzaSyAJ5XXmXlPuHPqRysgfYIFPkF4cwKrCICU");
                String finalUrl = urlBuilder.build().toString();

                HashMap<String, String> body = new HashMap<>();
                body.put("email", String.valueOf(emailInput.getText()));
                body.put("password", String.valueOf(passwordInput.getText()));
                body.put("returnSecureToken", "true");

                Gson gson = new Gson();
                String jsonBody = gson.toJson(body);
                errorTextView.setText("");
                RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(finalUrl) // URL with parameters
                        .post(requestBody) // Request body
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        assert getActivity() != null;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                errorTextView.setText("An error occured.");
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            Gson gson = new Gson();
                            Type type = new TypeToken<HashMap<String, String>>() {}.getType();
                            HashMap<String, String> hashMap = gson.fromJson(responseBody, type);
                            String idToken1 = hashMap.get("idToken");
                            SharedPreferences.Editor editor = loginInfo.edit();
                            editor.putString("idToken", idToken1);

                            // Login in successful, show logged in screen
                            editor.putBoolean("loggedIn", true);
                            editor.apply();
                            assert getActivity() != null;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loginTitleView.setText("Logged In");
                                    loginLayout.setVisibility(View.GONE);
                                    logoutLayout.setVisibility(View.VISIBLE);
                                    updateOnlineList(linearLayout, loginLayout, logoutLayout);
                                }
                            });
                        } else {
                            assert getActivity() != null;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    errorTextView.setText("Make sure that you have provided the correct email and password. ");
                                }
                            });
                        }
                        response.body().close(); // Close the response body
                    }
                });


            }
        });
        return linearLayout;
    }
    private void updateOnlineList(LinearLayout linearLayout, LinearLayout loginLayout, LinearLayout logoutLayout){
        ListView onlineListView = linearLayout.findViewById(R.id.online_saved_view_list);
        SwipeRefreshLayout swipeRefreshLayout = linearLayout.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
                updateOnlineList(linearLayout, loginLayout, logoutLayout);
            }
        );
        SharedPreferences loginInfo = getContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        String idToken = loginInfo.getString("idToken", null);
        if(loggedIn && idToken != null){
            ArrayList<Record> onlineRecordList = new ArrayList<>();

            // get records from online
            Request request = new Request.Builder()
                    .url("https://www.customer.city/api/getHistory/")
                    .addHeader("Cookie", "token=" + idToken)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    SharedPreferences.Editor editor = loginInfo.edit();
                    editor.putString("loggedIn", null);
                    editor.putBoolean("idToken", false);
                    editor.apply();

                    logoutLayout.setVisibility(View.GONE);
                    loginLayout.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
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
                        for (List<OnlineRecord> value : allData.values()) {
                            OnlineRecord thisOnlineRecord = value.get(0);
                            if(!isAdded())return;
                            onlineRecordList.add(
                                    new Record(
                                            thisOnlineRecord.getId(),
                                            DataConverter.companyIDToCategory(thisOnlineRecord.getCompany_id(), getResources().openRawResource(R.raw.categories)),
                                            DataConverter.companyIDToSubCategory(thisOnlineRecord.getCompany_id(), getResources().openRawResource(R.raw.sub_categories)),
                                            thisOnlineRecord.getCompany_name_cn(),
                                            thisOnlineRecord.getServices_scope_cn(),
                                            thisOnlineRecord.getService_hotline(),
                                            thisOnlineRecord.getEmail(),
                                            thisOnlineRecord.getAddress_cn(),
                                            thisOnlineRecord.getAdded_detail_cn(),
                                            thisOnlineRecord.getTips_cn()
                                    )
                            );
                        }

                        assert getActivity() != null;
                        getActivity().runOnUiThread(() -> {
                            if(!isAdded())return;
                            TwoLineAdapter onlineAdapter = new TwoLineAdapter(requireContext(), onlineRecordList);
                            onlineListView.setAdapter(onlineAdapter);
                            swipeRefreshLayout.setRefreshing(false);
                        });
                    } else {
                        System.out.println("Unsuccessful response: " + response.code());
                    }
                    response.body().close();
                }
            });
        }

        Button logoutButton = linearLayout.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = loginInfo.edit();
            editor.putString("loggedIn", null);
            editor.putBoolean("idToken", false);
            editor.apply();
            logoutLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        });
    }
}