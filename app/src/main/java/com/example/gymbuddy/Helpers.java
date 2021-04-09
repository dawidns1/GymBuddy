package com.example.gymbuddy;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class Helpers {

    public static final String CHANNEL_ID = "id";
    public final static String NEW_WORKOUT_KEY = "new workout key";
    public static final String EXERCISES_KEY = "exercises";
    public static final String WORKOUTS_KEY = "edit workout key";
    public static final String POSITION_KEY = "position";
    public static final String WORKOUT_KEY = "workout";
    public final static String NEW_EXERCISE_KEY = "new exercise key";
    public final static String NEW_SESSION_KEY = "new session key";
    public static final String RESUMED_KEY = "resumed";
    public static final String CHART_KEY = "chart";

    public static void setupActionBar(String text1, String text2, ActionBar actionBar, Activity activity) {
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(activity.getApplicationContext(),R.color.orange_500)));
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);

            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
            View customActionBar = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.action_bar, null);
            actionBar.setCustomView(customActionBar, params);
            TextView abText1 = activity.findViewById(R.id.abText1);
            TextView abText2 = activity.findViewById(R.id.abText2);
            abText1.setText(text1);
            abText2.setText(text2);
        }
    }

    public static void shake(View v) {
        ObjectAnimator
                .ofFloat(v, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
                .setDuration(200)
                .start();
    }

    public static void enableEFABClickable(ExtendedFloatingActionButton efab, Context context) {
        efab.setClickable(true);
        efab.setTextColor(ContextCompat.getColor(context, R.color.white));
        int colorInt = ContextCompat.getColor(context, R.color.orange_500);
        ColorStateList csl = ColorStateList.valueOf(colorInt);
        efab.setStrokeColor(csl);
        int colorIntB = ContextCompat.getColor(context, R.color.orange_500_alpha);
        ColorStateList cslB = ColorStateList.valueOf(colorIntB);
        efab.setBackgroundTintList(cslB);
    }

    public static void disableEFABClickable(ExtendedFloatingActionButton efab, Context context) {
        efab.setClickable(false);
        efab.setTextColor(ContextCompat.getColor(context, R.color.grey_500));
        int colorInt = ContextCompat.getColor(context, R.color.grey_500);
        ColorStateList csl = ColorStateList.valueOf(colorInt);
        efab.setStrokeColor(csl);
        int colorIntB = ContextCompat.getColor(context, R.color.grey_700_alpha);
        ColorStateList cslB = ColorStateList.valueOf(colorIntB);
        efab.setBackgroundTintList(cslB);
    }

    public static String stringFormat(double d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.format("%s", d);
    }


    public static String stringFormatPercentage(double d) {
        if (Math.round(1.0*d) == (long) d)
            return String.valueOf((int)d);
        else
            return String.format("%.1f", d);
    }

    public static float stringDateToMillis(String date) {
        String[] values = date.split("-");
        int year = Integer.parseInt(values[0]);
        int month = Integer.parseInt(values[1])-1;
        int day = Integer.parseInt(values[2]);
        Calendar time = Calendar.getInstance();
        time.set(year, month, day);
        return time.getTimeInMillis();
    }

    public static float arrayToTotal(int[] reps, float[] load) {
        float total = 0;
        for (int i = 0; i < reps.length; i++) {
            total += reps[i] * load[i];
        }
        return total;
    }

    public static void handleAds(FrameLayout adContainer, Activity activity){
        AdView ad = new AdView(activity.getApplicationContext());
        ad.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        adContainer.addView(ad);
        loadBanner(ad, activity);
    }

    private static void loadBanner(AdView ad, Activity activity) {
        AdRequest adRequest = new AdRequest.Builder().build();

        AdSize adSize = getAdSize(activity);
        ad.setAdSize(adSize);

        ad.loadAd(adRequest);
    }

    private static AdSize getAdSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity.getApplicationContext(), adWidth);
    }
}
