package com.blis.customercity;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class FindFragment extends Fragment {
    private static String selectedCategory, selectedType, selectedCompany;
    public static ArrayList<Record> selectedRecords;
    private static ArrayList<String> resultList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_find, container, false);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.default_spinner_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner categorySpinner = linearLayout.findViewById(R.id.category_spinner);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // save selected category
                String selectedItem = parent.getItemAtPosition(position).toString();
                selectedCategory = selectedItem;

                // get list of subcategories
                String categoryID = DataConverter.categoryNameToID(selectedItem, getResources().openRawResource(R.raw.categories));
                ArrayList<String> subCategories = DataConverter.getSubCategories(categoryID, getResources().openRawResource(R.raw.sub_categories));

                // add list to spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, subCategories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                final Spinner typeSpinner = linearLayout.findViewById(R.id.type_spinner);
                typeSpinner.setAdapter(adapter);
                typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // save selected item
                        String selectedItem = parent.getItemAtPosition(position).toString();
                        selectedType = selectedItem;

                        // get list of companies
                        String sub_categoryID = DataConverter.subCategoryToID(selectedItem, getResources().openRawResource(R.raw.sub_categories));
                        ArrayList<String> companies = DataConverter.getCompanies(sub_categoryID, getResources().openRawResource(R.raw.companies));

                        // add list to spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, companies);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        final Spinner companySpinner = linearLayout.findViewById(R.id.company_spinner);
                        companySpinner.setAdapter(adapter);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Search button
        final Button button = linearLayout.findViewById(R.id.button_id);
        button.setOnClickListener(v -> {
            // get list of companies
            Spinner companySpinner = linearLayout.findViewById(R.id.company_spinner);
            if(companySpinner.getSelectedItemPosition() == -1) return;
            selectedCompany = companySpinner.getSelectedItem().toString();
            String companyId = DataConverter.companyToID(selectedCompany, getResources().openRawResource(R.raw.companies));
            selectedRecords = DataConverter.getRecords(companyId, getResources().openRawResource(R.raw.records), selectedCategory, selectedType, selectedCompany);
            if(selectedRecords.isEmpty()) return;

            // set list view
            ListView listView = linearLayout.findViewById(R.id.resultView);
            resultList = new ArrayList<>();
            for (Record record : selectedRecords) {
                resultList.add(record.formatToString());
            }
            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, resultList);
            listView.setAdapter(adapter1);

            // set company name
            TextView companyTextView = linearLayout.findViewById(R.id.company_textview);
            companyTextView.setText(String.format("%s(%s/%s)", selectedCompany, selectedCategory, selectedType));
        });

        // List view on click
        ListView listView = linearLayout.findViewById(R.id.resultView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent recordIntent = new Intent(getActivity(), RecordActivity.class);
                recordIntent.putExtra("selected_record", selectedRecords.get(position));
                startActivity(recordIntent);
            }
        });
        return linearLayout;
    }
}