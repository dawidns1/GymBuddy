package com.example.gymbuddy.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.gymbuddy.R
import com.example.gymbuddy.helpers.Helpers
import com.example.gymbuddy.helpers.Helpers.toPrettyString
import com.example.gymbuddy.model.Exercise
import com.example.gymbuddy.view.NewSessionActivity

class NotificationService : Service() {
    private val mBinder: IBinder = LocalBinder()
    var isBreakEnded = false
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder
    private var timeTxt: String? = null
    var exercise: Exercise? = null
    private var pendingIntent: PendingIntent? = null
    private var currentSet: Int = 0

    inner class LocalBinder : Binder() {
        val serverInstance: NotificationService
            get() = this@NotificationService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        cancelAll()
        return super.onUnbind(intent)
    }

    fun setCurrentSet(set: Int) {
        currentSet = set
    }

    fun cancelAll() {
        stopForeground(true)
        notificationManager.cancelAll()
    }

    fun updateTime(time: String?) {
        builder.setContentTitle("${resources.getString(R.string.breakNotification)} - $time")
        notificationManager.notify(Helpers.FOREGROUND_NOTIFICATION, builder.build())
    }

    override fun onCreate() {
        val intent = Intent(this, NewSessionActivity::class.java)
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(Helpers.CHANNEL_ID, resources.getString(R.string.breakCountdown), importance).apply {
            enableLights(true)
        }
        notificationManager = getSystemService(NotificationManager::class.java).apply {
            createNotificationChannel(channel)
        }
        builder = NotificationCompat.Builder(this, Helpers.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, R.mipmap.ic_launcher))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setShowWhen(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        super.onCreate()
    }

    fun displayBreakFinishedNotification() {
        isBreakEnded = true
        builder = NotificationCompat.Builder(this, Helpers.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, R.mipmap.ic_launcher))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setShowWhen(true)
            .setVibrate(longArrayOf(0, 50, 50, 50))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentTitle(getString(R.string.breakFinished))
            .setContentText("${exercise!!.name}${lastSessionTextBuilder()}")
        notificationManager.notify(Helpers.FOREGROUND_NOTIFICATION, builder.build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (isBreakEnded) {
            builder = NotificationCompat.Builder(this, Helpers.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setShowWhen(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            isBreakEnded = false
        }
        builder.setWhen(System.currentTimeMillis())
            .setContentTitle("${resources.getString(R.string.breakNotification)} - $timeTxt")
            .setContentText("${exercise!!.name}${lastSessionTextBuilder()}")
            .setOnlyAlertOnce(true)
            .setSilent(true)
        startForeground(Helpers.FOREGROUND_NOTIFICATION, builder.build())
        return START_NOT_STICKY
    }

    private fun lastSessionTextBuilder(): String {
        return if (exercise!!.sessions.size > 1) {
            " - ${resources.getString(R.string.lastSession)} ${exercise!!.sessions[1].load[currentSet].toPrettyString()}x" +
                    "${exercise!!.sessions[1].reps[currentSet]}|" +
                    (exercise!!.sessions[1].rir?.let{
                        if (it[currentSet] == 6) "-" else it[currentSet].toString()
                    } ?: "-")
        } else {
            ""
        }
    }

    fun setTimeTxt(timeTxt: String?) {
        this.timeTxt = timeTxt
    }
}