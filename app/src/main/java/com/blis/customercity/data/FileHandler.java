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

    public static ArrayList<Company> subCategoryToRecords(Context context, String subCategory){
        ArrayList<OnlineRecord> savedRecords = getSavedRecords(context);
        ArrayList<OnlineRecord> filtered = savedRecords.stream().filter(record -> record.getSubCategory().equalsIgnoreCase(subCategory)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Company> companies = new ArrayList<>();
        filtered.forEach(onlineRecord -> {
            Company company = new Company();
            company.setCompany_name_cn(onlineRecord.getCompany_name_cn());
            company.setCompany_name_en(onlineRecord.getCompany_name_en());
            companies.add(company);
        });
        return companies;
    }
    public static ArrayList<OnlineRecord> companyNameToRecords(Context context, String companyName){
        ArrayList<OnlineRecord> savedRecords = getSavedRecords(context);
        Stream<OnlineRecord> filtered = savedRecords.stream().filter(record -> record.getCompany_name_cn().equalsIgnoreCase(companyName));
        return filtered.collect(Collectors.toCollection(ArrayList::new));
    }
    public static ArrayList<String> getAllLocalCompanies(Context context){
        ArrayList<OnlineRecord> savedRecords = getSavedRecords(context);
        return savedRecords.stream().map(OnlineRecord::getCompany_name_cn).collect(Collectors.toCollection(ArrayList::new));
    }
    public static ArrayList<OnlineRecord> getSavedRecords(Context context){
        String addedRecords = FileHandler.loadFromFile(context, "addedRecords");
        ArrayList<OnlineRecord> onlineRecords = new ArrayList<>();
        if(!addedRecords.isEmpty()){
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<OnlineRecord>>() {}.getType();
            onlineRecords = gson.fromJson(addedRecords, listType);
        }
        return onlineRecords;
    }
    public static void saveSavedRecord(Context context, ArrayList<OnlineRecord> onlineRecords){
        Gson gson = new Gson();
        String jsonString = gson.toJson(onlineRecords);
        FileHandler.saveToFile(context, "addedRecords", jsonString);
    }

    public static void removeFile(Context context, String fileName){
        File dir = context.getFilesDir();
        File file = new File(dir, fileName);
        boolean deleted = file.delete();
    }
    public static void saveToFile(Context context, String filename, String data) {
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
    public static String loadFromFile(Context context, String filename) {
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
