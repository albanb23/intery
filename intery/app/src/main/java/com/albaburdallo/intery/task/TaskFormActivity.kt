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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TaskFormActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener  {
    private lateinit var adapter: TaskAdapter
    private lateinit var tasks: ArrayList<Task>
    private val db = FirebaseFirestore.getInstance()

    var startClicked = false
    var endClicked = false
    var dayStart = 0
    var monthStart: Int = 0
    var yearStart: Int = 0
    var hourStart: Int = 0
    var minuteStart: Int = 0
    var dayEnd = 0
    var monthEnd: Int = 0
    var yearEnd: Int = 0
    var hourEnd: Int = 0
    var minuteEnd: Int = 0
    var myDay = 0
    var myMonth: Int = 0
    var myYear: Int = 0
    var myHour: Int = 0
    var myMinute: Int = 0
    lateinit var start: Date
    lateinit var end: Date
    lateinit var startTime: Date
    lateinit var endTime: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_form)
        this.getSupportActionBar()?.hide()
//        activity!!.layoutInflater.inflate(R.layout.activity_task_form, null)
        setup()
    }

    private fun setup() {
        tasks = arrayListOf()

        backImageView.setOnClickListener { onBackPressed() }

        startDateInput.setOnClickListener {
            startClicked = true
            val calendar: Calendar = Calendar.getInstance()
            dayStart = calendar.get(Calendar.DAY_OF_MONTH)
            monthStart = calendar.get(Calendar.MONTH)
            yearStart = calendar.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(this, this, yearStart, monthStart, dayStart)
            datePickerDialog.show()
        }

        endDateInput.setOnClickListener {
            endClicked = true
            val calendar: Calendar = Calendar.getInstance()
            dayEnd = calendar.get(Calendar.DAY_OF_MONTH)
            monthEnd = calendar.get(Calendar.MONTH)
            yearEnd = calendar.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(this, this, yearEnd, monthEnd, dayEnd)
            datePickerDialog.show()
        }

        startTimeInput.setOnClickListener {
            startClicked = true
            val calendar: Calendar = Calendar.getInstance()
            hourStart = calendar.get(Calendar.HOUR_OF_DAY)
            minuteStart = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, this, hourStart, minuteStart, true)
            timePickerDialog.show()
        }

        endTimeInput.setOnClickListener {
            endClicked = true
            val calendar: Calendar = Calendar.getInstance()
            hourEnd = calendar.get(Calendar.HOUR_OF_DAY)
            minuteEnd = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, this, hourEnd, minuteEnd, true)
            timePickerDialog.show()
        }

        adapter = TaskAdapter(this, tasks)
        saveTaskButton.setOnClickListener { i ->  addTask() }
    }

    private fun addTask() {
        val name = taskNameEditText.text.toString()
        val notes = notesEditText.text.toString()
        if (!TextUtils.isEmpty(name)) {
            var task = Task(name, start, end, startTime, endTime, false, false, notes)
            if (task != null) {
//                task.toMap()
                db.collection("tasks").document(name).set(task)

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

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        myDay = day
        myYear = year
        myMonth = month + 1
        val date = myDay.toString() + "/" + myMonth.toString() + "/" + myYear.toString()
        val calendar: Calendar = Calendar.getInstance()
        if(startClicked) {
            startDateInput.text = date
            start = Date(myYear-1900, myMonth, myDay)
            startClicked = false
        } else if (endClicked) {
            endDateInput.text = date
            end = Date(myYear-1900, myMonth, myDay)
            endClicked = false
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        myHour = hourOfDay
        myMinute = minute
        val date = myHour.toString() + ":" + myMinute.toString()
        if(startClicked) {
            startTimeInput.text = date
            startTime = Date(myYear-1900, myMonth, myDay, myHour, myMinute)
            startClicked = false
        } else if (endClicked) {
            endTimeInput.text = date
            endTime = Date(myYear-1900, myMonth, myDay, myHour, myMinute)
            endClicked = false
        }
    }

}