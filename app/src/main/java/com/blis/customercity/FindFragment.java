package com.blis.customercity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class FindFragment extends Fragment {
    private static String selectedCategory, selectedSubCategory, selectedCompany;
    public static ArrayList<Record> selectedRecords;
    private static ArrayList<String> resultList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.fragment_find, container, false);

        ChipGroup categoryChipGroup = scrollView.findViewById(R.id.category_chip_group);
        ChipGroup subCategoryChipGroup = scrollView.findViewById(R.id.sub_category_chip_group);
        ListView listView = scrollView.findViewById(R.id.resultView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width
                ViewGroup.LayoutParams.WRAP_CONTENT  // Height
        );

        String[] categories = getResources().getStringArray(R.array.default_categories);
        for(String category : categories){
            Chip newChip = new Chip(requireContext());
            newChip.setText(category);
            newChip.setLayoutParams(params);
            newChip.setCheckable(true);
            newChip.setCheckedIconVisible(false);
            newChip.setChipBackgroundColor(getResources().getColorStateList(R.color.chip_background_color_selector, null));
            newChip.setTextColor(getResources().getColorStateList(R.color.chip_text_color_selector, null));
            newChip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#03A9F4")));
            newChip.setChipStrokeWidth(2);
            categoryChipGroup.addView(newChip);
        }
        categoryChipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup chipGroup, @NonNull List<Integer> list) {
                if(list.isEmpty())return;
                listView.setVisibility(View.GONE);
                subCategoryChipGroup.removeAllViews();
                Chip selectedChip = scrollView.findViewById(chipGroup.getCheckedChipId());
                selectedCategory = (String) selectedChip.getText();

                // get list of subcategories
                String categoryID = DataConverter.categoryNameToID(selectedCategory, getResources().openRawResource(R.raw.categories));
                ArrayList<String> subCategories = DataConverter.getSubCategories(categoryID, getResources().openRawResource(R.raw.sub_categories));

                // create next chip group
                for(String category : subCategories){
                    Chip newChip = new Chip(requireContext());
                    newChip.setText(category);
                    newChip.setLayoutParams(params);
                    newChip.setCheckable(true);
                    newChip.setCheckedIconVisible(false);
                    newChip.setChipBackgroundColor(getResources().getColorStateList(R.color.chip_background_color_selector, null));
                    newChip.setTextColor(getResources().getColorStateList(R.color.chip_text_color_selector, null));
                    newChip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#03A9F4")));
                    newChip.setChipStrokeWidth(2);
                    subCategoryChipGroup.addView(newChip);
                }
            }
        });
        subCategoryChipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup chipGroup, @NonNull List<Integer> list) {
                if(list.isEmpty())return;
                listView.setVisibility(View.VISIBLE);
                Chip selectedChip = scrollView.findViewById(chipGroup.getCheckedChipId());
                selectedSubCategory = (String) selectedChip.getText();

                // get list of companies
                String sub_categoryID = DataConverter.subCategoryToID(selectedSubCategory, getResources().openRawResource(R.raw.sub_categories));
                ArrayList<String> companies = DataConverter.getCompanies(sub_categoryID, getResources().openRawResource(R.raw.companies));

                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, companies);
                listView.setAdapter(adapter1);
            }
        });


//        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                // save selected category
//                String selectedItem = parent.getItemAtPosition(position).toString();
//                selectedCategory = selectedItem;
//
//                // get list of subcategories
//                String categoryID = DataConverter.categoryNameToID(selectedItem, getResources().openRawResource(R.raw.categories));
//                ArrayList<String> subCategories = DataConverter.getSubCategories(categoryID, getResources().openRawResource(R.raw.sub_categories));
//
//                // add list to spinner
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, subCategories);
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                typeSpinner.setAdapter(adapter);
//
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                // save selected item
//                String selectedItem = parent.getItemAtPosition(position).toString();
//                selectedType = selectedItem;
//
//                // get list of companies
//                String sub_categoryID = DataConverter.subCategoryToID(selectedItem, getResources().openRawResource(R.raw.sub_categories));
//                ArrayList<String> companies = DataConverter.getCompanies(sub_categoryID, getResources().openRawResource(R.raw.companies));
//
//                // add list to spinner
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, companies);
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                companySpinner.setAdapter(adapter);
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//
//        // Search button
//        final Button button = scrollView.findViewById(R.id.button_id);
//        button.setOnClickListener(v -> {
//            new Thread(()->{
//                // get list of companies
//                if(companySpinner.getSelectedItemPosition() == -1) return;
//                selectedCompany = companySpinner.getSelectedItem().toString();
//                String companyId = DataConverter.companyToID(selectedCompany, getResources().openRawResource(R.raw.companies));
//                selectedRecords = DataConverter.getRecords(companyId, getResources().openRawResource(R.raw.records), selectedCategory, selectedType, selectedCompany);
//                if(selectedRecords.isEmpty()) return;
//                resultList = new ArrayList<>();
//                for (Record record : selectedRecords) {
//                    resultList.add(record.formatToString());
//                }
//                if(getView() == null) return;
//                getView().post(() -> {
//                    // set list view
//                    ListView listView = scrollView.findViewById(R.id.resultView);
//                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, resultList);
//                    listView.setAdapter(adapter1);
//
//                    // set company name
//                    TextView companyTextView = scrollView.findViewById(R.id.company_textview);
//                    companyTextView.setText(String.format("%s(%s/%s)", selectedCompany, selectedCategory, selectedType));
//                });
//            }).start();
//        });
//
//        // List view on click
//        ListView listView = scrollView.findViewById(R.id.resultView);
//        listView.setOnItemClickListener((parent, view, position, id) -> {
//            Intent recordIntent = new Intent(getActivity(), RecordActivity.class);
//            recordIntent.putExtra("selected_record", selectedRecords.get(position));
//            startActivity(recordIntent);
//        });
        return scrollView;
    }
}