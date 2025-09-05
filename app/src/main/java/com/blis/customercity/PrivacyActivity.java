package com.blis.customercity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

public class PrivacyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy);
        TextView textView = findViewById(R.id.privacy_text);
        String htmlString = "<h3 id=\"customer-city-android-\">Customer City Android 應用程式</h3>\n" +
                "<h2 id=\"-\">私隱政策</h2>\n" +
                "<h3 id=\"-\">私隱政策聲明</h3>\n" +
                "<p>如你對本公司之私隱政策有任何疑問，或有意查閱或更正個人資料，請以電郵方式通知我們。</p>\n" +
                "<ul>\n" +
                "<li>電郵地址：cs@customer.city</li>\n" +
                "</ul>\n" +
                "<h4 id=\"-\">本地儲存記錄</h4>\n" +
                "<p>本地儲存記錄允許用戶創建和保存自訂記錄以供將來搜索。本地儲存記錄只會保存在用戶自己手機裏：正常情況下，只有設備所有者才能存取。如果需要清除，只需透過 App 裏的功能刪除記錄，或直接解除安裝應用程式即可。</p>\n" +
                "<h4 id=\"-\">賬號資料</h4>\n" +
                "<p>賬號資料包括電郵地址，會被收集作賬號識別。我們會透過<code>identitytoolkit.googleapis.com</code>管理賬號。綫上儲存記錄會保存在我們的資料庫中，以便將來訪問。</p>\n" +
                "<h4 id=\"-\">登入狀態</h4>\n" +
                "<p>登入後，登入狀態和登入 idToken 會儲存在本地 SharedPreference 中，允許用戶在應用程式重新啟動後保持登入狀態。我們不會收集 SharedPreference 中的數據。如果需要清除，只需透過 App 裏的功能登出賬號，或直接解除安裝應用程式即可。</p>\n" +
                "<h4 id=\"https-\">HTTPS 請求</h4>\n" +
                "<p>我們會向<code>www.customer.city</code>資料來源發出 HTTPS 請求，以檢索公司記錄。如非登入狀態，此類請求不會發送任何個人識別資訊（除了執行請求所必需的訊息，例如 IP 位址）。登入后，idToken會用作個人識別資訊，以檢索該用戶個人賬號綫上儲存記錄等資信。</p>\n" +
                "<h4 id=\"-\">用戶使用數據</h4>\n" +
                "<p>我們可能會收集匿名分析數據，包括應用程式首次訪問、按鈕點擊、頁面訪問等，以改進應用程序。</p>";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(htmlString, Html.FROM_HTML_MODE_COMPACT));
        } else {
            textView.setText(Html.fromHtml(htmlString));
        }

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            FirebaseHandler.logButtonClick(this, this, backButton);
            finish();
        });
    }
}
