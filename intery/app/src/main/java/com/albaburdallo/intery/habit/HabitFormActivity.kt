package com.albaburdallo.intery.habit

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.util.entities.Habit
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import dev.sasikanth.colorsheet.ColorSheet
import kotlinx.android.synthetic.main.activity_habit_form.*
import kotlinx.android.synthetic.main.activity_task_form.*
import kotlinx.android.synthetic.main.habit_list.*
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.*

class HabitFormActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var habits: MutableList<Habit>
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email
    private lateinit var habitId: String
    private var habitProgress = 0

    var timeClicked = false
    private var time: Date? = null
    var year = 0
    var month = 0
    var day = 0
    var hour = 0
    var minute = 0
    private var updated = Calendar.getInstance().time
    private var startDate: Date? = null
    private var habitName: String? = null
    private var days = ""

    companion object {
        private const val COLOR_SELECTED = "selectedColor"
        private const val NO_COLOR_OPTION = "noColorOption"
    }

    private var selectedColor: Int = ColorSheet.NO_COLOR
    private var noColorOption = false
    private lateinit var colors: IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_form)
        colors = this.resources.getIntArray(R.array.colors)
        selectedColor = savedInstanceState?.getInt(COLOR_SELECTED)?:colors.first()
        noColorOption = savedInstanceState?.getBoolean(NO_COLOR_OPTION)?:false
        habitProgress = intent.extras?.getInt("progress") ?: 0
    }

    override fun onStart() {
        super.onStart()
        habitId = intent.extras?.getString("habitId") ?: ""

        habitBackImageView.setOnClickListener {
            if (habitId=="") {
                showHabit()
            } else {
                showHabit(habitId)
            }
        }

        val form = intent.extras?.getString("form") ?: ""
        if (form == "edit") {
            habitsTrashImageView.visibility = View.VISIBLE
            createHabitText.text = resources.getString(R.string.editHabit)
        } else {
            habitsTrashImageView.visibility = View.INVISIBLE
            createHabitText.text = resources.getString(R.string.newHabit)
        }

        if (habitId != "") {
            db.collection("habits").document(habitId).get().addOnSuccessListener {
                habitNameEditText.setText(it.get("name") as String)
                habitName = it.get("name") as String
                updated = (it.get("updated") as? Timestamp)?.toDate()
                startDate = (it.get("startDate") as? Timestamp)?.toDate()
                habitRemindMeCheckBox.isChecked = it.get("notifyMe") as Boolean
                habitNotesEditText.setText(it.get("notes") as String)
                days = it.get("daysCompleted") as String
                habitReminderTextView.text = (it.get("when") as? Timestamp)?.let { it1 ->
                    formatTime(
                        it1.toDate()
                    )
                }
                habitColorPoint.setColorFilter((it.get("color") as String).toInt())
                selectedColor = (it.get("color") as String).toInt()
            }

            habitsTrashImageView.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(this.resources.getString(R.string.delete))
                    .setCancelable(false)
                    .setPositiveButton(this.resources.getString(R.string.yes)) { dialog, id ->
                        db.collection("habits").document(habitId).delete()
                        showHabit()
                    }
                    .setNegativeButton(this.resources.getString(R.string.no)) { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
        }

        val frequencySpinner: Spinner = findViewById(R.id.habitFrequencySpinner)
        var frequency: ArrayList<String> = arrayListOf()
        frequency.add(resources.getString(R.string.everyDay))
        frequency.add(resources.getString(R.string.twoAWeek))
        frequency.add(resources.getString(R.string.oneAWeek))
        frequency.add(resources.getString(R.string.twoAMonth))
        frequency.add(resources.getString(R.string.oneAMonth))
        frequency.add(resources.getString(R.string.threeWeek))
        frequency.add(resources.getString(R.string.threeMonth))
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_item,
            frequency
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        frequencySpinner.adapter = adapter

        if (habitId!=""){
            db.collection("habits").document(habitId).get().addOnSuccessListener {
                val period = (it.get("period") as Long).toInt()
                val times = (it.get("times") as Long).toInt()
                if (period == 7) {
                    when (times) {
                        1 -> frequencySpinner.setSelection(2)
                        2 -> frequencySpinner.setSelection(1)
                        3 -> frequencySpinner.setSelection(5)
                        7 -> frequencySpinner.setSelection(0)
                    }
                } else {
                    when (times) {
                        1 -> frequencySpinner.setSelection(5)
                        2 -> frequencySpinner.setSelection(3)
                        3 -> frequencySpinner.setSelection(6)
                    }
                }
            }
        }

        val colorButton = findViewById<ImageView>(R.id.habitColorPoint)
        colorButton.setOnClickListener {
            ColorSheet().cornerRadius(8)
                .colorPicker(colors = colors,
                    noColorOption = noColorOption,
                    selectedColor = selectedColor,
                    listener = { color ->
                        selectedColor = color
                        colorButton.drawable.setTint(selectedColor)
                    })
                .show(supportFragmentManager)

        }

        if (habitReminderTextView.text != "" && habitReminderTextView.text != null && !habitRemindMeCheckBox.isChecked){
            habitRemindMeCheckBox.isChecked = true
        }

        habits = arrayListOf()

        habitReminderTextView.setOnClickListener {
            timeClicked = true
            val calendar: Calendar = Calendar.getInstance()
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, this, hour, minute, true)
            timePickerDialog.show()
        }

        saveHabitButton.setOnClickListener {
            if (validateForm()) {
                addHabit()
            }
        }

    }

    private fun validateForm():Boolean {
        var res = true
        res && habitNameEditText.validator().nonEmpty().addErrorCallback { habitNameEditText.error = it }.check()
        if (habitRemindMeCheckBox.isChecked) {
            res && habitReminderTextView.validator().nonEmpty().addErrorCallback { habitReminderTextView.error = it }.check()
        }
        if (habitReminderTextView.text != "" && habitReminderTextView.text != null && !habitRemindMeCheckBox.isChecked){
            habitRemindMeCheckBox.isChecked = true
        }

        return res
    }

    private fun addHabit() {
        val name = habitNameEditText.text.toString()
        var period = 0
        var times = 0
        when (habitFrequencySpinner.selectedItem) {
            resources.getString(R.string.everyDay) -> {
                period = 7
                times = 7
            }
            resources.getString(R.string.twoAWeek) -> {
                period = 7
                times = 2
            }
            resources.getString(R.string.oneAWeek) -> {
                period = 7
                times = 1
            }
            resources.getString(R.string.twoAMonth) -> {
                period = 30
                times = 2
            }
            resources.getString(R.string.oneAMonth) -> {
                period = 30
                times = 1
            }
            resources.getString(R.string.threeWeek) -> {
                period = 7
                times = 3
            }
            resources.getString(R.string.threeMonth) -> {
                period = 30
                times = 3
            }
        }
        val reminder = habitRemindMeCheckBox.isChecked
        val remindTime = time
        val color = selectedColor.toString()
        val notes = habitNotesEditText.text.toString()
        val today = Calendar.getInstance().time
        var start = startDate
        if (startDate == null) {
            start = today
        }

        var id = habitId
        if (id=="") {
            id = authEmail + "-" + name
        }
        var oneDayBefore = updated
        if (habitId == "" || (habitId!="" && formatDate(updated) != formatDate(today))) {
            val cal = Calendar.getInstance()
            cal.time = today
            cal.add(Calendar.DAY_OF_YEAR, -1)
            oneDayBefore = cal.time
        }

        val habit = Habit(id, name, notes, start, color, reminder, remindTime,
            period, times, habitProgress.toDouble(), oneDayBefore, days)

            db.collection("habits").whereEqualTo("user.email", authEmail).get().addOnSuccessListener { documents ->
                var exists = false
                for (document in documents) {
                    if (habitName!=null && habitName!=name) { //si el nombre que se esta escribiendo es distinto que el que estaba
                        val habitname = document.get("name") as String
                        if (habitname == name) { //comprobamos que otro habito no tenga ese nombre
                            exists = true
                            break
                        }
                    }
                }
                if (!exists) {
                    db.collection("habits").document(habit.id).set(habit)
                    habits.add(habit)
                    showHabit()
                } else {
                    Toast.makeText(this, R.string.exisitingHabit, Toast.LENGTH_LONG).show()
                }
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

    private fun showHabit() {
        val habitIntent = Intent(this, HabitActivity::class.java).apply { }
        startActivity(habitIntent)
    }

    private fun showHabit(habitId: String) {
        val habitIntent = Intent(this, HabitShowActivity::class.java)
        if (habitId!=null) {
            habitIntent.putExtra("habitId", habitId)
        }
        startActivity(habitIntent)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val textTime = hourOfDay.toString() + ":" + minute.toString()
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        if (timeClicked) {
            habitReminderTextView.setText(textTime)
            time = Date(year - 1900, month, day, hourOfDay, minute)
            timeClicked = false
        }
    }
}