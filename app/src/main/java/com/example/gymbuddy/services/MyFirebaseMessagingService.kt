package com.example.gymbuddy.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        Toast.makeText(this, "message received", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent();
//        intent.setAction(ACTION_SEND_MESSAGE);
//        intent.putExtra(MESSAGE_SEND, remoteMessage);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}