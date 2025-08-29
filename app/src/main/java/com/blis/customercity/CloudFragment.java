package com.blis.customercity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blis.customercity.data.FileHandler;
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

    @Override
    public void onResume() {
        super.onResume();
        CoordinatorLayout linearLayout = (CoordinatorLayout) getView();
        RadioGroup radiogroup = linearLayout.findViewById(R.id.toggle_radio_group);
        if(radiogroup.getCheckedRadioButtonId() == R.id.view_local_button){
            RecyclerView addedRecyclerView = linearLayout.findViewById(R.id.addedRecyclerView);
            RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
            addedRecyclerView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            updateOfflineList(linearLayout);
        }
        if(radiogroup.getCheckedRadioButtonId() == R.id.view_online_button){
            RecyclerView addedRecyclerView = linearLayout.findViewById(R.id.addedRecyclerView);
            RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
            addedRecyclerView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            updateOnlineList(linearLayout);
        }
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

        updateUI(loggedIn);
        if(loggedIn && idToken != null){
//            updateOnlineList(linearLayout);
//            updateOfflineList(linearLayout);
        }

        Button switchToUserButton = linearLayout.findViewById(R.id.switch_to_user_button);
        switchToUserButton.setOnClickListener(v->{
            FirebaseHandler.logButtonClick(requireContext(), this, switchToUserButton);
            Main main = (Main) getActivity();
            if(main == null) return;
            main.goToSignIn();
        });

        RadioButton viewOnlineButton = linearLayout.findViewById(R.id.view_online_button);
        viewOnlineButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked)return;
            RecyclerView addedRecyclerView = linearLayout.findViewById(R.id.addedRecyclerView);
            RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
            addedRecyclerView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            updateOnlineList(linearLayout);
        });
        RadioButton viewLocalButton = linearLayout.findViewById(R.id.view_local_button);
        viewLocalButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked)return;
            RecyclerView addedRecyclerView = linearLayout.findViewById(R.id.addedRecyclerView);
            RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
            addedRecyclerView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            updateOfflineList(linearLayout);
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
    private TwoLineAdapter offlineAdapter;
    private void updateOfflineList(CoordinatorLayout linearLayout){
        noRecordViewOnline = linearLayout.findViewById(R.id.no_record_text);
        noRecordViewOnline.setVisibility(View.GONE);
        noRecordViewLocal = linearLayout.findViewById(R.id.no_record_text_local);
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

        offlineRecordList = FileHandler.getSavedRecords(requireContext());
        if(offlineRecordList.isEmpty()){
            noRecordViewLocal.setVisibility(View.VISIBLE);
        }else{
            noRecordViewLocal.setVisibility(View.GONE);
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

                Bundle args = new Bundle();
                args.putSerializable("selected_record", offlineRecordList.get(position));

                Fragment resultFragment = new RecordFragment();
                resultFragment.setArguments(args);

                Main main = (Main) getActivity();
                if(main == null || !isAdded())return;
                main.setCurrentFragment(resultFragment);
            }

            @Override
            public void onDeleteClick(int position) {
                RecyclerView.ViewHolder viewHolder = addedRecyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder != null) {
                    removeLocalItem(viewHolder, linearLayout);
                }
            }
        });
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
                    FileHandler.saveSavedRecord(requireContext(), offlineRecordList);

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
    private final ArrayList<OnlineRecord> onlineRecordList = new ArrayList<>();
    private ArrayList<OnlineRecord> offlineRecordList = new ArrayList<>();
    private TextView noRecordViewOnline, noRecordViewLocal;
    private void updateOnlineList(CoordinatorLayout linearLayout){
        noRecordViewLocal = linearLayout.findViewById(R.id.no_record_text_local);
        noRecordViewLocal.setVisibility(View.GONE);
        noRecordViewOnline = linearLayout.findViewById(R.id.no_record_text);
//        ListView onlineListView = linearLayout.findViewById(R.id.online_saved_view_list);
        RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        SwipeRefreshLayout swipeRefreshLayout = linearLayout.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setOnRefreshListener(() -> updateOnlineList(linearLayout)
        );
        swipeRefreshLayout.setRefreshing(true);

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
                        if(getActivity() == null ||  !isAdded()){
                            return;
                        }
                        getActivity().runOnUiThread(() -> {
                            if(!isAdded())return;
                            onlineAdapter.notifyDataSetChanged();
                            recyclerView.scheduleLayoutAnimation();
                            if(onlineAdapter.getItemCount() == 0){
                                noRecordViewOnline.setVisibility(View.VISIBLE);
                            }else {
                                noRecordViewOnline.setVisibility(View.GONE);
                            }
                            ItemTouchHelper itemTouchHelper = getItemTouchHelper();
                            itemTouchHelper.attachToRecyclerView(recyclerView);

                            onlineAdapter.setOnItemClickListener(new TwoLineAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    if (onlineRecordList.isEmpty()) return;

                                    Bundle args = new Bundle();
                                    args.putSerializable("selected_record", onlineRecordList.get(position));

                                    Fragment resultFragment = new RecordFragment();
                                    resultFragment.setArguments(args);

                                    Main main = (Main) getActivity();
                                    if(main == null || !isAdded())return;
                                    main.setCurrentFragment(resultFragment);
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
                                                noRecordViewOnline.setVisibility(View.VISIBLE);
                                            }else {
                                                noRecordViewOnline.setVisibility(View.GONE);
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