package com.blis.customercity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
public class DataConverter {
    private static String searchInCSV(String targetColumn, String targetString, String resultColumn, InputStream inputStream){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            // get first line
            String[] firstLine =reader.readLine().split("\\|");
            int resultIndex = 0;
            int targetIndex = 1;
            for (int i = 0; i < firstLine.length; i++) {
                if(firstLine[i].equals(resultColumn)){
                    resultIndex = i;
                    continue;
                }
                if(firstLine[i].equals(targetColumn)){
                    targetIndex = i;
                }
            }
            while((line=reader.readLine())!= null){
                String[] data = line.split("\\|");
                if(data[targetIndex].equals(targetString)){
                    reader.close();
                    return data[resultIndex];
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "Not found";
    }
    public static String categoryNameToID(String categoryName, InputStream inputStream){
        return searchInCSV("category_cn", categoryName, "id", inputStream);
    }
    public static ArrayList<String> getSubCategories(String categoryId, InputStream inputStream) {
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            // get first line
            String[] firstLine =reader.readLine().split("\\|");
            int cat_idColumn = 0;
            int sub_category_cnColumn = 1;
            for (int i = 0; i < firstLine.length; i++) {
                if(firstLine[i].equals("cat_id")){
                    cat_idColumn = i;
                    continue;
                }
                if(firstLine[i].equals("sub_category_cn")){
                    sub_category_cnColumn = i;
                }
            }
            ArrayList<String> allSubCategories = new ArrayList<>();
            while((line=reader.readLine())!= null){
                String[] data = line.split("\\|");
                if(data[cat_idColumn].equals(categoryId)){
                    allSubCategories.add(data[sub_category_cnColumn]);
                }
            }
            reader.close();
            return allSubCategories;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String subCategoryToID(String subCategoryName, InputStream inputStream){
        return searchInCSV("sub_category_cn", subCategoryName, "id", inputStream);
    }
    public static ArrayList<String> searchCompanies(String companyName, InputStream inputStream){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            // get first line
            String[] firstLine =reader.readLine().split("\\|");
            int company_name_cnColumn = 2;
            for (int i = 0; i < firstLine.length; i++) {
                if(firstLine[i].equals("company_name_cn")){
                    company_name_cnColumn = i;
                    break;
                }
            }

            ArrayList<String> result = new ArrayList<>();
            while((line=reader.readLine())!= null){
                if(line.contains(companyName)){
                    String companyString = line.split("\\|")[company_name_cnColumn];
                    if(result.contains(companyString))continue;
                    result.add(companyString);
                }
            }
            reader.close();
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static ArrayList<String> getAllOnlineCompanies(InputStream inputStream){
        return searchCompanies("", inputStream);
    }
    public static String companyIDToCompany(String companyID, InputStream inputStream) {
        return searchInCSV("id", companyID, "company_name_cn", inputStream);
    }
    public static String companyIDToSubCategory(String companyID, InputStream inputStream) {
        String subCategoryID = companyID.substring(0, 7);
        return searchInCSV("id", subCategoryID, "sub_category_cn", inputStream);
    }
    public static String companyIDToCategory(String companyID, InputStream inputStream) {
        String categoryID = companyID.substring(0, 3);
        return searchInCSV("id", categoryID, "category_cn", inputStream);
    }

    public static ArrayList<String> companyNameToIDs(String companyName, InputStream inputStream){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            // get first line
            String[] firstLine =reader.readLine().split("\\|");
            int companyNameColumn = 2;
            int idColumn = 0;
            for (int i = 0; i < firstLine.length; i++) {
                if(firstLine[i].equals("company_name_cn")){
                    companyNameColumn = i;
                }
                if(firstLine[i].equals("id")){
                    idColumn = i;
                }
            }
            ArrayList<String> allIDs = new ArrayList<>();
            while((line=reader.readLine())!= null){
                String[] data = line.split("\\|", -1);
                if(data[companyNameColumn].equals(companyName)){
                    allIDs.add(data[idColumn]);
                }
            }
            reader.close();
            return allIDs;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
