package com.blis.customercity.Data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class CompanyResult{
    private ArrayList<Company> data;
    private HashMap<String, String> meta;

    public ArrayList<Company> getData() {
        return data;
    }
    public void setData(ArrayList<Company> data) {
        this.data = data;
    }
    public HashMap<String, String> getMeta() {
        return meta;
    }
    public void setMeta(HashMap<String, String> meta) {
        this.meta = meta;
    }
}

class SearchResult{
    static class Data{
        public ArrayList<Company> companies;
        public ArrayList<OnlineRecord> records;
    }
    public Data data;
    public HashMap<String, String> meta;
}

public class DataAPI {
    private static final OkHttpClient client = new OkHttpClient();
    private static final HashMap<String, ArrayList<Company>> companies = new HashMap<>();
    private static final HashMap<String, ArrayList<OnlineRecord>> records = new HashMap<>();
    public static ArrayList<Company> subCatIDToCompanies(String subCatID){
        // https://www.customer.city/api/getCompanies/?subCatId={subCatID}

        if(companies.containsKey(subCatID)){
            return companies.get(subCatID);
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.customer.city/api/getCompanies").newBuilder();
        urlBuilder.addQueryParameter("subCatId", subCatID);
        String finalUrl = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(finalUrl)
                .build();
        Call call = client.newCall(request);
        try (Response response = call.execute()){
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                Gson gson = new Gson();
                CompanyResult companyResult = gson.fromJson(responseBody, CompanyResult.class);
                companies.put(subCatID, companyResult.getData());
                return companyResult.getData();
            } else {
                System.err.println("Request failed with code: " + response.code());
            }
        } catch (IOException e) {
            System.err.println("Error during request: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    public static ArrayList<OnlineRecord> companyIDtoRecords(String companyID){
        // https://www.customer.city/api/search/?ids={companyID}
        String[] id = companyID.split("-");

        if(records.containsKey(id[2])){
            return records.get(id[2]);
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.customer.city/api/search").newBuilder();
        urlBuilder.addQueryParameter("ids", id[2]);
        String finalUrl = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(finalUrl)
                .build();
        Call call = client.newCall(request);
        try (Response response = call.execute()){
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                Gson gson = new Gson();
                SearchResult searchResult = gson.fromJson(responseBody, SearchResult.class);
                return searchResult.data.records;
            } else {
                System.err.println("Request failed with code: " + response.code());
            }
        } catch (IOException e) {
            System.err.println("Error during request: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    public static HashMap<String, ArrayList<OnlineRecord>> getSavedRecords(String idToken){
        Request request = new Request.Builder()
                .url("https://www.customer.city/api/getHistory/")
                .addHeader("Cookie", "token=" + idToken)
                .build();
        Call call = client.newCall(request);
        try (Response response = call.execute()){
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                Gson gson = new Gson();
                Type type = new TypeToken<HashMap<String, HashMap<String, ArrayList<OnlineRecord>>>>() {}.getType();
                HashMap<String, HashMap<String, ArrayList<OnlineRecord>>> hashMap = gson.fromJson(responseBody, type);
                return hashMap.get("data");
            } else {
                System.err.println("Request failed with code: " + response.code());
            }
        } catch (IOException e) {
            System.err.println("Error during request: " + e.getMessage());
        }
        return new HashMap<>();
    }
    public static boolean updateHistory(String idToken, String recordID){
        HttpUrl originalUrl = HttpUrl.parse("https://www.customer.city/api/editHistory/");
        HttpUrl.Builder urlBuilder = originalUrl.newBuilder();
        urlBuilder.addQueryParameter("id", recordID);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Cookie", "token=" + idToken)
                .build();
        Call call = client.newCall(request);
        try (Response response = call.execute()){
            if (response.isSuccessful()) {
                return true;
            } else {
                System.err.println("Request failed with code: " + response.code());
            }
        } catch (IOException e) {
            System.err.println("Error during request: " + e.getMessage());
        }
        return false;
    }
}
