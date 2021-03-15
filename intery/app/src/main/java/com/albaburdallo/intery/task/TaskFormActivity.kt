package com.albaburdallo.intery.task

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.entities.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.android.synthetic.main.activity_task_form.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class TaskFormActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    //    private lateinit var adapter: TaskAdapter
    private lateinit var tasks: ArrayList<Task>
    private lateinit var calendars: ArrayList<String>
    private val db = FirebaseFirestore.getInstance()
    lateinit var task: Task
    private var calendarId: String = ""

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
    private lateinit var taskid: String
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_form)
        this.getSupportActionBar()?.hide()

    }

    override fun onStart() {
        super.onStart()
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        calendarId = prefs.getString("calendar", "").toString()

        taskid = intent.extras?.getString("taskid") ?: ""

        if (taskid != "") {
            db.collection("tasks").document(taskid).get().addOnSuccessListener {value ->
                taskNameEditText.setText(value!!.get("name") as? String)
                startDateInput.text = formatDate((value.get("startDate") as Timestamp).toDate())
                start = (value.get("startDate") as Timestamp).toDate()
                if (value.get("startTime") != null && value.get("endDate") != null && value.get("endTime") != null) {
                    startTimeInput.text = formatTime((value.get("startTime") as Timestamp).toDate())
                    startTime = (value.get("startTime") as Timestamp).toDate()
                    endDateInput.text = formatDate((value.get("endDate") as Timestamp).toDate())
                    end = (value.get("endDate") as Timestamp).toDate()
                    endTimeInput.text = formatTime((value.get("endTime") as Timestamp).toDate())
                    endTime = (value.get("endTime") as Timestamp).toDate()
                }
                allDayCheckBox.isChecked = value.get("allDay") as Boolean
                remindMeCheckBox.isChecked = value.get("notifyMe") as Boolean
                notesEditText.setText(value.get("notes") as? String)
            }

            trashImageView.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(this.resources.getString(R.string.delete))
                    .setCancelable(false)
                    .setPositiveButton(this.resources.getString(R.string.yes)) { dialog, id ->
                        db.collection("tasks").document(taskid).delete()
                        showList()
                    }
                    .setNegativeButton(this.resources.getString(R.string.no)) { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
        }

        val spinner: Spinner = findViewById(R.id.calendarSelect)
        calendars = arrayListOf()
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            applicationContext,
            android.R.layout.simple_spinner_item,
            calendars
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        db.collection("calendars").orderBy("created").addSnapshotListener(this) { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            calendars.clear()
            for (document in value!!) {
                val user = document.get("user") as HashMap<*, *>
                if (user["email"] == authEmail) {
                    val calendarName = document.get("name") as String
                    calendars.add(calendarName)
                }
            }
            adapter.notifyDataSetChanged()

            if (taskid != "") {
                db.collection("tasks").document(taskid).get().addOnSuccessListener {value ->
                    val cal = value!!.get("calendar") as HashMap<*, *>
                    for (calendar in calendars) {
                        if (calendar == cal["name"]) {
                            spinner.setSelection(calendars.indexOf(calendar))
                            break
                        }
                    }
                }
            } else {
                if (calendarId != "") {
                    for (calendar in calendars) {
                        if (calendar == calendarId.substring(0, calendarId.indexOf("-"))) {
                            spinner.setSelection(calendars.indexOf(calendar))
                            break
                        }
                    }
                } else {
                    spinner.setSelection(0)
                }
            }
        }

        tasks = arrayListOf()

        backImageView.setOnClickListener { onBackPressed() }

        val form = intent.extras?.getString("form") ?: ""
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

//        adapter = TaskAdapter(this, tasks)
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
        val calendarTitle = calendarSelect.selectedItem.toString()
        var calendar: com.albaburdallo.intery.model.entities.Calendar? = null
        db.collection("calendars").whereEqualTo("name", calendarTitle)
            .addSnapshotListener(this) { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                for (document in value!!) {
                    val user = document.get("user") as HashMap<*, *>
                    if (user["email"] == authEmail) {
                        calendar = com.albaburdallo.intery.model.entities.Calendar(
                            document.get("id") as String,
                            document.get("name") as String,
                            document.get("description") as String,
                            document.get("color") as String
                        )
                    }
                }
                val idList = arrayListOf<Int>()
                db.collection("tasks").orderBy("id", Query.Direction.DESCENDING).get().addOnSuccessListener {value ->
                    for (document in value!!) {
                        val documentId = document.get("id") as String
                        val subId = documentId.substring(0, documentId.indexOf("-"))
                        idList.add(subId.toInt())
                    }
                    var id = taskid
                    if (id == "") {
                        var int = 1
                        if (idList.isNotEmpty()) {
                            int = idList[0] + 1
                        }
                        id = int.toString() + "-" + name + "-" + authEmail
                    }

                    if (!TextUtils.isEmpty(name)) {
                        if (end != null && startTime != null && endTime != null) {
                            task = Task(
                                id,
                                name,
                                start,
                                startTime,
                                end,
                                endTime,
                                allDay,
                                remindMe,
                                notes,
                                false,
                                calendar
                            )
                        } else {
                            task = Task(
                                id,
                                name,
                                start,
                                allDay,
                                remindMe,
                                notes,
                                false,
                                calendar
                            )
                        }

                        if (!allDay && (task.startDate > task.endDate || (task.startDate == task.endDate && task.startTime.time > task.endTime.time))) {
                            Toast.makeText(
                                this,
                                this.getString(R.string.taskDateValidation),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            db.collection("tasks").document(task.id).set(task as Task)
                            tasks.add(task as Task)
                            showList()
                        }
                    }
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
        if (calendarSelect.selectedItem == null) {
            Toast.makeText(
                this,
                this.getString(R.string.calendarValidation),
                Toast.LENGTH_LONG
            ).show()
            res = false
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
        val date = myDay.toString() + "/" + (myMonth + 1).toString() + "/" + myYear.toString()
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
        when {
            myYear == 0 -> {
                myYear = Calendar.getInstance().get(Calendar.YEAR)
            }
            myMonth == 0 -> {
                myMonth = Calendar.getInstance().get(Calendar.MONTH)
            }
            myDay == 0 -> {
                myDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            }
        }

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
        var res: String
        val pattern = "dd/MM/yyyy HH:mm"
        val simpleDateFormat = SimpleDateFormat(pattern)
        res = simpleDateFormat.format(date)
        val index = res.indexOf(" ") + 1
        res = res.substring(index)
        return res
    }

}