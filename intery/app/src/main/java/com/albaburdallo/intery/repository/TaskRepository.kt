package com.albaburdallo.intery.repository

import com.albaburdallo.intery.model.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TaskRepository {
    private val db = FirebaseFirestore.getInstance()

    public fun getAllTasks() {
        var tasks: ArrayList<Task> = arrayListOf()
        val authEmail = FirebaseAuth.getInstance().currentUser?.email;
        db.collection("tasks").orderBy("created.time", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = document.get("user") as HashMap<String, String>
                    if (user["email"] == authEmail) {
                        val name = document.get("name") as String
                        val startDate = document.get("startDate") as Timestamp
                        val endDate = document.get("endDate") as Timestamp
                        val startTime = document.get("startTime") as Timestamp
                        val endTime = document.get("endTime") as Timestamp
                        val allday = document.get("allDay") as Boolean
                        val notifyme = document.get("notifyMe") as Boolean
                        val notes = document.get("notes") as String
                        val done = document.get("done") as Boolean
                        tasks.add(
                            Task(
                                name,
                                startDate.toDate(),
                                endDate.toDate(),
                                startTime.toDate(),
                                endTime.toDate(),
                                allday,
                                notifyme,
                                notes,
                                done
                            )
                        )
                    }
                }
            }
    }

}