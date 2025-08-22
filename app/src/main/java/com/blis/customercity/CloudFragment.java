package com.blis.customercity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blis.customercity.data.OnlineRecord;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;


public class CloudFragment extends Fragment {
    private TwoLineAdapter onlineAdapter;
    private LinearLayout loginLayout, logoutLayout;

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

        loginLayout = linearLayout.findViewById(R.id.login_layout);
        logoutLayout = linearLayout.findViewById(R.id.logout_layout);

        updateUI(loggedIn);
        if(loggedIn && idToken != null){
            updateOnlineList(linearLayout);
        }
        recordActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> updateOnlineList(linearLayout));

        Button switchToUserButton = linearLayout.findViewById(R.id.switch_to_user_button);
        switchToUserButton.setOnClickListener(v->{
            Main main = (Main) getActivity();
            if(main == null) return;
            main.goToSignIn();
        });

        return linearLayout;
    }

    public void updateUI(boolean signedIn) {
        if(loginLayout == null || logoutLayout == null) return;
        if(signedIn){
            loginLayout.setVisibility(View.GONE);
            logoutLayout.setVisibility(View.VISIBLE);
        }else{
            loginLayout.setVisibility(View.VISIBLE);
            logoutLayout.setVisibility(View.GONE);
        }
    }
    private Toast savedToast;
    private ActivityResultLauncher<Intent> recordActivityResultLauncher;
    private final ArrayList<OnlineRecord> onlineRecordList = new ArrayList<>();
    private TextView noRecordView;

    private void updateOnlineList(LinearLayout linearLayout){
        noRecordView = linearLayout.findViewById(R.id.no_record_text);
//        ListView onlineListView = linearLayout.findViewById(R.id.online_saved_view_list);
        RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        SwipeRefreshLayout swipeRefreshLayout = linearLayout.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> updateOnlineList(linearLayout)
        );
        if(onlineAdapter == null){
            onlineAdapter = new TwoLineAdapter(requireContext(), onlineRecordList);
        }
        recyclerView.setAdapter(onlineAdapter);
        if(getContext() == null) return;
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
                    Main main = (Main) getActivity();
                    if(main == null || !isAdded())return;
                    main.runOnUiThread(()->{
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(requireContext(), "網路發生錯誤，正在登出", Toast.LENGTH_LONG).show();
                        main.performLogout();
                    });
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
                        onlineRecordList.clear();
                        for (List<OnlineRecord> value : allData.values()) {
                            OnlineRecord thisOnlineRecord = value.get(0);
                            if(!isAdded()) return;
                            onlineRecordList.add(thisOnlineRecord);
                        }

                        assert getActivity() != null;
                        getActivity().runOnUiThread(() -> {
                            if(!isAdded())return;
                            onlineAdapter.notifyDataSetChanged();
                            recyclerView.scheduleLayoutAnimation();
                            if(onlineAdapter.getItemCount() == 0){
                                noRecordView.setVisibility(View.VISIBLE);
                            }else {
                                noRecordView.setVisibility(View.GONE);
                            }
                            ItemTouchHelper itemTouchHelper = getItemTouchHelper();
                            itemTouchHelper.attachToRecyclerView(recyclerView);

                            onlineAdapter.setOnItemClickListener(new TwoLineAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    if (onlineRecordList.isEmpty()) return;
                                    Intent recordIntent = new Intent(getActivity(), RecordActivity.class);
                                    recordIntent.putExtra("selected_record", onlineRecordList.get(position));
                                    recordActivityResultLauncher.launch(recordIntent);
                                }

                                @Override
                                public void onDeleteClick(int position) {
                                    RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                                    if (viewHolder != null) {
                                        removeItem(viewHolder);
                                    }
                                }
                            });

//                            onlineListView.setAdapter(onlineAdapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                            swipeRefreshLayout.setRefreshing(false);
                        });
                    } else {
                        if(response.code() == 401){
                            Main main = (Main) getActivity();
                            if(!isAdded() || main == null) return;
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "發生錯誤，正在登出", Toast.LENGTH_LONG).show();
                                main.performLogout();
                                swipeRefreshLayout.setRefreshing(false);
                            });
                        }
                        System.out.println("Unsuccessful response: " + response.code());
                    }
                    response.body().close();
                }

                @NonNull
                private ItemTouchHelper getItemTouchHelper() {
                    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                            return false;
                        }
                        @Override
                        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                            removeItem(viewHolder);
                        }
                    };
                    return new ItemTouchHelper(simpleItemTouchCallback);
                }

                private void removeItem(RecyclerView.ViewHolder viewHolder) {
                    //Remove swiped item from list and notify the RecyclerView
                    int position = viewHolder.getAdapterPosition();
                    ConfirmationDialog.showConfirmationDialog(
                        requireContext(),
                        "確認",
                        "移除記錄?",
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
                                    assert getActivity() != null;
                                    getActivity().runOnUiThread(()->{
                                        if (savedToast != null) {
                                            savedToast.cancel();
                                        }
                                        savedToast = Toast.makeText(requireContext(), "發生錯誤", Toast.LENGTH_SHORT);
                                        savedToast.show();
                                        onlineAdapter.notifyItemChanged(position);
                                    });
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        assert getActivity() != null;
                                        getActivity().runOnUiThread(() -> {
                                            if (savedToast != null) {
                                                savedToast.cancel();
                                            }
                                            savedToast = Toast.makeText(requireContext(), "成功移除記錄", Toast.LENGTH_SHORT);
                                            savedToast.show();

                                            onlineRecordList.remove(position);
                                            onlineAdapter.notifyItemRemoved(position);
                                            if(onlineAdapter.getItemCount() == 0){
                                                noRecordView.setVisibility(View.VISIBLE);
                                            }else {
                                                noRecordView.setVisibility(View.GONE);
                                            }
                                            dialog.dismiss();
                                        });
                                    } else {
                                        if (savedToast != null) {
                                            savedToast.cancel();
                                        }
                                        savedToast = Toast.makeText(requireContext(), "發生錯誤", Toast.LENGTH_SHORT);
                                        savedToast.show();
                                    }
                                    response.body().close();
                                }
                            });
                        },
                        (dialog, which) -> {
                            onlineAdapter.notifyItemChanged(position);
                            dialog.dismiss();
                        });
                }
            });
        }
    }
}