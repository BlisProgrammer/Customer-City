package com.blis.customercity;

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

import com.blis.customercity.data.DataAPI;
import com.blis.customercity.data.FileHandler;
import com.blis.customercity.data.Record;

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
        String companyName = bundle.getString("company_name");
        if(companyName == null)return linearLayout;
        if(companyName.isEmpty())return linearLayout;

        TextView companyNameView = linearLayout.findViewById(R.id.company_name_view);
        ListView mainListView = linearLayout.findViewById(R.id.result_view);
        ProgressBar progressBar = linearLayout.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // get company name
        companyNameView.setText(companyName);

        new Thread(()->{
            ArrayList<Record> selectedOnlineRecords = DataAPI.companyNameToRecords(companyName);
            ArrayList<Record> selectedLocalRecords = FileHandler.companyNameToRecords(requireContext(), companyName);
            ArrayList<Record> selectedRecords = new ArrayList<>();
            selectedRecords.addAll(selectedLocalRecords);
            selectedRecords.addAll(selectedOnlineRecords);
            if(selectedRecords.isEmpty()) {
                ArrayList<String> resultList = new ArrayList<>();
                resultList.add("發生錯誤，請稍後嘗試");
                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(requireContext(), R.layout.result_item, resultList);
                if(getActivity() == null) return;
                getActivity().runOnUiThread(()->{
                    mainListView.setAdapter(adapter1);
                    progressBar.setVisibility(View.GONE);
                    mainListView.setOnItemClickListener(null);
                });
                return;
            }

            // set list view
            ArrayList<String> resultList = new ArrayList<>();
            for (Record record : selectedRecords) {
                resultList.add(record.formatToString());
            }
            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(requireContext(), R.layout.result_item, resultList);

            if(getActivity() == null) return;
            getActivity().runOnUiThread(()->{

                mainListView.setAdapter(adapter1);
                progressBar.setVisibility(View.GONE);

                mainListView.setOnItemClickListener((parent1, view1, position1, id1) -> {

                    Bundle args = new Bundle();
                    args.putSerializable("selected_record", selectedRecords.get(position1));

                    Fragment resultFragment = new RecordFragment();
                    resultFragment.setArguments(args);

                    Main main = (Main) getActivity();
                    if(main == null || !isAdded())return;
                    main.setCurrentFragment(resultFragment);
                });
            });
        }).start();
        Button backButton = linearLayout.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            FirebaseHandler.logButtonClick(requireContext(), this, backButton);
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();
        });
        return linearLayout;
    }
}
