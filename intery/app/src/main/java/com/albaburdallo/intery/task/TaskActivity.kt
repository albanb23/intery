package com.albaburdallo.intery.task

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.albaburdallo.intery.HomeActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.entities.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.task_list.*
import java.util.*


class TaskActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    private lateinit var taskList: ListView
    private lateinit var createTaskButton: Button
    private lateinit var adapter: TaskAdapter
    private lateinit var tasks: MutableList<Task>
    private lateinit var taskid: String
    private var showAll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        this.getSupportActionBar()?.hide()

        //Guardado de datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        showAll = prefs.getBoolean("showAll", false)
        setup()

    }

    private fun setup() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()

        taskList = findViewById(R.id.taskList)
        createTaskButton = findViewById(R.id.createTaskButton)
        tasks = arrayListOf()
        val authEmail = FirebaseAuth.getInstance().currentUser?.email;

        if (showAll) {
            eyeClosedIcon.visibility = View.GONE
            eyeOpenIcon.visibility = View.VISIBLE
        } else {
            eyeClosedIcon.visibility = View.VISIBLE
            eyeOpenIcon.visibility = View.GONE
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

        var taskcoll: Query = if (showAll) {
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
                                    done
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
                                    done
                                )
                            )
                        }
                    }
                }

                adapter = TaskAdapter(this, tasks)
                taskList.adapter = adapter
                taskList.emptyView = findViewById(R.id.noTasksTextView)

                taskList.setOnItemClickListener { parent, view, position, id ->
                    val task = adapter.getItem(position)
                    showTaskForm(task, "edit")
                    adapter.notifyDataSetChanged()
                }

            }

        createTaskButton.setOnClickListener {
            var task: Task? = null
            showTaskForm(task, "create")
        }

        closeImageView.setOnClickListener {
            showHome()
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

}