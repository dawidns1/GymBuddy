package com.example.gymbuddy.helpers;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.example.gymbuddy.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class Helpers {

    public static final String CHANNEL_ID = "id";
    public static final String CHANNEL2_ID = "id2";
    public final static String NEW_WORKOUT_KEY = "new workout key";
    public static final String EXERCISES_KEY = "exercises";
    public static final String WORKOUTS_KEY = "edit workout key";
    public static final String POSITION_KEY = "position";
    public static final String WORKOUT_KEY = "workout";
    public final static String NEW_EXERCISE_KEY = "new exercise key";
    public static final String EDITED_EXERCISE_KEY="edited exercise key";
    public final static String NEW_SESSION_KEY = "new session key";
    public static final String RESUMED_KEY = "resumed";
    public static final String CHART_KEY = "chart";
    public static final String ACTION_SEND_MESSAGE = "action send message";
    public static final String MESSAGE_SEND = "message send";
    public static final int FOREGROUND_NOTIFICATION=0;
    public static final String AD_ID_MAIN_NATIVE = "ca-app-pub-3836143618707347/3998950331";
    public static final String AD_ID_NEW_SESSION_NATIVE = "ca-app-pub-3836143618707347/8449401049";
    public static final String AD_ID_EXERCISES_NATIVE = "ca-app-pub-3836143618707347/8500946906";
    public static final String AD_ID_CHART_NATIVE = "ca-app-pub-3836143618707347/9293980365";
    public static final String AD_ID_SCHEDULE_NATIVE = "ca-app-pub-3836143618707347/3192779903";
    public static final String AD_ID_SESSIONS_NATIVE = "ca-app-pub-3836143618707347/6713222519";
    public static final String WORKOUT="workout";
    public static final String CURRENT_SESSION="current session";
    public static final String CURRENT_SET="current set";
    public static final String BACKED="backed";
    public static final int CREATE_FILE = 51;
    public static final int PICK_FILE = 52;
    public static final int OPEN_DIRECTORY = 53;
    public static final int FILE_SHARING = 54;
    public static AdRequest savedAdRequest;
    public static final String MODE_SAVE_TO_FILE="saveToFile";
    public static final String MODE_SAVE_TO_CLOUD="saveToCloud";
    public static final String MODE_SHARE="share";
    public static final String MODE_ADD="add";
    public static final String MODE_DELETE="delete";
    public static final String WORKOUT_ID_KEY="id";
    public static final String WORKOUT_NAME_KEY="name";
    public static final String WORKOUT_EXERCISE_NUMBER_KEY="exerciseNumber";
    public static final String WORKOUT_TYPE_KEY="type";
    public static final String WORKOUT_MUSCLE_GROUP_KEY="muscleGroup";
    public static final String WORKOUT_MUSCLE_GROUP_SECONDARY_KEY="muscleGroupSecondary";
    public static final String WORKOUT_TIMESTAMP_KEY="timestamp";




    public static boolean workoutTypeComparator(String type1, String type2){
        if((type1.equals("Push") || type1.equals("Pull") || type1.equals("Legs")) &&
                (type2.equals("Push") || type2.equals("Pull") || type2.equals("Legs"))){
            return true;
        } else if((type1.equals("Upper") || type1.equals("Lower")) &&
                (type2.equals("Upper") || type2.equals("Lower"))){
            return true;
        } else return type1.equals(type2);
    }

    public static String workoutTypeHeaderGenerator(String type, Context context){
        if((type.equals("Push") || type.equals("Pull") || type.equals("Legs"))){
            return "Push/Pull/Legs";
        } else if((type.equals("Upper") || type.equals("Lower"))){
            return "Upper/Lower";
        } else if(type.equals("---")){
            return context.getResources().getString(R.string.other);
        } else return type;
    }

    public static void showRatingUserInterface(Activity activity) {
        long lastAppRating = Utils.getInstance(activity.getApplicationContext()).getLastAppRating();
        int days = 0;
        if (lastAppRating != 0) {
            days = millisToDays(System.currentTimeMillis() - lastAppRating);
        }
//        Toast.makeText(activity, ""+days, Toast.LENGTH_SHORT).show();
        if (days > 14) {
            Utils.getInstance(activity.getApplicationContext()).setLastAppRating(System.currentTimeMillis());
            ReviewManager manager = ReviewManagerFactory.create(activity);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                try {
                    if (task.isSuccessful()) {
                        ReviewInfo reviewInfo = task.getResult();
                        Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                        flow.addOnCompleteListener(task2 -> {
                        });
                    }
                } catch (Exception ex) {

                }
            });
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showKeyboard(Context context){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    public static ImageView setupActionBar(String text1, String text2, ActionBar actionBar, Activity activity) {
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(activity.getApplicationContext(), R.color.grey_900)));
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);

            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
            View customActionBar = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.action_bar, null);
            actionBar.setCustomView(customActionBar, params);
            ImageView imgSuperset=activity.findViewById(R.id.imgSupersetBar);
            TextView abText1 = activity.findViewById(R.id.abText1);
            abText1.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.orange_500));
            TextView abText2 = activity.findViewById(R.id.abText2);
            abText2.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.orange_500));
            abText1.setText(text1);
            abText2.setText(text2);
            return imgSuperset;
        }
        return null;
    }

    public static void shake(View v) {
        ObjectAnimator
                .ofFloat(v, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
                .setDuration(200)
                .start();
    }

    public static void shakeVertically(View v, int duration) {
        ObjectAnimator
                .ofFloat(v, "translationY", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
                .setDuration(duration)
                .start();
    }

    public static void enableEFABClickable(ExtendedFloatingActionButton efab, Context context) {
        efab.setClickable(true);
        efab.setTextColor(ContextCompat.getColor(context, R.color.white));
        int colorInt = ContextCompat.getColor(context, R.color.orange_500);
        ColorStateList csl = ColorStateList.valueOf(colorInt);
        efab.setStrokeColor(csl);
        int colorIntB = ContextCompat.getColor(context, R.color.grey_900);
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
        if (Math.round(1.0 * d) == (long) d)
            return String.valueOf((int) d);
        else
            return String.format("%.1f", d);
    }

    public static float stringDateToMillis(String date) {
        String[] values = date.split("-");
        int year = Integer.parseInt(values[0]);
        int month = Integer.parseInt(values[1]) - 1;
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

    public static AdLoader handleNativeAds(TemplateView template, Activity activity, String adUnitId, AdLoader adLoader) {
//        adUnitId="ca-app-pub-3940256099942544/2247696110";
        if (adLoader == null) {
            adLoader = new AdLoader.Builder(activity.getApplicationContext(), adUnitId)
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            template.setVisibility(GONE);
                            super.onAdFailedToLoad(loadAdError);
                        }

                        @Override
                        public void onAdLoaded() {
                            template.getMockLayout().setVisibility(GONE);
                            template.getRealLayout().setVisibility(VISIBLE);
                            super.onAdLoaded();
                        }
                    })
                    .forNativeAd(nativeAd -> {
                        NativeTemplateStyle styles = new
                                NativeTemplateStyle.Builder().withMainBackgroundColor(new ColorDrawable(ContextCompat.getColor(activity.getApplicationContext(), R.color.grey_900))).build();

                        template.setStyles(styles);
                        template.setNativeAd(nativeAd);

                        if (activity.isDestroyed()) {
                            nativeAd.destroy();
                        }
                    })
                    .build();
        }


        long lastAdShown = Utils.getInstance(activity.getApplicationContext()).getLastAdShown();
        long currentTime = System.currentTimeMillis();
        if (lastAdShown == 0 || (currentTime - lastAdShown) / 1000 > 60 || savedAdRequest==null) {
            savedAdRequest = new AdRequest.Builder().build();
            Utils.getInstance(activity.getApplicationContext()).setLastAdShown(currentTime);
//            Toast.makeText(activity, "new Ad", Toast.LENGTH_SHORT).show();
        }
        adLoader.loadAd(savedAdRequest);

        return adLoader;
    }

    public static void handleAds(FrameLayout adContainer, Activity activity, String adUnitId) {
        AdView ad = new AdView(activity.getApplicationContext());
        ad.setAdUnitId(adUnitId);
//        ad.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        adContainer.addView(ad);
        loadBanner(ad, activity);
        adContainer.getLayoutParams().height = ad.getAdSize().getHeightInPixels(activity.getApplicationContext());
        ad.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                adContainer.setVisibility(View.GONE);
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdLoaded() {
                adContainer.setVisibility(View.VISIBLE);
                super.onAdLoaded();
            }
        });
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

    private static int millisToDays(long millis) {
        return (int) (millis / 1000 / 60 / 60 / 24);
    }

    public static class DecimalDigitsInputFilter implements InputFilter {

        Pattern mPattern;

        public DecimalDigitsInputFilter() {
            mPattern = Pattern.compile("[0-9]*+((\\.[0-9]?)?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher matcher = mPattern.matcher(dest);
            if (!matcher.matches())
                return "";
            return null;
        }
    }
}
