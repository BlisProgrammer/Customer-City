package com.blis.customercity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

public class ServiceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service);

        TextView textView = findViewById(R.id.service_text);
        String htmlString = "<p>歡迎您使用Customer City Android 應用程式（以下簡稱“本應用程式”）提供的服務。請仔細閱讀以下服務條款（以下簡稱“本條款”），一旦您使用本應用程式，即表示您同意遵守本條款。</p>\n" +
                "<h4 id=\"-\">服務內容</h4>\n" +
                "<p>本應用程式提供的服務包括但不限於公司聯絡記錄的在線搜索、瀏覽、儲存等。</p>\n" +
                "<h4 id=\"-\">用戶資格</h4>\n" +
                "<p>使用本應用程式服務的用戶必須具備完全的民事行為能力。若用戶為未成年人，需在其監護人指導下使用本應用程式。</p>\n" +
                "<h4 id=\"-\">用戶註冊</h4>\n" +
                "<p>用戶在使用本應用程式某些功能時，可能需要註冊帳戶。註冊帳戶時，請提供準確、完整的個人資料，並保持這些資料的更新。</p>\n" +
                "<p>用戶應妥善保管帳戶密碼，不得將帳戶、密碼轉讓或出借給他人使用。用戶對使用其帳戶和密碼進行的一切活動負全部責任。</p>\n" +
                "<h4 id=\"-\">隱私保護</h4>\n" +
                "<p>本應用程式重視用戶的私隱保護，將按照私隱政策處理用戶的個人資料。</p>\n" +
                "<h4 id=\"-\">知識產權</h4>\n" +
                "<p>本應用程式及其所使用的所有內容（包括但不限於文字、圖像、音頻、視頻等）均受著作權、商標權及其他相關法律的保護。</p>\n" +
                "<h4 id=\"-\">責任限制</h4>\n" +
                "<p>本應用程式對因不可抗力或其他非本應用程式過錯原因造成的服務中斷或損失不承擔責任。</p>\n" +
                "<h4 id=\"-\">條款修改</h4>\n" +
                "<p>本應用程式有權根據需要修改本條款，修改後的條款將在本應用程式公佈。用戶繼續使用本應用程式即視為接受修改後的條款。</p>";

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
