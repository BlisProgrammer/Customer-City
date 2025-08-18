package com.blis.customercity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blis.customercity.Data.OnlineRecord;
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
        if(loggedIn && idToken != null){
            loginLayout.setVisibility(View.GONE);
            logoutLayout.setVisibility(View.VISIBLE);
            updateOnlineList(linearLayout, loginLayout, logoutLayout);
        }
        recordActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        updateOnlineList(linearLayout, loginLayout, logoutLayout);
                    }
                });

        Button switchToUserButton = linearLayout.findViewById(R.id.switch_to_user_button);
        switchToUserButton.setOnClickListener(v->{
            Main.bottomNavigationView.setSelectedItemId(R.id.nav_user);
        });

        return linearLayout;
    }
    private Toast savedToast;
    private ActivityResultLauncher<Intent> recordActivityResultLauncher;

    private void updateOnlineList(LinearLayout linearLayout, LinearLayout loginLayout, LinearLayout logoutLayout){
        ListView onlineListView = linearLayout.findViewById(R.id.online_saved_view_list);
        SwipeRefreshLayout swipeRefreshLayout = linearLayout.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
                updateOnlineList(linearLayout, loginLayout, logoutLayout);
            }
        );
        ArrayList<OnlineRecord> onlineRecordList = new ArrayList<>();
        SharedPreferences loginInfo = getContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        String idToken = loginInfo.getString("idToken", null);
        swipeRefreshLayout.setRefreshing(true);
        if(loggedIn && idToken != null){

            // get records from online
            Request request = new Request.Builder()
                    .url("https://www.customer.city/api/getHistory/")
                    .addHeader("Cookie", "token=" + idToken)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    SharedPreferences.Editor editor = loginInfo.edit();
                    editor.putBoolean("loggedIn", false);
                    editor.putString("idToken", null);
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
                            if(!isAdded()) return;
                            onlineRecordList.add(thisOnlineRecord);
                        }

                        assert getActivity() != null;
                        getActivity().runOnUiThread(() -> {
                            if(!isAdded())return;
                            TwoLineAdapter onlineAdapter = new TwoLineAdapter(requireContext(), onlineRecordList);
                            onlineListView.setAdapter(onlineAdapter);
                            swipeRefreshLayout.setRefreshing(false);
                        });
                    } else {
                        if(response.code() == 401){
                            SharedPreferences.Editor editor = loginInfo.edit();
                            editor.putBoolean("loggedIn", false);
                            editor.putString("idToken", null);
                            editor.apply();

                            getActivity().runOnUiThread(() -> {
                                logoutLayout.setVisibility(View.GONE);
                                loginLayout.setVisibility(View.VISIBLE);
                                swipeRefreshLayout.setRefreshing(false);
                            });
                        }
                        System.out.println("Unsuccessful response: " + response.code());
                    }
                    response.body().close();
                }
            });
        }

        onlineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(onlineRecordList.isEmpty())return;
                Intent recordIntent = new Intent(getActivity(), RecordActivity.class);
                recordIntent.putExtra("selected_record", onlineRecordList.get(position));
                recordActivityResultLauncher.launch(recordIntent);
            }
        });
        onlineListView.setOnItemLongClickListener((parent, view, position, id) -> {
            ConfirmationDialog.showConfirmationDialog(
                    requireContext(),
                    "Confirm Action",
                    "Delete saved record? ",
                    (dialog, which) -> {
                        OnlineRecord selectedRecord = onlineRecordList.get(position);

                        // remove with api call
                        HttpUrl originalUrl = HttpUrl.parse("https://www.customer.city/api/editHistory/");
                        assert originalUrl != null;
                        HttpUrl.Builder urlBuilder = originalUrl.newBuilder();
                        urlBuilder.addQueryParameter("id", selectedRecord.getId());

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
                                savedToast = Toast.makeText(requireContext(), "Error Occurred", Toast.LENGTH_SHORT);
                                savedToast.show();
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    assert getActivity() != null;
                                    getActivity().runOnUiThread(() -> {
                                        if (savedToast != null) {
                                            savedToast.cancel();
                                        }
                                        savedToast = Toast.makeText(requireContext(), "Item removed", Toast.LENGTH_SHORT);
                                        savedToast.show();

                                        onlineRecordList.remove(position);
                                        TwoLineAdapter onlineAdapter = new TwoLineAdapter(requireContext(), onlineRecordList);
                                        onlineListView.setAdapter(onlineAdapter);
                                        dialog.dismiss();
                                    });
                                } else {
                                    if (savedToast != null) {
                                        savedToast.cancel();
                                    }
                                    savedToast = Toast.makeText(requireContext(), "Error Occurred", Toast.LENGTH_SHORT);
                                    savedToast.show();
                                }
                                response.body().close();
                            }
                        });
                    },
                    (dialog, which) -> dialog.dismiss()

            );
            return true;
        });
    }
}