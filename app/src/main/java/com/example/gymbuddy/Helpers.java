package com.example.gymbuddy;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class Helpers {

    public static void setupActionBar(String text1, String text2, ActionBar actionBar, Activity activity) {
        actionBar.setBackgroundDrawable(new ColorDrawable(activity.getApplicationContext().getResources().getColor(R.color.orange_500)));
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
        int colorIntB = ContextCompat.getColor(context,R.color.orange_500_alpha);
        ColorStateList cslB = ColorStateList.valueOf(colorIntB);
        efab.setBackgroundTintList(cslB);
    }

    public static void disableEFABClickable(ExtendedFloatingActionButton efab, Context context) {
        efab.setClickable(false);
        efab.setTextColor(ContextCompat.getColor(context,R.color.grey_500));
        int colorInt = ContextCompat.getColor(context,R.color.grey_500);
        ColorStateList csl = ColorStateList.valueOf(colorInt);
        efab.setStrokeColor(csl);
        int colorIntB = ContextCompat.getColor(context,R.color.grey_700_alpha);
        ColorStateList cslB = ColorStateList.valueOf(colorIntB);
        efab.setBackgroundTintList(cslB);
    }
}
