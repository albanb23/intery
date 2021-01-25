package com.albaburdallo.intery.task

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import com.albaburdallo.intery.HomeActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_task.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TaskActivity : AppCompatActivity() {
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
        val authEmail = FirebaseAuth.getInstance().currentUser?.email;
        db.collection("tasks").orderBy("created.time", Query.Direction.DESCENDING).get()
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
                        if (endDate != null && startTime != null && endTime != null) {
                            tasks.add(
                                Task(
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
                            tasks.add(Task(name, startDate.toDate(), allday, notifyme, notes, done))
                        }
                    }
                }

                adapter = TaskAdapter(this, tasks)
                taskList.adapter = adapter
                taskList.emptyView = findViewById(R.id.noTasksTextView)

//                taskList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
//                    val task = adapter.getItem(position)
//                    showTaskForm()
//                    adapter.notifyDataSetChanged()
//                }

                taskList.setOnItemClickListener { parent, view, position, id ->
                    val form = "edit"
                    val task = adapter.getItem(position)
                    showTaskForm(task, form)
                }
            }

        createTaskButton.setOnClickListener {
            val form = "create"
            var task = Task("",Calendar.getInstance().time, false, false, "", false)
            showTaskForm(task, form)
        }

        closeImageView.setOnClickListener {
            showHome()
        }

    }

    private fun showTaskForm(task: Task?, form: String) {
        val taskFormIntent = Intent(this, TaskFormActivity::class.java)
        if (task != null) {
            taskFormIntent.putExtra("taskName", task.name)
        }
        taskFormIntent.putExtra("form", form)
        startActivity(taskFormIntent)
    }

    private fun showHome() {
        val homeIntent = Intent(this, HomeActivity::class.java)
        startActivity(homeIntent)
    }

}