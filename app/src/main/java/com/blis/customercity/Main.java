package com.blis.customercity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
//        FileHandler.removeFile(this, "saved_list"); // for debugging purposes
        Fragment findFragment = new FindFragment();
        Fragment savedFragment = new SavedFragment();
        Fragment searchFragment = new SearchFragment();
        Fragment cloudFragment = new CloudFragment();
        setCurrentFragment(findFragment);
        // navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_view);
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if(id == R.id.nav_saved){
                setCurrentFragment(savedFragment);
                return true;
            }
            if(id == R.id.nav_find){
                setCurrentFragment(findFragment);
                return true;
            }
            if(id == R.id.nav_search){
                setCurrentFragment(searchFragment);
                return true;
            }
            if(id == R.id.nav_cloud){
                setCurrentFragment(cloudFragment);
                return true;
            }
            return false;
        });


        // Fire Firebase
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Customer City");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "name");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, fragment)
                .commit();
    }
}
