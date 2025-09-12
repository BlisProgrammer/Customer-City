package com.blis.customercity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseHandler {
    /**
     * Log button click to firebase analytics
     * @param context context of button parent
     * @param fragment fragment object of parent
     * @param button the button object
     */
    public static void logButtonClick(Context context, Fragment fragment, Button button){
        // Fire Firebase
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SOURCE_PLATFORM, "Customer City");
        bundle.putString("fragment_class", fragment.getClass().getName());
        bundle.putString("screen_class", fragment.getClass().getName());
        bundle.putString("button_text", button.getText().toString());
        mFirebaseAnalytics.logEvent("button_click", bundle);
    }
    /**
     * Log action button click to firebase analytics
     * @param context context of button parent
     * @param activity activity object of parent
     * @param button the button object
     */
    public static void logActionButtonClick(Context context, Activity activity, FloatingActionButton button){
        // Fire Firebase
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SOURCE_PLATFORM, "Customer City");
        bundle.putString("fragment_class", activity.getClass().getName());
        bundle.putString("screen_class", activity.getClass().getName());
        bundle.putString("button_text", button.getClass().getName());
        mFirebaseAnalytics.logEvent("button_click", bundle);
    }

    /**
     * Log button click events to firebase analytics
     * @param context context of button parent
     * @param activity Activity object of parent
     * @param button the button object
     */
    public static void logButtonClick(Context context, Activity activity, Button button){
        // Fire Firebase
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SOURCE_PLATFORM, "Customer City");
        bundle.putString("activity_class", activity.getClass().getName());
        bundle.putString("screen_class", activity.getClass().getName());
        bundle.putString("button_text", button.getText().toString());
        mFirebaseAnalytics.logEvent("button_click", bundle);
    }
    /**
     * Log item click events to firebase analytics
     * @param context context of item parent
     * @param fragment fragment of item parent
     * @param position position of item clicked
     * @param parent adapter view of item parent
     */
    public static void logItemClick(Context context, Fragment fragment, AdapterView<?> parent, View view, int position, long id){
        // Fire Firebase
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SOURCE_PLATFORM, "Customer City");
        bundle.putString("fragment_class", fragment.getClass().getName());
        bundle.putString("screen_class", fragment.getClass().getName());
        bundle.putString("item_text", (String) parent.getItemAtPosition(position));
        mFirebaseAnalytics.logEvent("item_click", bundle);
    }
}
