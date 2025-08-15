package com.blis.customercity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

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
        String companyId = bundle.getString("company_id");
        if(companyId == null)return linearLayout;

        ListView mainListView = linearLayout.findViewById(R.id.result_view);

//        ArrayList<Record> selectedRecords = DataConverter.getRecords(
//                companyId,
//                getResources().openRawResource(R.raw.records),
//                DataConverter.companyIDToCategory(companyId, getResources().openRawResource(R.raw.categories)),
//                DataConverter.companyIDToSubCategory(companyId, getResources().openRawResource(R.raw.sub_categories)),
//                DataConverter.companyIDToCompany(companyId, getResources().openRawResource(R.raw.companies)));
        new Thread(()->{
            ArrayList<OnlineRecord> selectedRecords = DataAPI.companyIDtoRecords(companyId);
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

                mainListView.setOnItemClickListener((parent1, view1, position1, id1) -> {
                    Intent recordIntent = new Intent(getActivity(), RecordActivity.class);
                    recordIntent.putExtra("selected_record", selectedRecords.get(position1));
                    startActivity(recordIntent);
                });
            });
        }).start();
        return linearLayout;
    }
}
