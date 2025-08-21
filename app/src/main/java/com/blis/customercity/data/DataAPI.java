package com.blis.customercity.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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

class RegisterResult{
    public int code;
    public String message;
    public ArrayList<HashMap<String, String>> errors;
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
    public static ArrayList<OnlineRecord> companyIDtoRecords(ArrayList<String> companyIDs){
        // https://www.customer.city/api/search/?ids={companyID}
        ArrayList<String> ids = new ArrayList<>();
        for(String companyId : companyIDs){
            String[] id = companyId.split("-");
            ids.add(id[2]);
        }
        String connectedId = String.join(",", ids);

        if(records.containsKey(connectedId)){
            return records.get(connectedId);
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.customer.city/api/search").newBuilder();
        urlBuilder.addQueryParameter("ids", connectedId);
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
                records.put(connectedId, searchResult.data.records);
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
    public static String getToken(String emailInput, String passwordInput){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword").newBuilder();
        urlBuilder.addQueryParameter("key", "AIzaSyAJ5XXmXlPuHPqRysgfYIFPkF4cwKrCICU");
        String finalUrl = urlBuilder.build().toString();

        HashMap<String, String> body = new HashMap<>();
        body.put("email", emailInput);
        body.put("password", passwordInput);
        body.put("returnSecureToken", "true");
        Gson gson = new Gson();
        String jsonBody = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);

        try (Response response = call.execute()){
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                Type type = new TypeToken<HashMap<String, String>>() {}.getType();
                HashMap<String, String> hashMap = gson.fromJson(responseBody, type);

                // Login in successful, show logged in screen
                return hashMap.get("idToken");
            } else {
                System.err.println("Request failed with code: " + response.code());
            }
        } catch (IOException e) {
            System.err.println("Error during request: " + e.getMessage());
        }
        return null;
    }
    public static String createAccount(String emailInput, String passwordInput){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://identitytoolkit.googleapis.com/v1/accounts:signUp").newBuilder();
        urlBuilder.addQueryParameter("key", "AIzaSyAJ5XXmXlPuHPqRysgfYIFPkF4cwKrCICU");
        String finalUrl = urlBuilder.build().toString();

        HashMap<String, String> body = new HashMap<>();
        body.put("email", emailInput);
        body.put("password", passwordInput);
        body.put("returnSecureToken", "true");
        Gson gson = new Gson();
        String jsonBody = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);

        try (Response response = call.execute()){
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                return "SUCCESS";
            } else {
                int responseCode = response.code();
                System.err.println("Request failed with code: " + responseCode);
                if(responseCode == 400){
                    Type type = new TypeToken<HashMap<String, RegisterResult>>() {}.getType();
                    HashMap<String, RegisterResult> registerResult = gson.fromJson(responseBody, type);
                    String errorMessage = registerResult.get("error").message;
                    if(errorMessage != null){
                        return errorMessage;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error during request: " + e.getMessage());
        }
        return "ERROR OCCURRED";
    }
    public static boolean resetPassword(String emailInput){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode").newBuilder();
        urlBuilder.addQueryParameter("key", "AIzaSyAJ5XXmXlPuHPqRysgfYIFPkF4cwKrCICU");
        String finalUrl = urlBuilder.build().toString();

        HashMap<String, String> body = new HashMap<>();
        body.put("email", emailInput);
        body.put("requestType", "PASSWORD_RESET");
        Gson gson = new Gson();
        String jsonBody = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);

        try (Response response = call.execute()){
            return response.isSuccessful();
        } catch (IOException e) {
            System.err.println("Error during request: " + e.getMessage());
        }
        return false;
    }
}
