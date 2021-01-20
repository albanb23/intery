package com.albaburdallo.intery.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import com.albaburdallo.intery.HomeActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_task.*
import java.util.*
import kotlin.collections.ArrayList

class TaskActivity : AppCompatActivity(){
    private val db = FirebaseFirestore.getInstance()

    private lateinit var taskList: ListView
    private lateinit var createTaskButton: Button
    private lateinit var adapter: TaskAdapter
    private lateinit var tasks: ArrayList<Task>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        this.getSupportActionBar()?.hide()

        setup()

    }

    private fun setup() {
        taskList = findViewById(R.id.taskList)
        createTaskButton = findViewById(R.id.createTaskButton)
        tasks = arrayListOf()
        db.collection("tasks").orderBy("created.time", Query.Direction.DESCENDING).get().addOnSuccessListener { documents ->
            for (document in documents) {
                val name = document.get("name") as String
                val startDate = document.get("startDate") as Timestamp
                val endDate = document.get("endDate") as Timestamp
                val startTime = document.get("startTime") as Timestamp
                val endTime = document.get("endTime") as Timestamp
                val allday = document.get("allDay") as Boolean
                val notifyme = document.get("notifyMe") as Boolean
                val notes = document.get("notes") as String
                tasks.add(Task(name, startDate.toDate(), endDate.toDate(), startTime.toDate(), endTime.toDate(), allday, notifyme, notes))
            }

            adapter = TaskAdapter(this, tasks)
            taskList.adapter = adapter
            taskList.emptyView = findViewById(R.id.noTasksTextView)
        }

        taskList.onItemClickListener = AdapterView.OnItemClickListener { adapterView: AdapterView<*>, view1: View, position: Int, l: Long ->
            fun onItemClick(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {
                var task: Task = tasks[position]
                task.isDone = !task.isDone
                adapter.notifyDataSetChanged()
            }
        }

        createTaskButton.setOnClickListener {
            showTaskForm()
        }

        closeImageView.setOnClickListener {
            showHome()
        }

    }

    private fun showTaskForm() {
        val taskFormIntent = Intent(this, TaskFormActivity::class.java)
        startActivity(taskFormIntent)
    }

    private fun showHome() {
        val homeIntent = Intent(this, HomeActivity::class.java)
        startActivity(homeIntent)
    }

}