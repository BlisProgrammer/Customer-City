package com.blis.customercity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class DataConverter {
    /**
     * Search for string with in CSV separated by "|"
     * @param targetColumn Search for string in this column
     * @param targetString Search with this provided string
     * @param resultColumn Return the string in this column
     * @param inputStream Stream of csv file
     * @return Only the first match of the target String
     */
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
    private static final HashMap<String, String> categoryNameToIdMap = new HashMap<>();

    /**
     * Search for id of category
     * @param categoryName name of category
     * @param inputStream {@code getResources().openRawResource(R.raw.categories)}
     * @return Category id
     */
    public static String categoryNameToID(String categoryName, InputStream inputStream){
        if(categoryNameToIdMap.containsKey(categoryName))return categoryNameToIdMap.get(categoryName);
        String catID = searchInCSV("category_cn", categoryName, "id", inputStream);
        categoryNameToIdMap.put(categoryName, catID);
        return catID;
    }

    /**
     * Get all sub categories by category id
     * @param categoryId id of category
     * @param inputStream {@code getResources().openRawResource(R.raw.sub_categories)}
     * @return ArrayList of sub categories
     */
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

    /**
     * Find id of sub category name
     * @param subCategoryName name of sub category
     * @param inputStream {@code getResources().openRawResource(R.raw.sub_categories)}
     * @return string of id
     */
    public static String subCategoryToID(String subCategoryName, InputStream inputStream){
        return searchInCSV("sub_category_cn", subCategoryName, "id", inputStream);
    }

    /**
     * Search for companies with the given company name. Companies with different id could be using the same chinese name.
     * @param companyName name of companies
     * @param inputStream {@code getResources().openRawResource(R.raw.companies)}
     * @return One list of companies which names contains the target string
     */
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

    /**
     * All online companies
     * @param inputStream {@code getResources().openRawResource(R.raw.companies)}
     * @return Arraylist of all company chinese name.
     */
    public static ArrayList<String> getAllOnlineCompanies(InputStream inputStream){
        return searchCompanies("", inputStream);
    }

    /**
     * Search for companies with company id
     * @param companyID id of company
     * @param inputStream {@code getResources().openRawResource(R.raw.companies))}
     * @return string of company chinese name
     */
    public static String companyIDToCompany(String companyID, InputStream inputStream) {
        return searchInCSV("id", companyID, "company_name_cn", inputStream);
    }

    /**
     * Get subcategory of company
     * @param companyID id of company
     * @param inputStream {@code getResources().openRawResource(R.raw.sub_categories)}
     * @return string of company sub category name
     */
    public static String companyIDToSubCategory(String companyID, InputStream inputStream) {
        String subCategoryID = companyID.substring(0, 7);
        return searchInCSV("id", subCategoryID, "sub_category_cn", inputStream);
    }
    /**
     * Get category of company
     * @param companyID id of company
     * @param inputStream {@code getResources().openRawResource(R.raw.categories)}
     * @return string of company category name
     */
    public static String companyIDToCategory(String companyID, InputStream inputStream) {
        String categoryID = companyID.substring(0, 3);
        return searchInCSV("id", categoryID, "category_cn", inputStream);
    }
}
