package com.blis.customercity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Main extends AppCompatActivity {
    DrawerLayout navDrawer;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

//        FileHandler.removeFile(this, "saved_list"); // for debugging purposes
//        SharedPreferences preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.clear();
//        editor.apply();

        Fragment findFragment = new FindFragment();
        Fragment savedFragment = new SavedFragment();
        Fragment searchFragment = new SearchFragment();
        Fragment userFragment = new UserFragment();
        Fragment directoryFragment = new DirectoryFragment();
        Fragment cloudFragment = new CloudFragment();
        setCurrentFragment(findFragment);

        // navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_view);
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if(id == R.id.nav_search){
                setCurrentFragment(findFragment);
                return true;
            }
            if(id == R.id.nav_save){
                setCurrentFragment(cloudFragment);
                return true;
            }
            if(id == R.id.nav_user){
                setCurrentFragment(userFragment);
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

        // navigation drawer
        navDrawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarDrawerToggle drawerToggle = setupDrawerToggle();
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();
        navDrawer.addDrawerListener(drawerToggle);
    }
    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, navDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }
    private void setCurrentFragment(Fragment fragment) { // Support function for setting fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, fragment)
                .commit();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
