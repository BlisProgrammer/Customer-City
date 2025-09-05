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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Main extends AppCompatActivity {
    private DrawerLayout navDrawer;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private Button signinButton;
    private SharedPreferences loginInfo;
    private int clickedNavigationItemID;
    MenuItem navLoginItem, navLogoutItem;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

//        // for debugging purposes
//        FileHandler.removeFile(this, "addedRecords");
//        SharedPreferences preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.clear();
//        editor.apply();

        // Setup navigation menu
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu navMenu = navigationView.getMenu();
        navLoginItem = navMenu.findItem(R.id.nav_login);
        navLogoutItem = navMenu.findItem(R.id.nav_logout);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            clickedNavigationItemID = menuItem.getItemId();
            navDrawer.closeDrawer(GravityCompat.START);
            return true;
        });

        // Set up initial sign in UI
        signinButton = findViewById(R.id.account_button);
        loginInfo = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean loggedIn = loginInfo.getBoolean("loggedIn", false);
        String idToken = loginInfo.getString("idToken", null);
        String email = loginInfo.getString("email", null);
        if(loggedIn && idToken != null) {
            performLogin(idToken, email);
        }else{
            performLogout();
        }

        // Set up bottom navigation view and fragments
        bottomNavigationView = findViewById(R.id.navigation_view);
        Fragment findFragment = new FindFragment();
        Fragment userFragment = new UserFragment();
        Fragment cloudFragment = new CloudFragment();
        Fragment aboutFragment = new AboutFragment();
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if(id == R.id.nav_home){
                setCurrentFragment(aboutFragment);
                return true;
            }
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
            if(id == R.id.nav_empty){
                AddFragment addFragment = new AddFragment();
                setCurrentFragment(addFragment);
                return true;
            }
            return false;
        });

        // Set initial fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("main_fragment");
        if(currentFragment != null) {
            setCurrentFragment(currentFragment);
        }else{
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        // Fire Firebase: Fire app open signal
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Customer City");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Android App");
        bundle.putString(FirebaseAnalytics.Param.CONTENT, "Customer City opened");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);

        // navigation drawer
        navDrawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, navDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
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
                if(id == R.id.nav_privacy){
                    Intent intent = new Intent(Main.this, PrivacyActivity.class);
                    startActivity(intent);
                }
                if(id == R.id.nav_service){
                    Intent intent = new Intent(Main.this, ServiceActivity.class);
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

        // Floating action button
        FloatingActionButton addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            bottomNavigationView.setSelectedItemId(R.id.nav_empty);
            FirebaseHandler.logActionButtonClick(this, this, addButton);
        });
    }

    /**
     * Store login info to shared preference, and update login ui of activity and main_fragment. <br>
     * <b>Requires {@code signinButton} and {@code navLoginItem} to be initiated. </b>
     * @param idToken The {@code idToken} generated when login through
     * @param emailInputString Email of the account
     * @see com.blis.customercity.data.DataAPI#getToken(String, String) DataAPI#getToken(email, password)
     */
    public void performLogin(String idToken, String emailInputString){
        // Update shared preference
        SharedPreferences.Editor editor = loginInfo.edit();
        editor.putString("idToken", idToken);
        editor.putBoolean("loggedIn", true);
        editor.putString("email", emailInputString);
        editor.apply();

        // Update text on sign in button
        signinButton.setText(R.string.sign_out);
        signinButton.setOnClickListener(v2 -> {
            FirebaseHandler.logButtonClick(this, this, signinButton);
            performLogout();
        });

        // Update menu options on drawer menu
        navLogoutItem.setVisible(true);
        navLoginItem.setVisible(false);

        // Update UI of current fragment
        updateCurrentFragmentLoginUI(true);
    }
    public void performLogout(){
        // Update shared preference
        boolean wasLoggedIn = loginInfo.getBoolean("loggedIn", false);
        SharedPreferences.Editor editor = loginInfo.edit();
        editor.putString("idToken", null);
        editor.putBoolean("loggedIn", false);
        editor.apply();

        // Update text on sign in button
        signinButton.setText(R.string.sign_in);
        signinButton.setOnClickListener(v2 -> {
            FirebaseHandler.logButtonClick(this, this, signinButton);
            bottomNavigationView.setSelectedItemId(R.id.nav_user);
        });

        // Update menu options on drawer menu
        navLoginItem.setVisible(true);
        navLogoutItem.setVisible(false);

        // Update UI of current fragment
        updateCurrentFragmentLoginUI(false);

        // Show logged out hint
        if(wasLoggedIn){
            Toast.makeText(this, "登出成功", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Search for "main_fragment" and perform updateLoginUI method for different fragments.
     * @param loggedIn The result of this update. (true = changes into logged in mode)
     */
    private void updateCurrentFragmentLoginUI(boolean loggedIn){
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("main_fragment");
        if(currentFragment instanceof UserFragment){
            UserFragment findFragment = (UserFragment) currentFragment;
            findFragment.updateLoginUI(loggedIn);
        }else if(currentFragment instanceof CloudFragment){
            CloudFragment cloudFragment = (CloudFragment) currentFragment;
            cloudFragment.updateUI(loggedIn);
        }else if(currentFragment instanceof RecordFragment){
            RecordFragment recordFragment = (RecordFragment) currentFragment;
            recordFragment.updateUI(loggedIn);
        }
    }

    /**
     * Perform click on <b>User</b> button at bottom navigation view.
     * @see #goToCloud()
     * @see #goToSearch()
     */
    public void goToSignIn(){
        bottomNavigationView.setSelectedItemId(R.id.nav_user);
    }

    /**
     * Perform click on <b>Search</b> button at bottom navigation view.
     * @see #goToCloud()
     * @see #goToSignIn()
     */
    public void goToSearch(){
        bottomNavigationView.setSelectedItemId(R.id.nav_search);
    }

    /**
     * Perform click on <b>Saved</b> button at bottom navigation view.
     * @see #goToSignIn()
     * @see #goToSearch()
     */
    public void goToCloud(){
        bottomNavigationView.setSelectedItemId(R.id.nav_save);
    }


    /**
     * Replaces current "main_fragment" into another fragment. <br>
     * Use {@link #goToSignIn()}, {@link #goToCloud()}, {@link #goToSearch()} to update bottom navigation view selected item as well.
     * @param fragment Fragment to change into.
     * @see #goToSearch()
     * @see #goToCloud()
     * @see #goToSearch()
     */
    public void setCurrentFragment(Fragment fragment) { // Support function for setting fragment
        // Fire Firebase
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, fragment.getClass().getName());
        bundle.putString(FirebaseAnalytics.Param.SOURCE_PLATFORM, "Customer City");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, fragment, "main_fragment")
                .addToBackStack(null)
                .commit();
    }
}
