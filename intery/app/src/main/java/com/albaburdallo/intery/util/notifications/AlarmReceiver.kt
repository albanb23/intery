package com.albaburdallo.intery.util.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat


class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val notificationManager = context?.let { ContextCompat.getSystemService(
            it,
            NotificationManager::class.java
        ) } as NotificationManager

        val title = intent?.extras?.getString("title")
        val messageBody = intent?.extras?.getString("messageBody")
        notificationManager.sendNotification(title?:"", messageBody?:"", context)
    }
}