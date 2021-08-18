package com.example.gymbuddy.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.gymbuddy.view.NewSessionActivity;
import com.example.gymbuddy.R;
import com.example.gymbuddy.helpers.Helpers;
import com.example.gymbuddy.model.Exercise;

public class NotificationService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private boolean breakFinished = false;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private String timeTxt;
    private Exercise exercise;
    private PendingIntent pendingIntent;

    public class LocalBinder extends Binder {
        public NotificationService getServerInstance() {
            return NotificationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void cancelAll() {
        stopForeground(true);
        notificationManager.cancelAll();
    }

    public void updateTime(String time) {
        builder.setContentText(time);
        notificationManager.notify(Helpers.FOREGROUND_NOTIFICATION, builder.build());

    }

    @Override
    public void onCreate() {

        Intent intent = new Intent(this, NewSessionActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel;
        channel = new NotificationChannel(Helpers.CHANNEL_ID, getResources().getString(R.string.breakCountdown), importance);
        channel.enableLights(true);

        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        builder = new NotificationCompat.Builder(this, Helpers.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setShowWhen(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        super.onCreate();
    }

    public void displayBreakFinishedNotification() {
        breakFinished = true;
        builder = new NotificationCompat.Builder(this, Helpers.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setShowWhen(true)
                .setVibrate(new long[]{0, 50, 50, 50})
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(getString(R.string.breakFinished))
                .setContentText(getString(R.string.clickToOpenAndDoTheSet));
        notificationManager.notify(Helpers.FOREGROUND_NOTIFICATION, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "here", Toast.LENGTH_SHORT).show();
        if (breakFinished) {
            builder = new NotificationCompat.Builder(this, Helpers.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setShowWhen(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            breakFinished = false;
        }
        builder.setWhen(System.currentTimeMillis())
                .setContentText(timeTxt)
                .setContentTitle(getResources().getString(R.string.breakNotification) + " - " + exercise.getName())
                .setOnlyAlertOnce(true)
                .setSilent(true);
        startForeground(Helpers.FOREGROUND_NOTIFICATION, builder.build());

        return START_NOT_STICKY;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public void setTimeTxt(String timeTxt) {
        this.timeTxt = timeTxt;
    }

    public boolean isBreakEnded() {
        return breakFinished;
    }

    public void setBreakEnded(boolean breakEnded) {
        this.breakFinished = breakEnded;
    }

}
