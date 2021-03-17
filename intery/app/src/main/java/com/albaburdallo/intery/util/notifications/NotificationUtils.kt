package com.albaburdallo.intery.util.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.albaburdallo.intery.R
import com.albaburdallo.intery.task.TaskActivity

private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0
private val FLAGS = 0

fun NotificationManager.sendNotification(title: String, messageBody: String, applicationContext: Context) {

    val taskIntent = Intent(applicationContext, TaskActivity::class.java)
    val taskPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        taskIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val snoozeIntent = Intent(applicationContext, SnoozeReceiver::class.java)
    val snoozePendingIntent: PendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        REQUEST_CODE,
        snoozeIntent,
        FLAGS)

    val builder = NotificationCompat.Builder(applicationContext, "intery_channel")

        .setContentIntent(taskPendingIntent)
        .setAutoCancel(true)
        .addAction(R.drawable.calendar, "Snooze", snoozePendingIntent)
        //cambiar
        .setSmallIcon(R.drawable.calendar)
        .setContentTitle(title)
        .setContentText(messageBody)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}