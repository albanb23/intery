package com.albaburdallo.intery.task

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.entities.Calendar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_calendar.*
import kotlinx.android.synthetic.main.activity_task_form.*
import kotlinx.android.synthetic.main.activity_task_form.backImageView
import java.util.HashMap

class CalendarActivity : AppCompatActivity(), AddCalendarFragment.CalendarCallbackListener {
    private val db = FirebaseFirestore.getInstance()

    private lateinit var calendarList: ListView
    private lateinit var createCalendarButton: Button
    private lateinit var adapter: CalendarAdapter
    private lateinit var calendars: MutableList<Calendar>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        setup()
    }

    private fun setup() {

        calendarBackImage.setOnClickListener { showList() }

        calendarList = findViewById(R.id.calendarList)
        createCalendarButton = findViewById(R.id.createCalendarButton)
        calendars = arrayListOf()
        val authEmail = FirebaseAuth.getInstance().currentUser?.email;

        db.collection("calendars").get().addOnSuccessListener { documents ->
            for (document in documents){
                val user = document.get("user") as HashMap<String, String>
                if (user["email"] == authEmail) {
                    val title = document.get("name") as String
                    val color = document.get("color") as String
                    val description = document.get("description") as String
                    calendars.add(Calendar(title, description, color))
                }
            }

            adapter = CalendarAdapter(this, calendars)
            calendarList.adapter = adapter
            calendarList.emptyView = findViewById(R.id.noCalendarsTextView)
        }

        createCalendarButton.setOnClickListener {
            AddCalendarFragment.newInstance().show(supportFragmentManager, this.resources.getString(R.string.create_calendar))
        }

        calendarList.setOnItemClickListener { parent, view, position, id ->
            val calendar = adapter.getItem(position)
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            if (calendar != null) {
                prefs.putString("calendar", calendar.name)
                prefs.apply()
                showList()
            }
        }

    }

    override fun onCalendarAdded(calendar: Calendar) {
        calendars.add(calendar)
        db.collection("calendars").document(calendar.name).set(calendar as Calendar)
        adapter.notifyDataSetChanged()
        showList()
    }

    private fun showList() {
        val listIntent = Intent(this, TaskActivity::class.java)
        startActivity(listIntent)
    }
}