package com.albaburdallo.intery.task

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.entities.Calendar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_calendar.*
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.activity_task_form.*
import kotlinx.android.synthetic.main.activity_task_form.backImageView
import java.util.HashMap

class CalendarActivity : AppCompatActivity(), AddCalendarFragment.CalendarCallbackListener {
    private val db = FirebaseFirestore.getInstance()

    private lateinit var calendarList: RecyclerView
    private lateinit var createCalendarButton: Button
    private lateinit var adapter: CalendarAdapter
    private lateinit var calendars: MutableList<Calendar>
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
    }

    override fun onStart() {
        super.onStart()

        calendarBackImage.setOnClickListener { showTasks() }

        calendarList = findViewById(R.id.calendarList)
        createCalendarButton = findViewById(R.id.createCalendarButton)
        calendars = arrayListOf()

        db.collection("calendars").whereEqualTo("user.email", authEmail).addSnapshotListener(this) { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            calendars.clear()
            for (document in value!!) {
                    val id = document.get("id") as String
                    val title = document.get("name") as String
                    val color = document.get("color") as String
                    val description = document.get("description") as String
                    calendars.add(Calendar(id, title, description, color))
            }

            calendarList.layoutManager = LinearLayoutManager(this)
            adapter = CalendarAdapter(calendars)
            calendarList.adapter = adapter
            adapter.setOnItemClickListener(object: CalendarAdapter.ClickListener{
                override fun onItemClick(v: View, position: Int) {
                    val calendar = calendars[position]
                    val prefs =
                        getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
                    if (calendar != null) {
                        prefs.putString("calendar", calendar.id)
                        prefs.apply()
                        showTasks()
                    }
                }
            })

            if (calendars.isEmpty()) {
                noCalendarsTextView.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            } else {
                noCalendarsTextView.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }
        }

        createCalendarButton.setOnClickListener {
            AddCalendarFragment.newInstance()
                .show(supportFragmentManager, this.resources.getString(R.string.create_calendar))
        }

    }

    override fun onCalendarAdded(calendar: Calendar) {
        val query = db.collection("calendars")
        query.whereEqualTo("user.email", authEmail).get().addOnSuccessListener { value ->
            var exists = false
            for (document in value!!) {
                val calendarName = document.get("name") as String
                if (calendarName == calendar.name) {
                    exists = true
                    break
                }
            }
            if (!exists) {
                calendars.add(calendar)
                query.document(calendar.id).set(calendar as Calendar)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, R.string.exisitingCalendar, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showList() {
        val listIntent = Intent(this, CalendarActivity::class.java)
        startActivity(listIntent)
    }

    private fun showTasks() {
        val taskIntent = Intent(this, TaskActivity::class.java)
        startActivity(taskIntent)
    }
}