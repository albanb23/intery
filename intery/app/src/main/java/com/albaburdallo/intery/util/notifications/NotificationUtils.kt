package com.albaburdallo.intery.util.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.albaburdallo.intery.HomeActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.habit.HabitActivity
import com.albaburdallo.intery.task.TaskActivity

private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0
private val FLAGS = 0

fun NotificationManager.sendNotification(title: String, messageBody: String, applicationContext: Context) {

    val homeIntent = Intent(applicationContext, HomeActivity::class.java)
    val homePendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        homeIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val snoozeIntent = Intent(applicationContext, SnoozeReceiver::class.java)
    val snoozePendingIntent: PendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        REQUEST_CODE,
        snoozeIntent,
        FLAGS)

    val builder = NotificationCompat.Builder(applicationContext, "intery_channel")
        .setContentIntent(homePendingIntent)
        .setAutoCancel(true)
        .addAction(R.drawable.calendar, applicationContext.getString(R.string.snooze), snoozePendingIntent)
        //cambiar
        .setSmallIcon(R.drawable.ic_stat_tick)
        .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.ic_stat_tick))
        .setContentTitle(title)
        .setContentText(messageBody)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}