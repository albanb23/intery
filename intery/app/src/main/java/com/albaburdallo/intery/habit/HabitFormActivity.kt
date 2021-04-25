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
import java.util.*

class HabitFormActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var habits: MutableList<Habit>
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email
    private lateinit var habitId: String

    var dateClicked = false
    var timeClicked = false
    private var date: Date? = null
    private var time: Date? = null
    var year = 0
    var month = 0
    var day = 0
    var hour = 0
    var minute = 0

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
    }

    override fun onStart() {
        super.onStart()
        habitId = intent.extras?.getString("habitId") ?: ""

        habitBackImageView.setOnClickListener { showHabit() }

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
                habitDateInput.text = (it.get("endDate") as? Timestamp)?.let { it1 ->
                    formatDate(
                        it1.toDate()
                    )
                }
                habitRemindMeCheckBox.isChecked = it.get("notifyMe") as Boolean
                habitNotesEditText.setText(it.get("description") as String)
                habitReminderTextView.text = (it.get("when") as? Timestamp)?.let { it1 ->
                    formatTime(
                        it1.toDate()
                    )
                }
                habitColorPoint.setColorFilter((it.get("color") as String).toInt())
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
        frequency.add(resources.getString(R.string.everyDayFreq))
        frequency.add(resources.getString(R.string.everyTwoDaysFreq))
        frequency.add(resources.getString(R.string.everyThreeDaysFreq))
        frequency.add(resources.getString(R.string.everyWeekFreq))
        frequency.add(resources.getString(R.string.everyTwoWeeksFreq))
        frequency.add(resources.getString(R.string.everyMonthFreq))
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_item,
            frequency
        )
        frequencySpinner.adapter = adapter

        if (habitId!=""){
            db.collection("habits").document(habitId).get().addOnSuccessListener {
                val freq = (it.get("frequency") as Long).toInt()
                when (freq) {
                    0 -> frequencySpinner.setSelection(0)
                    1 -> frequencySpinner.setSelection(1)
                    2 -> frequencySpinner.setSelection(2)
                    3 -> frequencySpinner.setSelection(3)
                    4 -> frequencySpinner.setSelection(4)
                    5 -> frequencySpinner.setSelection(5)
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
                        colorButton.drawable.setTint(color)
                    })
                .show(supportFragmentManager)
        }


        if (habitReminderTextView.text != "" && habitReminderTextView.text != null && !habitRemindMeCheckBox.isChecked){
            habitRemindMeCheckBox.isChecked = true
        }

        habits = arrayListOf()

        habitDateInput.setOnClickListener {
            dateClicked = true
            val calendar: Calendar = Calendar.getInstance()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(this, this, year, month, day)
            datePickerDialog.show()
        }

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
        val endDate = date
        var frequency = 0
        when (habitFrequencySpinner.selectedItem) {
            resources.getString(R.string.everyDayFreq) -> {
                frequency = 0
            }
            resources.getString(R.string.everyTwoDaysFreq) -> {
                frequency = 1
            }
            resources.getString(R.string.everyThreeDaysFreq) -> {
                frequency = 2
            }
            resources.getString(R.string.everyWeekFreq) -> {
                frequency = 3
            }
            resources.getString(R.string.everyTwoWeeksFreq) -> {
                frequency = 4
            }
            resources.getString(R.string.everyMonthFreq) -> {
                frequency = 5
            }
        }
        val reminder = habitRemindMeCheckBox.isChecked
        val remindTime = time
        val color = selectedColor.toString()
        val notes = habitNotesEditText.text.toString()
        val today = Calendar.getInstance().time
        val id = authEmail + "-" + name
        val cal = Calendar.getInstance()
        cal.time = today
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val oneDayBefore = cal.time

        val habit = Habit(
            id,
            name,
            notes,
            today,
            endDate,
            color,
            reminder,
            remindTime,
            frequency,
            100,
            oneDayBefore
        )

        if (endDate!! < today) {
            Toast.makeText(
                this,
                this.getString(R.string.habitDateValidator),
                Toast.LENGTH_LONG
            ).show()
        } else {
            db.collection("habits").document(habit.id).set(habit)
            habits.add(habit)
            showHabit()
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

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val textDate = dayOfMonth.toString() + "/" + (month + 1).toString() + "/" + year.toString()
        if (dateClicked) {
            habitDateInput.setText(textDate)
            date = Date(year - 1900, month, dayOfMonth)
            dateClicked = false
        }
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