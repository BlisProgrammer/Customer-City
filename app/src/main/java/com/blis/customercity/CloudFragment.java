package com.blis.customercity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
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
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blis.customercity.data.OnlineRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import org.checkerframework.checker.units.qual.A;


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
        CoordinatorLayout linearLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_cloud, container, false);
        assert getContext() != null;
        SharedPreferences loginInfo = getContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        String idToken = loginInfo.getString("idToken", null);

        loginLayout = linearLayout.findViewById(R.id.login_layout);
        logoutLayout = linearLayout.findViewById(R.id.logout_layout);
        addButton = linearLayout.findViewById(R.id.add_button);

        updateUI(loggedIn);
        if(loggedIn && idToken != null){
//            updateOnlineList(linearLayout);
//            updateOfflineList(linearLayout);
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

        addButton.setOnClickListener(v -> {
            Main main = (Main) getActivity();
            if(main == null || !isAdded())return;
            AddFragment addFragment = new AddFragment();
            main.setCurrentFragment(addFragment);
        });

        RadioButton viewOnlineButton = linearLayout.findViewById(R.id.view_online_button);
        viewOnlineButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked)return;
            RecyclerView addedRecyclerView = linearLayout.findViewById(R.id.addedRecyclerView);
            RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
            addedRecyclerView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
            updateOnlineList(linearLayout);
        });
        RadioButton viewLocalButton = linearLayout.findViewById(R.id.view_local_button);
        viewLocalButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked)return;
            RecyclerView addedRecyclerView = linearLayout.findViewById(R.id.addedRecyclerView);
            RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
            addedRecyclerView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            addButton.setVisibility(View.VISIBLE);
            updateOfflineList(linearLayout);
        });
        RadioGroup radiogroup = linearLayout.findViewById(R.id.toggle_radio_group);
        if(radiogroup.getCheckedRadioButtonId() == R.id.view_local_button){
            RecyclerView addedRecyclerView = linearLayout.findViewById(R.id.addedRecyclerView);
            RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
            addedRecyclerView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            addButton.setVisibility(View.VISIBLE);
            updateOfflineList(linearLayout);
        }
        if(radiogroup.getCheckedRadioButtonId() == R.id.view_online_button){
            RecyclerView addedRecyclerView = linearLayout.findViewById(R.id.addedRecyclerView);
            RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
            addedRecyclerView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
            updateOnlineList(linearLayout);
        }

        return linearLayout;
    }
    FloatingActionButton addButton;

    public void updateUI(boolean signedIn) {
        if(loginLayout == null || logoutLayout == null) return;
        if(signedIn){
            loginLayout.setVisibility(View.GONE);
            logoutLayout.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
        }else{
            loginLayout.setVisibility(View.VISIBLE);
            logoutLayout.setVisibility(View.GONE);
            addButton.setVisibility(View.GONE);
        }
    }
    private TwoLineAdapter offlineAdapter;
    private void updateOfflineList(CoordinatorLayout linearLayout){
        noRecordView = linearLayout.findViewById(R.id.no_record_text);
        noRecordView.setVisibility(View.GONE);
        RecyclerView addedRecyclerView = linearLayout.findViewById(R.id.addedRecyclerView);
        addedRecyclerView.addItemDecoration(new DividerItemDecoration(addedRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        SwipeRefreshLayout swipeRefreshLayout = linearLayout.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setOnRefreshListener(() -> updateOfflineList(linearLayout)
        );

        offlineAdapter = new TwoLineAdapter(requireContext(), offlineRecordList);
        addedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        addedRecyclerView.setAdapter(offlineAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                removeLocalItem(viewHolder, linearLayout);
            }
        });
        itemTouchHelper.attachToRecyclerView(addedRecyclerView);

        String addedRecords = FileHandler.loadFromFile(requireContext(), "addedRecords");
        if(!addedRecords.isEmpty()){
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<OnlineRecord>>() {}.getType();
            offlineRecordList = gson.fromJson(addedRecords, listType);
            if(offlineRecordList.isEmpty()){
                noRecordView.setVisibility(View.VISIBLE);
            }else{
                noRecordView.setVisibility(View.GONE);
            }
            offlineAdapter = new TwoLineAdapter(requireContext(), offlineRecordList);
            addedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            addedRecyclerView.setAdapter(offlineAdapter);
            offlineAdapter.notifyDataSetChanged();
            addedRecyclerView.scheduleLayoutAnimation();
            swipeRefreshLayout.setRefreshing(false);

            offlineAdapter.setOnItemClickListener(new TwoLineAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (offlineRecordList.isEmpty()) return;
                    Intent recordIntent = new Intent(getActivity(), RecordActivity.class);
                    recordIntent.putExtra("selected_record", offlineRecordList.get(position));
                    recordActivityResultLauncher.launch(recordIntent);
                }

                @Override
                public void onDeleteClick(int position) {
                    RecyclerView.ViewHolder viewHolder = addedRecyclerView.findViewHolderForAdapterPosition(position);
                    if (viewHolder != null) {
                        removeLocalItem(viewHolder, linearLayout);
                    }
                }
            });
        }
        swipeRefreshLayout.setRefreshing(false);
    }
    private void removeLocalItem(RecyclerView.ViewHolder viewHolder, CoordinatorLayout linearLayout1){
        //Remove swiped item from list and notify the RecyclerView
        int position = viewHolder.getAdapterPosition();
        ConfirmationDialog.showConfirmationDialog(
                requireContext(),
                "確認",
                "移除記錄?",
                (dialog, which) -> {
                    offlineRecordList.remove(position);
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(offlineRecordList);
                    FileHandler.saveToFile(requireContext(), "addedRecords", jsonString);

                    updateOfflineList(linearLayout1);
                    savedToast = Toast.makeText(requireContext(), "成功移除記錄", Toast.LENGTH_SHORT);
                    savedToast.show();
                    offlineAdapter.notifyItemRemoved(position);
                },
                (dialog, which) -> {
                    offlineAdapter.notifyItemChanged(position);
                    dialog.dismiss();
                });
    }

    private Toast savedToast;
    private ActivityResultLauncher<Intent> recordActivityResultLauncher;
    private final ArrayList<OnlineRecord> onlineRecordList = new ArrayList<>();
    private ArrayList<OnlineRecord> offlineRecordList = new ArrayList<>();
    private TextView noRecordView;
    private void updateOnlineList(CoordinatorLayout linearLayout){
        noRecordView = linearLayout.findViewById(R.id.no_record_text);
        noRecordView.setVisibility(View.GONE);
//        ListView onlineListView = linearLayout.findViewById(R.id.online_saved_view_list);
        RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        SwipeRefreshLayout swipeRefreshLayout = linearLayout.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setOnRefreshListener(() -> updateOnlineList(linearLayout)
        );

        onlineAdapter = new TwoLineAdapter(requireContext(), onlineRecordList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(onlineAdapter);

        if(getContext() == null) return;
        SharedPreferences loginInfo = getContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        String idToken = loginInfo.getString("idToken", null);
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
                            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                            recyclerView.setAdapter(onlineAdapter);
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