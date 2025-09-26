package com.blis.customercity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blis.customercity.data.DataAPI;
import com.blis.customercity.data.FileHandler;
import com.blis.customercity.data.Record;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Array;
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
            noRecordViewOnline.setVisibility(View.GONE);
            updateMyCustomRecords(linearLayout);
        }
        if(radiogroup.getCheckedRadioButtonId() == R.id.view_online_button){
            RecyclerView addedRecyclerView = linearLayout.findViewById(R.id.addedRecyclerView);
            RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
            addedRecyclerView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            noRecordViewLocal.setVisibility(View.GONE);
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
            updateOnlineList(linearLayout);
            updateMyCustomRecords(linearLayout);
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
            updateMyCustomRecords(linearLayout);
        });

        return linearLayout;
    }

    /**
     * Update the UI according to signed in status. If not signed in, show login page. If yes, show logout page.
     * @param signedIn Sign in status, true to change into signed in version
     */
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

    /**
     * Create or reset all the content of Offline saved records
     * @param linearLayout layout of cloud fragment
     */
    private void updateMyCustomRecords(CoordinatorLayout linearLayout){
        noRecordViewOnline = linearLayout.findViewById(R.id.no_record_text);
        noRecordViewOnline.setVisibility(View.GONE);
        noRecordViewLocal = linearLayout.findViewById(R.id.no_record_text_local);
        RecyclerView addedRecyclerView = linearLayout.findViewById(R.id.addedRecyclerView);
        addedRecyclerView.addItemDecoration(new DividerItemDecoration(addedRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        SwipeRefreshLayout swipeRefreshLayout = linearLayout.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setOnRefreshListener(() -> updateMyCustomRecords(linearLayout)
        );

        offlineAdapter = new TwoLineAdapter(requireContext(), offlineRecordList);
        addedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        addedRecyclerView.setAdapter(offlineAdapter);
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                removeLocalItem(viewHolder);
//            }
//        });
//        itemTouchHelper.attachToRecyclerView(addedRecyclerView);
//        offlineRecordList = FileHandler.getSavedRecords(requireContext());

        SharedPreferences loginInfo = getContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        String idToken = loginInfo.getString("idToken", null);
        if(!loggedIn && idToken == null) return;

        new Thread(()->{
            offlineRecordList = DataAPI.getMyCustomRecords(idToken);
            if(!isAdded() || getActivity()==null) return;
            getActivity().runOnUiThread(()->{
                if(offlineRecordList.isEmpty()){
                    noRecordViewLocal.setVisibility(View.VISIBLE);
                }else{
                    noRecordViewLocal.setVisibility(View.GONE);
                }
                if(offlineAdapter == null){
                    offlineAdapter = new TwoLineAdapter(requireContext(), offlineRecordList);
                    addedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    addedRecyclerView.setAdapter(offlineAdapter);
                }
                offlineAdapter.updateList(offlineRecordList);
//                offlineAdapter.notifyDataSetChanged();

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
//                        RecyclerView.ViewHolder viewHolder = addedRecyclerView.findViewHolderForAdapterPosition(position);
//                        if (viewHolder != null) {
//                            removeLocalItem(viewHolder);
//                        }
                    }
                });
                swipeRefreshLayout.setRefreshing(false);
            });
        }).start();
    }

    /**
     * Remove specific recycler view holder from recycler view list
     * @param viewHolder Holder of recycler view with item to be removed
     */
    private void removeLocalItem(RecyclerView.ViewHolder viewHolder){
        //Remove swiped item from list and notify the RecyclerView
        int position = viewHolder.getBindingAdapterPosition();
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
    private final ArrayList<Record> onlineRecordList = new ArrayList<>();
    private ArrayList<Record> offlineRecordList = new ArrayList<>();
    private TextView noRecordViewOnline, noRecordViewLocal;
    /**
     * Create or reset all the content of online saved records
     * @param linearLayout layout of cloud fragment
     */
    private void updateOnlineList(CoordinatorLayout linearLayout) {
        noRecordViewLocal = linearLayout.findViewById(R.id.no_record_text_local);
        noRecordViewLocal.setVisibility(View.GONE);
        noRecordViewOnline = linearLayout.findViewById(R.id.no_record_text);
//        ListView onlineListView = linearLayout.findViewById(R.id.online_saved_view_list);
        RecyclerView recyclerView = linearLayout.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        SwipeRefreshLayout swipeRefreshLayout = linearLayout.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> updateOnlineList(linearLayout));
        swipeRefreshLayout.setRefreshing(true);

        onlineAdapter = new TwoLineAdapter(requireContext(), onlineRecordList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(onlineAdapter);

        if (getContext() == null) return;
        SharedPreferences loginInfo = getContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        String idToken = loginInfo.getString("idToken", null);
        if (!loggedIn || idToken == null) return;
        new Thread(()->{
            HashMap<String, ArrayList<Record>> savedRecords = DataAPI.getSavedRecords(idToken);
            if (savedRecords == null) {
                Main main = (Main) getActivity();
                if (main == null || !isAdded()) return;
                main.runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(requireContext(), "網路發生錯誤，正在登出", Toast.LENGTH_LONG).show();
                    main.performLogout();
                });
                return;
            }
            savedRecords.putAll(DataAPI.getCustomBookmark(idToken));

            ArrayList<Record> finalList = new ArrayList<>();
            for (String recordId : savedRecords.keySet()) {
                ArrayList<Record> thisRecord = savedRecords.get(recordId);
                if (thisRecord == null) return;
                finalList.addAll(thisRecord);
            }
            if (getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

//                onlineAdapter.notifyDataSetChanged();
//                recyclerView.scheduleLayoutAnimation();

                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerView.setAdapter(onlineAdapter);

                if (onlineAdapter.getItemCount() == 0) {
                    noRecordViewOnline.setVisibility(View.VISIBLE);
                } else {
                    noRecordViewOnline.setVisibility(View.GONE);
                }
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        removeItem(viewHolder, idToken);
                    }
                });
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
                        if (main == null || !isAdded()) return;
                        main.setCurrentFragment(resultFragment);
                    }

                    @Override
                    public void onDeleteClick(int position) {
                        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                        if (viewHolder != null) {
                            removeItem(viewHolder, idToken);
                        }
                    }
                });
                onlineAdapter.updateList(finalList);
                swipeRefreshLayout.setRefreshing(false);
            });
        }).start();
    }
    private void removeItem(RecyclerView.ViewHolder viewHolder, String idToken) {
        //Remove swiped item from list and notify the RecyclerView
        int position = viewHolder.getBindingAdapterPosition();
        ConfirmationDialog.showConfirmationDialog(
            requireContext(),
            "確認",
            "移除記錄?",
            (dialog, which) -> {
                Record selectedRecord = onlineRecordList.get(position);
                new Thread(()->{
                    boolean result = DataAPI.updateHistory(idToken, selectedRecord.getId());
                    requireActivity().runOnUiThread(()->{
                        if (!result){
                            if (savedToast != null) {
                                savedToast.cancel();
                            }
                            savedToast = Toast.makeText(requireContext(), "發生錯誤", Toast.LENGTH_SHORT);
                            savedToast.show();
                            onlineAdapter.notifyItemChanged(position);
                            return;
                        }
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
                }).start();
            },
            (dialog, which) -> {
                onlineAdapter.notifyItemChanged(position);
                dialog.dismiss();
            });
    }
}