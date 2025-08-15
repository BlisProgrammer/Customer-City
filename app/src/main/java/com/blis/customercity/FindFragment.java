package com.blis.customercity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.blis.customercity.Data.Company;
import com.blis.customercity.Data.DataAPI;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class FindFragment extends Fragment {
    private static String selectedCategory, selectedSubCategory, selectedCompany;
    public static ArrayList<Record> selectedRecords;
    private static ArrayList<Company> companies;
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
        ProgressBar progressBar = scrollView.findViewById(R.id.progressBar);
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
            newChip.setTextSize(15);
            categoryChipGroup.addView(newChip);
        }
        categoryChipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup chipGroup, @NonNull List<Integer> list) {
                if(list.isEmpty())return;
                listView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
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
                    newChip.setTextSize(15);
                    subCategoryChipGroup.addView(newChip);
                }
            }
        });
        subCategoryChipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup chipGroup, @NonNull List<Integer> list) {
                if(list.isEmpty())return;
                listView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                Chip selectedChip = scrollView.findViewById(chipGroup.getCheckedChipId());
                selectedSubCategory = (String) selectedChip.getText();

                // get list of companies
                String sub_categoryID = DataConverter.subCategoryToID(selectedSubCategory, getResources().openRawResource(R.raw.sub_categories));
//                ArrayList<String> companies = DataConverter.getCompanies(sub_categoryID, getResources().openRawResource(R.raw.companies));

                new Thread(()->{
                    companies = DataAPI.subCatIDToCompanies(sub_categoryID);
                    ArrayList<String> companyNames = new ArrayList<>();
                    for(Company company : companies){
                        companyNames.add(company.getCompany_name_cn());
                    }
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, companyNames);
                    if(getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        listView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        listView.setAdapter(adapter1);
                    });
                }).start();
            }
        });
        // List view on click
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Bundle args = new Bundle();
            args.putString("company_id", companies.get(position).getId());

            Fragment resultFragment = new ResultFragment();
            resultFragment.setArguments(args);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.flFragment, resultFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
        return scrollView;
    }
}