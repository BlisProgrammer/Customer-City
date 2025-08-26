package com.blis.customercity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout linearLayout = (RelativeLayout) inflater.inflate(R.layout.about, container, false);
        Button backButton = linearLayout.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Main main = (Main) getActivity();
            if(main == null) return;
            main.goToSearch();
        });
        return linearLayout;
    }
}
