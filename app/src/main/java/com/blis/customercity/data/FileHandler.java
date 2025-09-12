package com.blis.customercity.data;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHandler {

    /**
     * Search in locally saved records, find all Records in given sub-category (ignore case).
     * @param context Context object
     * @param subCategory Sub category name
     * @return Arraylist with all locally saved records
     */
    public static ArrayList<Company> subCategoryToRecords(Context context, String subCategory){
        ArrayList<Record> savedRecords = getSavedRecords(context);
        ArrayList<Record> filtered = savedRecords.stream().filter(record -> record.getSubCategory().equalsIgnoreCase(subCategory)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Company> companies = new ArrayList<>();
        filtered.forEach(onlineRecord -> {
            Company company = new Company();
            company.setCompany_name_cn(onlineRecord.getCompany_name_cn());
            company.setCompany_name_en(onlineRecord.getCompany_name_en());
            companies.add(company);
        });
        return companies;
    }

    /**
     * Get all records with the given company name
     * @param context Context of application: {@code requireContext()}
     * @param companyName Target name of company
     * @return all records of given company name
     */
    public static ArrayList<Record> companyNameToRecords(Context context, String companyName){
        ArrayList<Record> savedRecords = getSavedRecords(context);
        Stream<Record> filtered = savedRecords.stream().filter(record -> record.getCompany_name_cn().equalsIgnoreCase(companyName));
        return filtered.collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Load all locally saved companies chinese name from file
     * @param context Context of application: {@code requireContext()}
     * @return all chinese name of locally saved companies
     */
    public static ArrayList<String> getAllLocalCompanies(Context context){
        ArrayList<Record> savedRecords = getSavedRecords(context);
        return savedRecords.stream().map(Record::getCompany_name_cn).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Load saved records from file
     * @param context Context of application: {@code requireContext()}
     * @return List of records in the form of OnlineRecord
     * @see Record
     */
    public static ArrayList<Record> getSavedRecords(Context context){
        String addedRecords = FileHandler.loadFromFile(context, "addedRecords");
        ArrayList<Record> records = new ArrayList<>();
        if(!addedRecords.isEmpty()){
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Record>>() {}.getType();
            records = gson.fromJson(addedRecords, listType);
        }
        return records;
    }

    /**
     * Save all records into local file
     * @param context Context of application: {@code requireContext()}
     * @param records List of all records to be saved
     */
    public static void saveSavedRecord(Context context, ArrayList<Record> records){
        Gson gson = new Gson();
        String jsonString = gson.toJson(records);
        FileHandler.saveToFile(context, "addedRecords", jsonString);
    }

    /**
     * For debugging purpose: remove all saved files
     * @param context Context of application: {@code requireContext()}
     * @param fileName Name of file to be deleted
     */
    public static void removeFile(Context context, String fileName){
        File dir = context.getFilesDir();
        File file = new File(dir, fileName);
        boolean deleted = file.delete();
    }

    /**
     * Save string data to file, overwrite if file exist already.
     * @param context Context of application: {@code requireContext()}
     * @param filename Name of the file
     * @param data Data to be stored
     */
    private static void saveToFile(Context context, String filename, String data) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE); // MODE_PRIVATE makes the file only accessible by your app
            fos.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Load string data from file
     * @param context Context of application: {@code requireContext()}
     * @param filename Name of the file
     * @return String data
     */
    private static String loadFromFile(Context context, String filename) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = context.openFileInput(filename);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n"); // Append newline if you saved line by line
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
                if (isr != null) isr.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
