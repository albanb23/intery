package com.albaburdallo.intery.util.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.albaburdallo.intery.task.TaskActivity
import com.google.firebase.firestore.FirebaseFirestore


class DoneReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context?.let { ContextCompat.getSystemService(
            it,
            NotificationManager::class.java
        ) } as NotificationManager
        val db = FirebaseFirestore.getInstance()
        val taskId = intent?.extras?.getString("taskId")?:""
//        val taskId = taskName + "-" + FirebaseAuth.getInstance().currentUser?.email
//        println("======taskName=======" + taskName)
        println("======taskId=======" + taskId)
        db.collection("tasks").document(taskId).update("done", true)
        notificationManager.cancelAll()
    }
}