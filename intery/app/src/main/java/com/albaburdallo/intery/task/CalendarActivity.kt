package com.albaburdallo.intery.task

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.LoginActivity
import com.albaburdallo.intery.ProfileActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.habit.HabitActivity
import com.albaburdallo.intery.util.entities.Calendar
import com.albaburdallo.intery.wallet.WalletActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_calendar.*
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.activity_task_form.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.options.*

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

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.tasks_item -> {
                    showTasks()
                    true
                }
                R.id.wallet_item -> {
                    showWallet()
                    true
                }
                R.id.habits_item -> {
                    showHabit()
                    true
                }
                R.id.settings_item -> {
                    showHabit()
                    true
                }
                else -> {
                    false
                }
            }
        }

        logOutButton.setOnClickListener{
            //Borrado de datos
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            //onBackPressed() //para volver a la pantalla anterior
            showLogin()
        }

        db.collection("users").document(authEmail!!).get().addOnSuccessListener {
            var photo = it.get("photo") as String
            if (photo == "") {
                photo = ""
            } else {
                Picasso.get().load(photo).transform(CropCircleTransformation()).into(profilePicImage)
            }
        }

        val header = nav_view.getHeaderView(0)
        val profilePicImage = header.findViewById<ImageView>(R.id.profilePicImage)
        profilePicImage.setOnClickListener {
            if (authEmail != null) {
                showProfile(authEmail)
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

    private fun showTasks() {
        val taskIntent = Intent(this, TaskActivity::class.java)
        startActivity(taskIntent)
    }

    private fun showProfile(email: String) {
        val profileIntent = Intent(this, ProfileActivity::class.java)
        if (email != null) {
            profileIntent.putExtra("userEmail", email)
        }
        startActivity(profileIntent)
    }

    private fun showLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java).apply { }
        startActivity(loginIntent)
    }

    private fun showWallet() {
        val walletIntent = Intent(this, WalletActivity::class.java).apply { }
        startActivity(walletIntent)
    }

    private fun showHabit() {
        val habitIntent = Intent(this, HabitActivity::class.java).apply { }
        startActivity(habitIntent)
    }
}