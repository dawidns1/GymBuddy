package com.example.gymbuddy.services;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
//        Log.d(TAG, "onNewToken: " + s);
//        Toast.makeText(this, "" + s, Toast.LENGTH_SHORT).show();
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        Toast.makeText(this, "message received", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent();
//        intent.setAction(ACTION_SEND_MESSAGE);
//        intent.putExtra(MESSAGE_SEND, remoteMessage);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
