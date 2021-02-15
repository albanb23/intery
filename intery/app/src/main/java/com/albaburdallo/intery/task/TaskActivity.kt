package com.albaburdallo.intery.task

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.albaburdallo.intery.HomeActivity
import com.albaburdallo.intery.LoginActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.habit.HabitActivity
import com.albaburdallo.intery.model.entities.Calendar
import com.albaburdallo.intery.model.entities.Task
import com.albaburdallo.intery.wallet.WalletActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_options.*
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.task_list.*
import java.util.*
import kotlin.collections.HashMap


class TaskActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    private lateinit var taskList: ListView
    private lateinit var createTaskButton: Button
    private lateinit var adapter: TaskAdapter
    private lateinit var tasks: MutableList<Task>
    private lateinit var taskid: String
    private var showAll = false
    private var calendarName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        this.getSupportActionBar()?.hide()

        //Guardado de datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        calendarName = prefs.getString("calendar", "").toString()
        if (calendarName != "") {
            showAll = true
            eyeClosedIcon.visibility = View.GONE
            eyeOpenIcon.visibility = View.GONE
            calendarIcon.visibility = View.GONE
            bookmarkIcon.visibility = View.GONE
            trashCalendarImageView.visibility = View.VISIBLE

            trashCalendarImageView.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(this.resources.getString(R.string.deleteCalendar))
                    .setCancelable(false)
                    .setPositiveButton(this.resources.getString(R.string.yes)) { dialog, id ->
                        //se borran todas las tareas del calendario
                        db.collection("tasks").get().addOnSuccessListener { documents ->
                            for(document in documents) {
                                val cal = document.get("calendar") as HashMap<*, *>
                                if (cal["name"] == calendarName) {
                                    db.collection("tasks").document(document.id).delete()
                                }
                            }
                        }
                        //se borra el calendario
                        db.collection("calendars").document(calendarName).delete()
                        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
                        prefs.putString("calendar", null)
                        prefs.apply()
                        showTask()
                    }
                    .setNegativeButton(this.resources.getString(R.string.no)) { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
        }
        showAll = prefs.getBoolean("showAll", false)

        setup()
    }

    private fun setup() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()

        taskList = findViewById(R.id.taskList)
        createTaskButton = findViewById(R.id.createTaskButton)
        tasks = arrayListOf()
        val authEmail = FirebaseAuth.getInstance().currentUser?.email;

        if (calendarName=="") {
            if (showAll) {
                eyeClosedIcon.visibility = View.GONE
                eyeOpenIcon.visibility = View.VISIBLE
                trashCalendarImageView.visibility = View.GONE
            } else {
                eyeClosedIcon.visibility = View.VISIBLE
                eyeOpenIcon.visibility = View.GONE
                trashCalendarImageView.visibility = View.GONE
            }
        }

        eyeClosedIcon.setOnClickListener {
            prefs.putBoolean("showAll", true)
            prefs.apply()
            restartView()
        }

        eyeOpenIcon.setOnClickListener {
            prefs.putBoolean("showAll", false)
            prefs.apply()
            restartView()
        }

        println(showAll || calendarName!="")
        var taskcoll: Query = if (showAll || calendarName!="") {
            db.collection("tasks").orderBy("done", Query.Direction.ASCENDING)
        } else {
            db.collection("tasks").whereEqualTo("done", false)
        }

        taskcoll.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = document.get("user") as HashMap<String, String>
                    if (user["email"] == authEmail) {
                        val name = document.get("name") as String
                        val startDate = document.get("startDate") as Timestamp
                        val endDate = document.get("endDate") as? Timestamp
                        val startTime = document.get("startTime") as? Timestamp
                        val endTime = document.get("endTime") as? Timestamp
                        val allday = document.get("allDay") as Boolean
                        val notifyme = document.get("notifyMe") as Boolean
                        val notes = document.get("notes") as String
                        val done = document.get("done") as Boolean
                        val id  = document.get("id") as Long
                        val calendar = document.get("calendar") as HashMap<String, String>
                        if (endDate != null && startTime != null && endTime != null) {
                            tasks.add(
                                Task(
                                    id.toInt(),
                                    name,
                                    startDate.toDate(),
                                    startTime.toDate(),
                                    endDate.toDate(),
                                    endTime.toDate(),
                                    allday,
                                    notifyme,
                                    notes,
                                    done,
                                    com.albaburdallo.intery.model.entities.Calendar(calendar["name"],calendar["description"],calendar["color"])
                                )
                            )
                        } else {
                            tasks.add(
                                Task(
                                    id.toInt(),
                                    name,
                                    startDate.toDate(),
                                    allday,
                                    notifyme,
                                    notes,
                                    done,
                                    com.albaburdallo.intery.model.entities.Calendar(calendar["name"],calendar["description"],calendar["color"])
                                )
                            )
                        }
                    }
                }

                adapter = TaskAdapter(this, tasks)
                taskList.adapter = adapter
                taskList.emptyView = findViewById(R.id.noCalendarsTextView)

                taskList.setOnItemClickListener { parent, view, position, id ->
                    val task = adapter.getItem(position)
                    showTaskForm(task, "edit")
                    adapter.notifyDataSetChanged()
                }

                val borrar = arrayListOf<Task>()
                for (t in tasks) {
                    if(calendarName!="" && calendarName!=t.calendar.name) {
                        borrar.add(t)
                    }
                }
                tasks.removeAll(borrar)
            }

        createTaskButton.setOnClickListener {
            var task: Task? = null
            showTaskForm(task, "create")
        }

        closeImageView.setOnClickListener {
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.putString("calendar", null)
            prefs.apply()
            showHome()
        }

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.tasks_item -> {
                    showTask()
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

        bookmarkIcon.setOnClickListener {
            showCalendar()
        }

    }

    private fun restartView() {
        val intent = Intent(this, TaskActivity::class.java)
        finish()
        startActivity(intent)
        //quitar animación
        overridePendingTransition(0, 0);
    }

    private fun showTaskForm(task: Task?, form: String) {
        val taskFormIntent = Intent(this, TaskFormActivity::class.java)
        if (task != null) {
            taskFormIntent.putExtra("taskid", task.id.toString())
        }
        taskFormIntent.putExtra("form", form)
        startActivity(taskFormIntent)
    }

    private fun showHome() {
        val homeIntent = Intent(this, HomeActivity::class.java)
        startActivity(homeIntent)
    }

    private fun showLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java).apply { }
        startActivity(loginIntent)
    }

    private fun showWallet() {
        val walletIntent = Intent(this, WalletActivity::class.java).apply { }
        startActivity(walletIntent)
    }

    private fun showTask() {
        val taskIntent = Intent(this, TaskActivity::class.java).apply { }
        startActivity(taskIntent)
    }

    private fun showHabit() {
        val habitIntent = Intent(this, HabitActivity::class.java).apply { }
        startActivity(habitIntent)
    }

    private fun showCalendar() {
        val calendarIntent = Intent(this, CalendarActivity::class.java).apply { }
        startActivity(calendarIntent)
    }

}