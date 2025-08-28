package com.blis.customercity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.blis.customercity.data.FileHandler;
import com.blis.customercity.data.OnlineRecord;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class AddFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView linearLayout = (ScrollView) inflater.inflate(R.layout.fragment_add, container, false);

        // set spinner
        Spinner categorySpinner = linearLayout.findViewById(R.id.category_spinner);
        Spinner subCategorySpinner = linearLayout.findViewById(R.id.sub_category_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.default_categories, // Your data source (string array from strings.xml)
                android.R.layout.simple_spinner_item // Default layout for spinner items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                new Thread(()->{
                    String selectedCategory = parent.getItemAtPosition(position).toString();
                    String categoryID = DataConverter.categoryNameToID(selectedCategory, getResources().openRawResource(R.raw.categories));
                    ArrayList<String> subCategories = DataConverter.getSubCategories(categoryID, getResources().openRawResource(R.raw.sub_categories));

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, subCategories);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    if(getActivity() == null || !isAdded())return;
                    getActivity().runOnUiThread(()-> subCategorySpinner.setAdapter(adapter));
                }).start();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Find views
        TextInputEditText companyNameEdit = linearLayout.findViewById(R.id.add_company_name_edit);
        TextInputEditText companyScopeEdit = linearLayout.findViewById(R.id.add_company_scope_edit);
        TextInputEditText companyAddressEdit = linearLayout.findViewById(R.id.add_company_address_edit);
        TextInputEditText companyDetailEdit = linearLayout.findViewById(R.id.add_company_detail_edit);
        TextInputEditText companyEmailEdit = linearLayout.findViewById(R.id.add_company_email_edit);
        TextInputEditText companyHintEdit = linearLayout.findViewById(R.id.add_company_hint_edit);
        TextInputEditText companyHotlineEdit = linearLayout.findViewById(R.id.add_company_hotline_edit);

        // On save record
        Button saveButton = linearLayout.findViewById(R.id.save_button);
        TextView errorTextView = linearLayout.findViewById(R.id.error_text_view);
        saveButton.setOnClickListener(v -> {
            FirebaseHandler.logButtonClick(requireContext(), this, saveButton);
            errorTextView.setText("");

            String companyName = String.valueOf(companyNameEdit.getText());
            if(companyName.isEmpty()){
                errorTextView.setText("請輸入公司名稱");
                return;
            }
            String companyScope = String.valueOf(companyScopeEdit.getText());
            String companyAddress = String.valueOf(companyAddressEdit.getText());
            String companyDetail = String.valueOf(companyDetailEdit.getText());
            String companyEmail = String.valueOf(companyEmailEdit.getText());
            String companyHint = String.valueOf(companyHintEdit.getText());
            String companyHotline = String.valueOf(companyHotlineEdit.getText());
            if(companyScope.isEmpty()
                    && companyAddress.isEmpty()
                    && companyDetail.isEmpty()
                    && companyEmail.isEmpty()
                    && companyHint.isEmpty()
                    && companyHotline.isEmpty()){
                errorTextView.setText("請至少輸入一項資訊");
                return;
            }

            String category = categorySpinner.getSelectedItem().toString();
            String subCategory = subCategorySpinner.getSelectedItem().toString();

            OnlineRecord onlineRecord = new OnlineRecord();
            onlineRecord.setCompany_name_cn(companyName);
            onlineRecord.setServices_scope_cn(companyScope);
            onlineRecord.setService_hotline(companyHotline);
            onlineRecord.setEmail(companyEmail);
            onlineRecord.setAddress_cn(companyAddress);
            onlineRecord.setAdded_detail_cn(companyDetail);
            onlineRecord.setTips_cn(companyHint);

            onlineRecord.setCategory(category);
            onlineRecord.setSubCategory(subCategory);
//            onlineRecord.setCompany_id(DataConverter.generateCompanyID(category, subCategory, getResources().openRawResource(R.raw.categories), getResources().openRawResource(R.raw.sub_categories)));

            ArrayList<OnlineRecord> onlineRecords = FileHandler.getSavedRecords(requireContext());
            onlineRecords.add(0, onlineRecord);
            FileHandler.saveSavedRecord(requireContext(), onlineRecords);

            Toast.makeText(requireContext(),"儲存成功", Toast.LENGTH_SHORT).show();
            companyNameEdit.setText("");
            companyScopeEdit.setText("");
            companyAddressEdit.setText("");
            companyDetailEdit.setText("");
            companyEmailEdit.setText("");
            companyHintEdit.setText("");
            companyHotlineEdit.setText("");
        });

        return linearLayout;
    }
}
