//package com.example.gymbuddy;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.drawable.ColorDrawable;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.TextView;
//
//import androidx.appcompat.app.ActionBar;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class Helpers {
//    Activity activity;
//    Context context;
//
//    public Helpers(Activity activity, Context context) {
//        this.activity = activity;
//        this.context=context;
//    }
//
//    public void setupActionBar(String text1, String text2) {
//        ActionBar actionBar = (AppCompatActivity)activity.getSupportActionBar();
//        actionBar.setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.orange_500)));
//        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setDisplayUseLogoEnabled(false);
//        actionBar.setDisplayHomeAsUpEnabled(false);
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(false);
//
//        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
//        View customActionBar = LayoutInflater.from(context).inflate(R.layout.action_bar, null);
//        actionBar.setCustomView(customActionBar, params);
//        TextView abText1 = activity.findViewById(R.id.abText1);
//        TextView abText2 = activity.findViewById(R.id.abText2);
//        abText1.setText(text1);
//        abText2.setText(text2);
//    }
//}
