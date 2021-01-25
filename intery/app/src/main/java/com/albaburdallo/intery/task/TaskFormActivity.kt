package com.albaburdallo.intery.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat.is24HourFormat
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.albaburdallo.intery.HomeActivity
import com.albaburdallo.intery.LoginActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_task_form.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TaskFormActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var adapter: TaskAdapter
    private lateinit var tasks: ArrayList<Task>
    private val db = FirebaseFirestore.getInstance()
    lateinit var task: Task

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
    private var start: Date? = null
    private var end: Date? = null
    private var startTime: Date? = null
    private var endTime: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_form)
        this.getSupportActionBar()?.hide()


        val taskname = intent.extras?.getString("taskName") ?: ""
        if (taskname != "") {
            db.collection("tasks").document(taskname).get().addOnSuccessListener {
                taskNameEditText.setText(it.get("name") as? String)
                startDateInput.text = formatDate((it.get("startDate") as Timestamp).toDate())
                start = (it.get("startDate") as Timestamp).toDate()
                if (it.get("startTime") != null && it.get("endDate") != null && it.get("endTime") != null) {
                    startTimeInput.text = formatTime((it.get("startTime") as Timestamp).toDate())
                    startTime = (it.get("startTime") as Timestamp).toDate()
                    endDateInput.text = formatDate((it.get("endDate") as Timestamp).toDate())
                    end = (it.get("endDate") as Timestamp).toDate()
                    endTimeInput.text = formatTime((it.get("endTime") as Timestamp).toDate())
                    endTime = (it.get("endTime") as Timestamp).toDate()
                }
                allDayCheckBox.isChecked = it.get("allDay") as Boolean
                remindMeCheckBox.isChecked = it.get("notifyMe") as Boolean
                notesEditText.setText(it.get("notes") as? String)
            }

            trashImageView.setOnClickListener {
                db.collection("tasks").document(taskname).delete()
                showList()
            }
        }
        setup()

    }

    private fun setup() {
        tasks = arrayListOf()

        backImageView.setOnClickListener { onBackPressed() }

        val form = intent.extras?.getString("form")?:""
        if (form == "edit") {
            trashImageView.visibility = View.VISIBLE
        } else {
            trashImageView.visibility = View.INVISIBLE
        }

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

        allDayCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                startTime = null;
                end = null;
                endTime = null;
                startTimeInput.text = ""
                endTimeInput.text = ""
                endDateInput.text = ""
                startTimeInput.isEnabled = false
                endDateInput.isEnabled = false
                endTimeInput.isEnabled = false
            } else {
                startTimeInput.isEnabled = true
                endDateInput.isEnabled = true
                endTimeInput.isEnabled = true
            }

        }

        adapter = TaskAdapter(this, tasks)
        saveTaskButton.setOnClickListener { i ->
            if (validateForm()) {
                addTask()
            }
        }

    }

    private fun addTask() {
        val name = taskNameEditText.text.toString()
        val notes = notesEditText.text.toString()
        val allDay = allDayCheckBox.isChecked
        val remindMe = remindMeCheckBox.isChecked

        if (!TextUtils.isEmpty(name)) {
            if (end != null && startTime != null && endTime != null) {
                println("======start time=====" + startTime)
                task = Task(name, start, startTime, end, endTime, allDay, remindMe, notes, false)
            } else {
                task = Task(name, start, allDay, remindMe, notes, false)
            }

            if (task != null) {
                db.collection("tasks").document(name).set(task as Task)

                tasks.add(task as Task)
                adapter.notifyDataSetChanged()
                showList()
            }
        }
    }

    private fun validateForm(): Boolean {
        var res = true
        res =
            taskNameEditText.validator().nonEmpty().addErrorCallback { taskNameEditText.error = it }
                .check()
                    && startDateInput.validator().nonEmpty()
                .addErrorCallback { startDateInput.error = it }.check()
        if (!allDayCheckBox.isChecked) {
            res =
                startTimeInput.validator().nonEmpty().addErrorCallback { startTimeInput.error = it }
                    .check()
                        && endDateInput.validator().nonEmpty()
                    .addErrorCallback { endDateInput.error = it }.check()
                        && endTimeInput.validator().nonEmpty()
                    .addErrorCallback { endTimeInput.error = it }.check()
        }

        return res
    }

    private fun showList() {
        val listIntent = Intent(this, TaskActivity::class.java)
        startActivity(listIntent)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        myDay = day
        myYear = year
        myMonth = month
        val date = myDay.toString() + "/" + (myMonth+1).toString() + "/" + myYear.toString()
        val calendar: Calendar = Calendar.getInstance()
        if (startClicked) {
            startDateInput.text = date
            start = Date(myYear - 1900, myMonth, myDay)
            startClicked = false
        } else if (endClicked) {
            endDateInput.text = date
            end = Date(myYear - 1900, myMonth, myDay)
            endClicked = false
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        myHour = hourOfDay
        myMinute = minute
        val date = myHour.toString() + ":" + myMinute.toString()
        if (startClicked) {
            startTimeInput.text = date
            startTime = Date(myYear - 1900, myMonth, myDay, myHour, myMinute)
            startClicked = false
        } else if (endClicked) {
            endTimeInput.text = date
            endTime = Date(myYear - 1900, myMonth, myDay, myHour, myMinute)
            endClicked = false
        }
    }

    private fun formatDate(date: Date): String {
        val pattern = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(date)
    }

    private fun formatTime(date: Date): String {
        var res = ""
        val pattern = "dd/MM/yyyy HH:mm"
        val simpleDateFormat = SimpleDateFormat(pattern)
        res = simpleDateFormat.format(date)
        val index = res.indexOf(" ") + 1
        res = res.substring(index)
        return res
    }

}