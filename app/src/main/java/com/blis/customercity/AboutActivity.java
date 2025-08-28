package com.blis.customercity;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            FirebaseHandler.logButtonClick(this, this, backButton);
            finish();
        });
    }
}
