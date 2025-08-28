package com.blis.customercity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;

public class AboutFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.about, container, false);
        Button searchButton = linearLayout.findViewById(R.id.back_button);
        searchButton.setOnClickListener(v -> {
            FirebaseHandler.logButtonClick(requireContext(), this, searchButton);

            Main main = (Main) getActivity();
            if(main == null) return;
            main.goToSearch();
        });
        return linearLayout;
    }
}
