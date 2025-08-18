package com.blis.customercity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Main extends AppCompatActivity {
    private DrawerLayout navDrawer;
    private Toolbar toolbar;
    public BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

//        FileHandler.removeFile(this, "saved_list"); // for debugging purposes
//        SharedPreferences preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.clear();
//        editor.apply();

        // on navigate
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Sign in button
        SharedPreferences loginInfo = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        String idToken = loginInfo.getString("idToken", null);
        Button signinButton = findViewById(R.id.account_button);
        Menu navMenu = navigationView.getMenu();
        MenuItem navLoginItem = navMenu.findItem(R.id.nav_login);
        MenuItem navLogoutItem = navMenu.findItem(R.id.nav_logout);
        if(loggedIn && idToken != null) {
            signinButton.setText(R.string.sign_out);
            navLoginItem.setVisible(false);
            navLogoutItem.setVisible(true);
            signinButton.setOnClickListener(v -> {
                // Sign out procedure
                SharedPreferences.Editor editor = loginInfo.edit();
                editor.putString("idToken", null);
                editor.putBoolean("loggedIn", false);
                editor.apply();
                signinButton.setText(R.string.sign_in);
                signinButton.setOnClickListener(v2 -> {
                    bottomNavigationView.setSelectedItemId(R.id.nav_user);
                });
                navLoginItem.setVisible(true);
                navLogoutItem.setVisible(false);
            });
        }else{
            signinButton.setText(R.string.sign_in);
            signinButton.setOnClickListener(v2 -> {
                bottomNavigationView.setSelectedItemId(R.id.nav_user);
            });
            navLoginItem.setVisible(true);
            navLogoutItem.setVisible(false);
        }
        navigationView.invalidate();
        // on navigate
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if(id == R.id.nav_about){
                Intent intent = new Intent(Main.this, AboutActivity.class);
                startActivity(intent);
                navDrawer.closeDrawer(GravityCompat.START);
            }
            if(id == R.id.nav_help){
                Intent intent = new Intent(Main.this, HelpActivity.class);
                startActivity(intent);
                navDrawer.closeDrawer(GravityCompat.START);
            }
            if(id == R.id.nav_logout || id == R.id.nav_login){
                signinButton.performClick();
                navDrawer.closeDrawer(GravityCompat.START);
            }
            return false;
        });

        bottomNavigationView = findViewById(R.id.navigation_view);
        Fragment findFragment = new FindFragment();
        Fragment savedFragment = new SavedFragment();
        Fragment searchFragment = new SearchFragment();
        Fragment userFragment = new UserFragment(navigationView, signinButton, bottomNavigationView);
        Fragment directoryFragment = new DirectoryFragment();
        Fragment cloudFragment = new CloudFragment(navigationView, signinButton, bottomNavigationView);
        setCurrentFragment(findFragment);

        // navigation view
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
    public void setCurrentFragment(Fragment fragment) { // Support function for setting fragment
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
