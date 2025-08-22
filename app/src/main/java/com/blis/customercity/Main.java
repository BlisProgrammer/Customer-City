package com.blis.customercity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

    private Button signinButton;
    private SharedPreferences loginInfo;
    private int clickedNavigationItemID;

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
        loginInfo = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        String idToken = loginInfo.getString("idToken", null);
        String email = loginInfo.getString("email", null);
        signinButton = findViewById(R.id.account_button);
        if(loggedIn && idToken != null) {
            performLogin(idToken, email);
        }else{
            performLogout();
        }

        // on navigate
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            clickedNavigationItemID = menuItem.getItemId();
            navDrawer.closeDrawer(GravityCompat.START);
            return true;
        });

        bottomNavigationView = findViewById(R.id.navigation_view);
        Fragment findFragment = new FindFragment();
        Fragment userFragment = new UserFragment();
        Fragment cloudFragment = new CloudFragment();
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("main_fragment");
        if(currentFragment != null) {
            setCurrentFragment(currentFragment);
        }else{
            setCurrentFragment(findFragment);
        }

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
        navDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                int id = clickedNavigationItemID;
                if(id == R.id.nav_about){
                    Intent intent = new Intent(Main.this, AboutActivity.class);
                    startActivity(intent);
                }
                if(id == R.id.nav_help){
                    Intent intent = new Intent(Main.this, HelpActivity.class);
                    startActivity(intent);
                }
                if(id == R.id.nav_logout){
                    performLogout();
                }
                if(id == R.id.nav_login){
                    bottomNavigationView.setSelectedItemId(R.id.nav_user);
                }
                clickedNavigationItemID = 0;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    public void performLogin(String idToken, String emailInputString){
        SharedPreferences.Editor editor = loginInfo.edit();
        editor.putString("idToken", idToken);
        editor.putBoolean("loggedIn", true);
        editor.putString("email", emailInputString);
        editor.apply();
        signinButton.setText(R.string.sign_out);
        signinButton.setOnClickListener(v2 -> performLogout());

        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu navMenu = navigationView.getMenu();
        MenuItem navLoginItem = navMenu.findItem(R.id.nav_login);
        MenuItem navLogoutItem = navMenu.findItem(R.id.nav_logout);
        navLogoutItem.setVisible(true);
        navLoginItem.setVisible(false);
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("main_fragment");
        if(currentFragment instanceof UserFragment){
            UserFragment findFragment = (UserFragment) currentFragment;
            findFragment.updateLoginUI(true);
        }else if(currentFragment instanceof CloudFragment){
            CloudFragment cloudFragment = (CloudFragment) currentFragment;
            cloudFragment.updateUI(true);
        }
    }
    public void performLogout(){
        boolean wasLoggedIn = loginInfo.getBoolean("loggedIn", false);
        SharedPreferences.Editor editor = loginInfo.edit();
        editor.putString("idToken", null);
        editor.putBoolean("loggedIn", false);
        editor.apply();
        signinButton.setText(R.string.sign_in);
        signinButton.setOnClickListener(v2 -> bottomNavigationView.setSelectedItemId(R.id.nav_user));

        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu navMenu = navigationView.getMenu();
        MenuItem navLoginItem = navMenu.findItem(R.id.nav_login);
        MenuItem navLogoutItem = navMenu.findItem(R.id.nav_logout);
        navLoginItem.setVisible(true);
        navLogoutItem.setVisible(false);
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("main_fragment");
        if(currentFragment instanceof UserFragment){
            UserFragment findFragment = (UserFragment) currentFragment;
            findFragment.updateLoginUI(false);
        }else if(currentFragment instanceof CloudFragment){
            CloudFragment cloudFragment = (CloudFragment) currentFragment;
            cloudFragment.updateUI(false);
        }
        if(wasLoggedIn){
            Toast toast = new Toast(this);
            toast.setText("登出成功");
            toast.show();
        }
    }
    public void goToSignIn(){
        bottomNavigationView.setSelectedItemId(R.id.nav_user);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, navDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }
    public void setCurrentFragment(Fragment fragment) { // Support function for setting fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, fragment, "main_fragment")
                .commit();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navDrawer.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
