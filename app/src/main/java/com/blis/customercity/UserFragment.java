package com.blis.customercity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.blis.customercity.Data.DataAPI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

public class UserFragment extends Fragment {
    private Button signinButton;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences loginInfo;
    public UserFragment(Button signinButton, BottomNavigationView bottomNavigationView){
        this.signinButton = signinButton;
        this.bottomNavigationView = bottomNavigationView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_user, container, false);
        assert getContext() != null;
        loginInfo = getContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        String idToken = loginInfo.getString("idToken", null);

        LinearLayout loginLayout = linearLayout.findViewById(R.id.login_layout);
        LinearLayout logoutLayout = linearLayout.findViewById(R.id.logout_layout);
        if(loggedIn && idToken != null){
            loginLayout.setVisibility(View.GONE);
            logoutLayout.setVisibility(View.VISIBLE);
        }
        updateLoginUI(loggedIn, loginLayout, logoutLayout);

        // Handle login
        Button loginButton = linearLayout.findViewById(R.id.login_button);
        TextInputEditText emailInput = linearLayout.findViewById(R.id.login_email_text);
        TextInputEditText passwordInput = linearLayout.findViewById(R.id.login_password_text);
        TextView errorTextView = linearLayout.findViewById(R.id.error_text_view);

        loginButton.setOnClickListener(v -> {
            String emailInputString = String.valueOf(emailInput.getText());
            String passwordInputString = String.valueOf(passwordInput.getText());
            if(emailInputString.isEmpty()){
                errorTextView.setText("請輸入正確電郵地址");
                return;
            }
            if(passwordInputString.isEmpty()){
                errorTextView.setText("請輸入密碼");
                return;
            }
            errorTextView.setText("");
            new Thread(()->{
                String idToken1 = DataAPI.getToken(emailInputString, passwordInputString);
                getActivity().runOnUiThread(() -> {
                    if(idToken1 == null){
                        errorTextView.setText("發生錯誤");
                        return;
                    }
                    SharedPreferences.Editor editor = loginInfo.edit();
                    editor.putString("idToken", idToken1);
                    editor.putBoolean("loggedIn", true);
                    editor.apply();
                    updateLoginUI(true, loginLayout, logoutLayout);
                });
            }).start();
        });

        // Handle Logout
        Button logoutButton = linearLayout.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            // Sign out procedure
            signoutProcedure(loginLayout, logoutLayout);
        });

        // Handle register
        LinearLayout loginLoginLayout = linearLayout.findViewById(R.id.login_login_layout);
        LinearLayout loginRegisterLayout = linearLayout.findViewById(R.id.login_register_layout);
        Button switchToRegisterButton = linearLayout.findViewById(R.id.switch_to_register_button);
        switchToRegisterButton.setOnClickListener(v -> {
            loginLoginLayout.setVisibility(View.GONE);
            loginRegisterLayout.setVisibility(View.VISIBLE);
        });
        Button switchToLoginButton = linearLayout.findViewById(R.id.switch_to_login_button);
        switchToLoginButton.setOnClickListener(v -> {
            loginLoginLayout.setVisibility(View.VISIBLE);
            loginRegisterLayout.setVisibility(View.GONE);
        });

        return linearLayout;
    }

    private void signoutProcedure(LinearLayout loginLayout, LinearLayout logoutLayout) {
        SharedPreferences.Editor editor = loginInfo.edit();
        editor.putString("idToken", null);
        editor.putBoolean("loggedIn", false);
        editor.apply();
        updateLoginUI(false, loginLayout, logoutLayout);
    }
    private void updateLoginUI(boolean loggedIn, LinearLayout loginLayout, LinearLayout logoutLayout){
        if(!loggedIn){
            loginLayout.setVisibility(View.VISIBLE);
            logoutLayout.setVisibility(View.GONE);
            signinButton.setText(R.string.sign_in);
            signinButton.setOnClickListener(v2 -> {
                bottomNavigationView.setSelectedItemId(R.id.nav_user);
            });
        }else{
            loginLayout.setVisibility(View.GONE);
            logoutLayout.setVisibility(View.VISIBLE);
            signinButton.setText(R.string.sign_out);
            signinButton.setOnClickListener(v2 -> {
                signoutProcedure(loginLayout, logoutLayout);
            });
        }
    }
}
