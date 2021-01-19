package com.albaburdallo.intery.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat.is24HourFormat
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import com.albaburdallo.intery.HomeActivity
import com.albaburdallo.intery.LoginActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_task_form.*
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class TaskFormActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener  {
    private lateinit var adapter: TaskAdapter
    private lateinit var tasks: ArrayList<Task>
    private val db = FirebaseFirestore.getInstance()

    var day = 0
    var month: Int = 0
    var year: Int = 0
    var hour: Int = 0
    var minute: Int = 0
    var myDay = 0
    var myMonth: Int = 0
    var myYear: Int = 0
    var myHour: Int = 0
    var myMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_form)
        this.getSupportActionBar()?.hide()
//        activity!!.layoutInflater.inflate(R.layout.activity_task_form, null)
        setup()
    }

    private fun setup() {
        tasks = arrayListOf()

        adapter = TaskAdapter(this, tasks)
        saveTaskButton.setOnClickListener { i ->  addTask() }
    }

    private fun addTask() {
        val name = taskNameEditText.text.toString()

        startDateEditText.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(this, this, year, month, day)
            datePickerDialog.show()
        }

        if (!TextUtils.isEmpty(name)) {
            var task = Task(name)
            if (task != null) {
                task.toMap()
                db.collection("tasks").document(taskNameEditText.text.toString()).set(task)

                tasks.add(task)
                adapter.notifyDataSetChanged()
                showList(tasks)
            }
        }
    }

    private fun showList(tasks: ArrayList<Task>) {
        val listIntent = Intent(this, TaskActivity::class.java)
        startActivity(listIntent)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        myDay = day
        myYear = year
        myMonth = month
        val calendar: Calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this, this, hour, minute, true)
        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        myHour = hourOfDay
        myMinute = minute
        val date = "$myDay/$myMonth/$myYear $myHour:$myMinute"
        startDateEditText.setText(date)
    }

}