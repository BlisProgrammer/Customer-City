package com.blis.customercity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.blis.customercity.data.DataAPI;
import com.google.android.material.textfield.TextInputEditText;

public class UserFragment extends Fragment {
    private SharedPreferences loginInfo;
    LinearLayout loginLayout, logoutLayout;

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

        loginLayout = linearLayout.findViewById(R.id.login_layout);
        logoutLayout = linearLayout.findViewById(R.id.logout_layout);
        updateLoginUI(loggedIn && idToken != null);

        // Handle login
        Button loginButton = linearLayout.findViewById(R.id.login_button);
        TextInputEditText emailInput = linearLayout.findViewById(R.id.login_email_text);
        TextInputEditText passwordInput = linearLayout.findViewById(R.id.login_password_text);
        TextView errorTextView = linearLayout.findViewById(R.id.error_text_view);

        loginButton.setOnClickListener(v -> {
            closeKeyboard();
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
                String newIdToken = DataAPI.getToken(emailInputString, passwordInputString);
                Main main = (Main) getActivity();
                if(main == null || !isAdded()) return;
                main.runOnUiThread(() -> {
                    if(newIdToken == null){
                        errorTextView.setText("發生錯誤");
                        return;
                    }
                    main.performLogin(newIdToken, emailInputString);
                    main.goToCloud();
                    showToast(requireContext(), "登入成功");
                });
            }).start();
        });

        // Handle register
        LinearLayout loginLoginLayout = linearLayout.findViewById(R.id.login_login_layout);
        LinearLayout loginRegisterLayout = linearLayout.findViewById(R.id.login_register_layout);
        Button switchToRegisterButton = linearLayout.findViewById(R.id.switch_to_register_button);
        switchToRegisterButton.setOnClickListener(v -> {
            closeKeyboard();
            loginLoginLayout.setVisibility(View.GONE);
            loginRegisterLayout.setVisibility(View.VISIBLE);
        });
        Button switchToLoginButton = linearLayout.findViewById(R.id.switch_to_login_button);
        switchToLoginButton.setOnClickListener(v -> {
            closeKeyboard();
            loginLoginLayout.setVisibility(View.VISIBLE);
            loginRegisterLayout.setVisibility(View.GONE);
        });


        Button registerButton = linearLayout.findViewById(R.id.register_button);
        TextInputEditText registerEmail = linearLayout.findViewById(R.id.register_email_text);
        TextInputEditText registerPassword = linearLayout.findViewById(R.id.register_password_text);
        TextInputEditText registerPasswordConfirm = linearLayout.findViewById(R.id.register_password_confirm_text);
        TextView errorRegisterTextView = linearLayout.findViewById(R.id.register_error_text_view);
        registerButton.setOnClickListener(v -> {
            closeKeyboard();
            String emailInputString = String.valueOf(registerEmail.getText());
            String passwordInputString = String.valueOf(registerPassword.getText());
            String passwordConfirmInputString = String.valueOf(registerPasswordConfirm.getText());
            if(emailInputString.isEmpty()){
                errorRegisterTextView.setText("請輸入正確電郵地址");
                return;
            }
            if(passwordInputString.isEmpty()){
                errorRegisterTextView.setText("請輸入密碼");
                return;
            }
            if(passwordConfirmInputString.isEmpty()){
                errorRegisterTextView.setText("請確認密碼");
                return;
            }
            if(!passwordConfirmInputString.equals(passwordInputString)){
                errorRegisterTextView.setText("確認密碼錯誤");
                return;
            }
            errorRegisterTextView.setText("");
            new Thread(()->{
                String message = DataAPI.createAccount(emailInputString, passwordInputString);
                getActivity().runOnUiThread(()->{
                    switch (message) {
                        case "SUCCESS":
                            showToast(getActivity(), "登記成功");
                            loginLoginLayout.setVisibility(View.VISIBLE);
                            loginRegisterLayout.setVisibility(View.GONE);
                            return;
                        case "EMAIL_EXISTS":
                            errorRegisterTextView.setText("電郵地址已被使用");
                            showToast(getActivity(), "登記失敗");
                            return;
                        case "INVALID_EMAIL":
                            errorRegisterTextView.setText("電郵地址錯誤");
                            showToast(getActivity(), "登記失敗");
                            return;
                        default:
                            errorRegisterTextView.setText("登記失敗");
                            showToast(getActivity(), "登記失敗");
                    }
                });
            }).start();
        });
        Button loginResetPasswordButton = linearLayout.findViewById(R.id.login_reset_password_button);
        loginResetPasswordButton.setOnClickListener(v -> {
            closeKeyboard();
            String emailInputString = String.valueOf(emailInput.getText());
            if(emailInputString.isEmpty()){
                errorTextView.setText("請輸入正確電郵地址");
                return;
            }
            errorTextView.setText("");
            new Thread(()->{
                boolean successful = DataAPI.resetPassword(emailInputString);
                getActivity().runOnUiThread(()->{
                    if(!successful){
                        showToast(getActivity(), "重設發生錯誤");
                        return;
                    }
                    showToast(getActivity(), "重設連結己發送到電郵地址");
                });
            }).start();
        });

        return linearLayout;
    }
    private Toast savedToast;
    private void showToast(Context context, String text){
        if(savedToast != null){
            savedToast.cancel();
        }
        savedToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        savedToast.show();
    }
    private void closeKeyboard(){
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateLogin(LinearLayout linearLayout){
        String signedEmail = loginInfo.getString("email", null);
        TextView emailView = linearLayout.findViewById(R.id.email_view);
        emailView.setText(signedEmail);

        TextInputEditText oldPasswordEdit = linearLayout.findViewById(R.id.old_password);
        TextInputEditText newPasswordEdit = linearLayout.findViewById(R.id.new_password);
        TextInputEditText confirmNewPasswordEdit = linearLayout.findViewById(R.id.confirm_new_password);

        Button updatePasswordButton = linearLayout.findViewById(R.id.update_password_button);

        TextView errorView = linearLayout.findViewById(R.id.update_password_error_text_view);
        updatePasswordButton.setOnClickListener(v -> {
            errorView.setText("");
            closeKeyboard();
            String oldPassword = String.valueOf(oldPasswordEdit.getText());
            String newPassword = String.valueOf(newPasswordEdit.getText());
            String confirmNewPassword = String.valueOf(confirmNewPasswordEdit.getText());

            new Thread(()->{
                // check if old password is correct
                if(newPassword.isEmpty() || oldPassword.isEmpty()){
                    if(getActivity() != null && isAdded())
                        getActivity().runOnUiThread(()-> errorView.setText("請填寫密碼"));
                    return;
                }

                if(!newPassword.equals(confirmNewPassword)){
                    if(getActivity() != null && isAdded())
                        getActivity().runOnUiThread(()-> errorView.setText("新密碼驗證錯誤"));
                    return;
                }

                String confirmIdToken = DataAPI.getToken(signedEmail, oldPassword);
                if (confirmIdToken == null){
                    if(getActivity() != null && isAdded())
                        getActivity().runOnUiThread(()-> errorView.setText("舊密碼驗證錯誤"));
                    return;
                }

                // get new password
                boolean successful = DataAPI.updatePassword(confirmIdToken, newPassword);
                getActivity().runOnUiThread(()->{
                    if(!successful){
                        showToast(getActivity(), "更改過程發生錯誤");
                        return;
                    }
                    oldPasswordEdit.setText("");
                    newPasswordEdit.setText("");
                    confirmNewPasswordEdit.setText("");
                    showToast(getActivity(), "更改成功，請重新登入");

                    Main main = (Main) getActivity();
                    main.performLogout();
                });
            }).start();
        });
    }
    public void updateLoginUI(boolean loggedIn){
        if(loginLayout == null || logoutLayout == null){
            return;
        }
        if(!loggedIn){
            loginLayout.setVisibility(View.VISIBLE);
            logoutLayout.setVisibility(View.GONE);
        }else{
            loginLayout.setVisibility(View.GONE);
            logoutLayout.setVisibility(View.VISIBLE);
            updateLogin(logoutLayout);
        }
    }
}
