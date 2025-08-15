package com.blis.customercity;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_search, container, false);

        ListView listView = linearLayout.findViewById(R.id.search_list_view);
        ListView resultView = linearLayout.findViewById(R.id.search_result_view);
        SearchView searchView = linearLayout.findViewById(R.id.search_view);
        new Thread(()->{
            if(!isAdded())return;
            ArrayList<String> allCompanies = DataConverter.getAllCompanies(getResources().openRawResource(R.raw.companies));
            if(!isAdded())return;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, allCompanies);
            if(getView() == null) return;
            getView().post(()->{
                if(!isAdded())return;
                listView.setAdapter(adapter);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        adapter.getFilter().filter(newText);
                        return false;
                    }
                });
            });
        }).start();

        searchView.setOnQueryTextFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                listView.setVisibility(View.VISIBLE);
                resultView.setVisibility(View.GONE);
            } else {
                listView.setVisibility(View.GONE);
                resultView.setVisibility(View.VISIBLE);
            }
        });


        listView.setOnItemClickListener((parent, view, position, id) -> {
            new Thread(()->{
                String selectedCompany = (String) parent.getItemAtPosition(position);
//                String companyId = DataConverter.companyToID(selectedCompany, getResources().openRawResource(R.raw.companies));
                String companyId = "000";
                ArrayList<Record> selectedRecords = DataConverter.companyToRecords(
                        companyId,
                        getResources().openRawResource(R.raw.records),
                        DataConverter.companyIDToCategory(companyId, getResources().openRawResource(R.raw.categories)),
                        DataConverter.companyIDToSubCategory(companyId, getResources().openRawResource(R.raw.sub_categories)),
                        selectedCompany);
                if(selectedRecords.isEmpty()) return;

                // set list view
                ArrayList<String> resultList = new ArrayList<>();
                for (Record record : selectedRecords) {
                    resultList.add(record.formatToString());
                }
                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, resultList);

                if(getView() == null) return;
                getView().post(()->{
                    resultView.setAdapter(adapter1);
                    listView.setVisibility(View.GONE);
                    resultView.setVisibility(View.VISIBLE);
                    searchView.clearFocus();


                    resultView.setOnItemClickListener((parent1, view1, position1, id1) -> {
                        Intent recordIntent = new Intent(getActivity(), RecordActivity.class);
                        recordIntent.putExtra("selected_record", selectedRecords.get(position1));
                        startActivity(recordIntent);
                    });
                });
            }).start();
        });

        return linearLayout;
    }
}