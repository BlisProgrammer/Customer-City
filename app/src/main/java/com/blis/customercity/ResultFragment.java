package com.blis.customercity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.blis.customercity.Data.DataAPI;
import com.blis.customercity.Data.OnlineRecord;

import java.util.ArrayList;

public class ResultFragment extends Fragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_result, container, false);
        Bundle bundle = getArguments();
        if(bundle == null) return linearLayout;
        ArrayList<String> companyIDs = bundle.getStringArrayList("company_ids");
        if(companyIDs == null)return linearLayout;
        if(companyIDs.isEmpty())return linearLayout;

        TextView companyNameView = linearLayout.findViewById(R.id.company_name_view);
        ListView mainListView = linearLayout.findViewById(R.id.result_view);
        ProgressBar progressBar = linearLayout.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // get company name
        new Thread(()->{
            String companyName = DataConverter.companyIDToCompany(companyIDs.get(0), getResources().openRawResource(R.raw.companies));
            getActivity().runOnUiThread(()->{
                companyNameView.setText(companyName);
            });
        }).start();
        new Thread(()->{
            ArrayList<OnlineRecord> selectedRecords = DataAPI.companyIDtoRecords(companyIDs);
            if(selectedRecords.isEmpty()) return;

            // set list view
            ArrayList<String> resultList = new ArrayList<>();
            for (OnlineRecord record : selectedRecords) {
                resultList.add(record.formatToString());
            }
            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, resultList);

            if(getActivity() == null) return;

            getActivity().runOnUiThread(()->{

                mainListView.setAdapter(adapter1);
                progressBar.setVisibility(View.GONE);

                mainListView.setOnItemClickListener((parent1, view1, position1, id1) -> {
                    Intent recordIntent = new Intent(getActivity(), RecordActivity.class);
                    recordIntent.putExtra("selected_record", selectedRecords.get(position1));
                    startActivity(recordIntent);
                });
            });
        }).start();
        Button backButton = linearLayout.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Fragment findFragment = new FindFragment();

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(this);
            fragmentTransaction.commit();
        });
        return linearLayout;
    }
}
