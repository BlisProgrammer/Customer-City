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
        public ArrayList<Record> records;
    }
    public Data data;
    public HashMap<String, String> meta;
}

class RegisterResult{
    public int code;
    public String message;
    public ArrayList<HashMap<String, String>> errors;
}

class UpdateResult{
    public String kind, localId, email, idToken, passwordHash;
    public HashMap<String, ArrayList<HashMap<String, String>>> providerUserInfo;
    public boolean emailVerified;
}

public class DataAPI {
    private static final OkHttpClient client = new OkHttpClient();
    private static final HashMap<String, ArrayList<Company>> companies = new HashMap<>();
    private static final HashMap<String, ArrayList<Record>> records = new HashMap<>();

    /**
     * Get companies in a subCategory from API: {@code https://www.customer.city/api/getCompanies/?subCatId={subCatID}}.
     * @param subCatID ID of subcategory in the form of 7 character String, example: {@code 001-001}
     * @return The List of companies if found, or {@code null} if no company with the given ID exists.
     */
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
        return null;
    }

    /**
     * Get records in a company from API: {@code https://www.customer.city/search/?q={companyName}}.
     * @param companyName String of company name cn
     * @return The List of records if found, or <b>empty array</b> if no records with the given company name exists.
     */
    public static ArrayList<Record> companyNameToRecords(String companyName){
        // https://www.customer.city/search/?q=<CompanyName>
        if(records.containsKey(companyName)){
            return records.get(companyName);
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.customer.city/api/search").newBuilder();
        urlBuilder.addQueryParameter("q", companyName);
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
                records.put(companyName, searchResult.data.records);
                return searchResult.data.records;
            } else {
                System.err.println("Request failed with code: " + response.code());
            }
        } catch (IOException e) {
            System.err.println("Error during request: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Get all saved record of an account from {@code  https://www.customer.city/api/getHistory/}
     * @param idToken token for identification of user account
     * @return empty hashmap error, hashmap with arraylist of online record if successful
     */
    public static HashMap<String, ArrayList<Record>> getSavedRecords(String idToken){
        Request request = new Request.Builder()
                .url("https://www.customer.city/api/getHistory/")
                .addHeader("Cookie", "token=" + idToken)
                .build();
        Call call = client.newCall(request);
        try (Response response = call.execute()){
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                Gson gson = new Gson();
                Type type = new TypeToken<HashMap<String, HashMap<String, ArrayList<Record>>>>() {}.getType();
                HashMap<String, HashMap<String, ArrayList<Record>>> hashMap = gson.fromJson(responseBody, type);
                return hashMap.get("data");
            } else {
                System.err.println("Request failed with code: " + response.code());
            }
        } catch (IOException e) {
            System.err.println("Error during request: " + e.getMessage());
        }
        return new HashMap<>();
    }
    /**
     * Toggle history (records saved status) of online database. If it is saved, remove from saved list; if it is not saved, add to saved list. {@code  https://www.customer.city/api/editHistory/}
     * @param idToken token for identification of user account
     * @return true if history is changed, false if not
     */
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
    /**
     * Get token with email and password, through {@code  https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword}
     * @param emailInput account email
     * @param passwordInput account password
     * @return idToken in string if successful, null if login failed
     */
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
    /**
     * Create account with email and password {@code  https://identitytoolkit.googleapis.com/v1/accounts:signUp}
     * @param emailInput new account email
     * @param passwordInput new account password
     * @return {@code "ERROR OCCURRED"} or other error message if error, {@code "SUCCESS"} if account created successfully
     */
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
    /**
     * Send reset password link through email, with: {@code  https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode}
     * @param emailInput account email
     * @return true if email sent successfully, false if email failed to send
     */
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
    /**
     * Change password with idToken and new password {@code  https://identitytoolkit.googleapis.com/v1/accounts:update}
     * @param idToken token for identification of user account
     * @param newPassword password to change into
     * @return true if password changed successfully, false if failed to change password
     */
    public static boolean updatePassword(String idToken, String newPassword){
        // POST https://identitytoolkit.googleapis.com/v1/accounts:setAccountInfo?key=YOUR_API_KEY
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://identitytoolkit.googleapis.com/v1/accounts:update").newBuilder();
        urlBuilder.addQueryParameter("key", "AIzaSyAJ5XXmXlPuHPqRysgfYIFPkF4cwKrCICU");
        String finalUrl = urlBuilder.build().toString();

        HashMap<String, String> body = new HashMap<>();
        body.put("idToken", idToken);
        body.put("password", newPassword);
        Gson gson = new Gson();
        String jsonBody = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);

        try (Response response = call.execute()){
            System.out.println("Response: " + response.body());
            return response.isSuccessful();
        } catch (IOException e) {
            System.err.println("Error during request: " + e.getMessage());
        }
        return false;
    }
}
