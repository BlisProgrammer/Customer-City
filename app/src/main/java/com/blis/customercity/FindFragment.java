package com.blis.customercity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.blis.customercity.data.Company;
import com.blis.customercity.data.DataAPI;
import com.blis.customercity.data.FileHandler;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;


public class FindFragment extends Fragment {
    private class FindChips extends Chip{
        public FindChips(Context context, String category, LinearLayout.LayoutParams params) {
            super(context);
            setText(category);
            setLayoutParams(params);
            setCheckable(true);
            setCheckedIconVisible(false);
            setChipBackgroundColor(getResources().getColorStateList(R.color.chip_background_color_selector, null));
            setTextColor(getResources().getColorStateList(R.color.chip_text_color_selector, null));
            setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#03A9F4")));
            setChipStrokeWidth(2);
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }
    }
    private String selectedCategory, selectedSubCategory;
    private Thread companyListThread;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        NestedScrollView scrollView = (NestedScrollView) inflater.inflate(R.layout.fragment_find, container, false);

        LinearLayout subCategoryLayout = scrollView.findViewById(R.id.sub_category_layout);
        LinearLayout resultLayout = scrollView.findViewById(R.id.result_layout);

        ChipGroup categoryChipGroup = scrollView.findViewById(R.id.category_chip_group);
        ChipGroup subCategoryChipGroup = scrollView.findViewById(R.id.sub_category_chip_group);
        ProgressBar progressBar = scrollView.findViewById(R.id.progressBar);
        ListView resultView = scrollView.findViewById(R.id.resultView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width
                ViewGroup.LayoutParams.WRAP_CONTENT  // Height
        );

        String[] categories = getResources().getStringArray(R.array.default_categories);
        for(String category : categories){
            Chip newChip = new FindChips(requireContext(), category, params);
            categoryChipGroup.addView(newChip);
        }
        categoryChipGroup.setOnCheckedStateChangeListener((chipGroup, list) -> {
            if(list.isEmpty())return;
            if(companyListThread != null && companyListThread.isAlive()){
                companyListThread.interrupt();
            }
            resultLayout.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.GONE);
            subCategoryChipGroup.removeAllViews();
            Chip selectedChip = scrollView.findViewById(chipGroup.getCheckedChipId());
            selectedCategory = (String) selectedChip.getText();

            // get list of subcategories
            String categoryID = DataConverter.categoryNameToID(selectedCategory, getResources().openRawResource(R.raw.categories));
            ArrayList<String> subCategories = DataConverter.getSubCategories(categoryID, getResources().openRawResource(R.raw.sub_categories));

            // create next chip group
            for(String category : subCategories){
                Chip newChip = new FindChips(requireContext(), category, params);
                subCategoryChipGroup.addView(newChip);
            }
            subCategoryLayout.setVisibility(View.VISIBLE);
        });
        subCategoryChipGroup.setOnCheckedStateChangeListener((chipGroup, list) -> {
            if(list.isEmpty())return;
            resultLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            Chip selectedChip = scrollView.findViewById(chipGroup.getCheckedChipId());
            selectedSubCategory = (String) selectedChip.getText();

//                ArrayList<String> companies = DataConverter.getCompanies(sub_categoryID, getResources().openRawResource(R.raw.companies));

            if(companyListThread != null && companyListThread.isAlive()){
                companyListThread.interrupt();
            }
            companyListThread = new Thread(()->{
                // get list of companies
                String sub_categoryID = DataConverter.subCategoryToID(selectedSubCategory, getResources().openRawResource(R.raw.sub_categories));

                ArrayList<Company> onlineCompanies = DataAPI.subCatIDToCompanies(sub_categoryID);

                if(onlineCompanies == null){
                    onlineCompanies = new ArrayList<>();
                    onlineCompanies.add(Company.getErrorCompany());
                    resultView.setOnItemClickListener((parent, view, position, id) -> {
                        FirebaseHandler.logItemClick(requireContext(), this, parent, view, position, id);
                        Toast.makeText(requireContext(), "發生錯誤，請稍後嘗試", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                ArrayList<Company> offlineCompanies = FileHandler.subCategoryToRecords(requireContext(), selectedSubCategory);
                ArrayList<Company> companies = new ArrayList<>();
                companies.addAll(offlineCompanies);
                companies.addAll(onlineCompanies);
                // List view on click
                resultView.setOnItemClickListener((parent, view, position, id) -> {
                    FirebaseHandler.logItemClick(requireContext(), this, parent, view, position, id);
                    Bundle args = new Bundle();
                    args.putString("company_name", parent.getItemAtPosition(position).toString());

                    Fragment resultFragment = new ResultFragment();
                    resultFragment.setArguments(args);

                    Main main = (Main) getActivity();
                    if(main == null || !isAdded())return;
                    main.setCurrentFragment(resultFragment);
                });
                ArrayList<String> companyNames = companies.stream().map(Company::getCompany_name_cn).collect(Collectors.toCollection(ArrayList::new));
                if(!isAdded())return;
                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, companyNames);
                if(getActivity() == null) return;
                if(Thread.interrupted())return;
                getActivity().runOnUiThread(() -> {
                    resultLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    resultView.setAdapter(adapter1);
                });
            });
            companyListThread.start();
        });

        LinearLayout filterSection = scrollView.findViewById(R.id.filter_section);
        ListView searchListView = scrollView.findViewById(R.id.search_list_view);
        SearchView searchView = scrollView.findViewById(R.id.search_view);
        new Thread(()->{
            if(!isAdded())return;
            ArrayList<String> allOnlineCompanies = DataConverter.getAllOnlineCompanies(getResources().openRawResource(R.raw.companies));
            ArrayList<String> allLocalCompanies = FileHandler.getAllLocalCompanies(requireContext());
            ArrayList<String> allCompanies = new ArrayList<>();
            allCompanies.addAll(allLocalCompanies);
            allCompanies.addAll(allOnlineCompanies);
            LinkedHashSet<String> uniqueElements = new LinkedHashSet<>(allCompanies);
            allCompanies = new ArrayList<>(uniqueElements);
            if(!isAdded())return;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, allCompanies);
            if(getView() == null) return;
            getView().post(()->{
                if(!isAdded())return;
                searchListView.setAdapter(adapter);
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
                searchListView.setVisibility(View.VISIBLE);
                filterSection.setVisibility(View.GONE);
            } else {
                searchListView.setVisibility(View.GONE);
                filterSection.setVisibility(View.VISIBLE);
            }
        });


        searchListView.setOnItemClickListener((parent, view, position, id) -> {
            FirebaseHandler.logItemClick(requireContext(), this, parent, view, position, id);
            
            String selectedCompany = (String) parent.getItemAtPosition(position);
            Bundle args = new Bundle();
            args.putString("company_name", selectedCompany);

            Fragment resultFragment = new ResultFragment();
            resultFragment.setArguments(args);

            Main main = (Main) getActivity();
            if(main == null || !isAdded())return;
            main.setCurrentFragment(resultFragment);
        });

        return scrollView;
    }
}