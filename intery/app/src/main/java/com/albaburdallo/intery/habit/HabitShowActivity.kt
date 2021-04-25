package com.albaburdallo.intery.habit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.albaburdallo.intery.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_habit_show.*
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit

class HabitShowActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email
    private lateinit var habitId: String
    private val today = Calendar.getInstance().time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_show)
    }

    override fun onStart() {
        super.onStart()
        habitId = intent.extras?.getString("habitId") ?: ""

        habitShowBackImageView.setOnClickListener { showHabit() }
        habitEditImageView.setOnClickListener { showHabitForm(habitId, "edit") }

//        db.clearPersistence()
        db.collection("habits").document(habitId).get().addOnSuccessListener {
            val end = (it.get("endDate") as Timestamp).toDate() //100%
            val start = (it.get("startDate") as Timestamp).toDate() //0%
            val updated = (it.get("updated") as Timestamp).toDate()
            val habitProgress = it.get("progress") as Long

            goalProgressBar.progress = habitProgress.toInt()
            habitTitleText.text = it.get("name") as String

            if (today.after(end) || formatDate(updated)==formatDate(today)) {
                completeHabitButton.visibility = View.INVISIBLE
            } else {
                completeHabitButton.visibility = View.VISIBLE
            }

            completeHabitButton.setOnClickListener {
                val diff = end.time - today.time

                val progress =((diff / (1000*60*60*24)) % 7).toInt() //dias
                completeHabitButton.visibility = View.INVISIBLE

                val diffmax = end.time - start.time
                val max = ((diffmax / (1000*60*60*24)) % 7)+1 //dias
                goalProgressBar.max = 100
                println("max=========" + max)
                println("progress=========" + progress)
                val handler = Handler()
                Thread {
                    handler.post(Runnable {
                        goalProgressBar.progress = ((100 - (progress*100)/max).toInt())
                    })
                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }.start()

                db.collection("habits").document(habitId).update("progress", (100 - (progress*100)/max))
                db.collection("habits").document(habitId).update("updated", today)
            }
        }
    }

    private fun showHabit() {
        val habitIntent = Intent(this, HabitActivity::class.java).apply { }
        startActivity(habitIntent)
    }

    private fun showHabitForm(habit: String?, form: String) {
        val habitFormIntent = Intent(this, HabitFormActivity::class.java)
        if (habit != null) {
            habitFormIntent.putExtra("habitId", habit)
        }
        habitFormIntent.putExtra("form", form)
        startActivity(habitFormIntent)
    }

    private fun formatDate(date: Date): String {
        val pattern = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(date)
    }
}