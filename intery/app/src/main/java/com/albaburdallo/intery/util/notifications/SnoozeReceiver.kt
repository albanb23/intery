package com.albaburdallo.intery.util.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class SnoozeReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val notificationManager = context?.let { ContextCompat.getSystemService(it, NotificationManager::class.java) } as NotificationManager
        notificationManager.cancelAll()
    }
}