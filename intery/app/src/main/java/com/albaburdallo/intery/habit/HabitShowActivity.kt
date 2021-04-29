package com.albaburdallo.intery.habit

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.view.children
import com.albaburdallo.intery.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.activity_calendar_view.*
import kotlinx.android.synthetic.main.activity_habit_show.*
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.habit_calendar_day.view.*
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

class HabitShowActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email
    private lateinit var habitId: String
    private val today = Calendar.getInstance().time
    private var completed = false
    private var selectedDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_show)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        habitId = intent.extras?.getString("habitId") ?: ""

        habitShowBackImageView.setOnClickListener { showHabit() }

//        db.clearPersistence()
        db.collection("habits").document(habitId).get().addOnSuccessListener {
            val start = (it.get("startDate") as Timestamp).toDate() //0%
            val updated = (it.get("updated") as Timestamp).toDate()
            val habitProgress = it.get("progress") as Double
            val period = it.get("period") as Long
            val times = it.get("times") as Long
            val daysCompleted = it.get("daysCompleted") as String

            if (updated == today) {
                completed = true
            }

            goalProgressBar.progress = habitProgress.toInt()
            habitTitleText.text = it.get("name") as String

            if (completed || formatDate(updated)==formatDate(today)) {
                completeHabitButton.visibility = View.GONE
                nocompleteHabitButton.visibility = View.VISIBLE
            } else {
                completeHabitButton.visibility = View.VISIBLE
                nocompleteHabitButton.visibility = View.GONE
            }

            completeHabitButton.setOnClickListener { v ->
                calculateProgress(start, today, period.toInt(), times.toInt(), true, daysCompleted)

                db.collection("habits").document(habitId).update("updated", today)
                completeHabitButton.visibility = View.GONE
                completed = true
            }

            nocompleteHabitButton.setOnClickListener {
                calculateProgress(start, today, period.toInt(), times.toInt(), false, daysCompleted)
                val cal = Calendar.getInstance()
                cal.time = today
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = cal.time
                db.collection("habits").document(habitId).update("updated", yesterday)
                nocompleteHabitButton.visibility = View.GONE
                completed = false
            }

            val color = it.get("color") as String
            goalProgressBar.progressTintList = ColorStateList.valueOf(color.toInt())
            habitEditImageView.setOnClickListener { showHabitForm(
                habitId,
                "edit",
                goalProgressBar.progress
            ) }
        }

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        habitCalendarView.apply {
            setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

        habitCalendarView.post {
            selectDate(LocalDate.now())
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    }
                }
            }
        }

        habitCalendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                    container.day = day
                val textView = container.view.habitCalendarDayTextView
                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.visibility = View.VISIBLE
                    db.collection("habits").document(habitId).get().addOnSuccessListener {
                        val color = it.get("color") as String
                        textView.background.setTint(color.toInt())
                        val daysCompleted = it.get("daysCompleted") as String
                        val dates = daysCompleted.split(";")
                        val cal = Calendar.getInstance()
                        cal[Calendar.DAY_OF_MONTH] = 1
                        val myMonth = cal[Calendar.MONTH]
                        for (date in dates) {
                            while (myMonth == cal[Calendar.MONTH]) {
                                val day = cal.time
                                if (date == formatDate(day)) {
                                    textView.background.setTint(ColorUtils.blendARGB(color.toInt(), Color.BLACK, 0.2f))
                                } else {
                                    textView.background.setTint(color.toInt())
                                }
                                cal.add(Calendar.DAY_OF_MONTH, 1)
                            }

                        }
                        textView.background.setTint(color.toInt())
                    }
                } else {
                    textView.visibility = View.GONE
                }
            }

            override fun create(view: View) = DayViewContainer(view)
        }

        habitCalendarView.monthScrollListener = {
            selectDate(it.yearMonth.atDay(1))
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = view.legendLayout
        }

        habitCalendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, textView ->
                        textView.text = daysOfWeek[index].getDisplayName(
                            TextStyle.SHORT_STANDALONE,
                            Locale.getDefault()
                        ).toString().replace(".", "")
                    }
                }
            }
            override fun create(view: View) = MonthViewContainer(view)
        }
    }

    private fun calculateProgress(
        start: Date,
        today: Date,
        period: Int,
        times: Int,
        completed: Boolean,
        daysCompleted: String
    ) {
        goalProgressBar.max = 100
        var progress: Double
        val handler = Handler()
        var days = daysCompleted
        Thread {
            handler.post(Runnable {
                var daysCompletedOfPeriod = 0
                var day = start
                while (day <= today) {
                    val diff = (((day.time - start.time) / 1000 * 60 * 60 * 24) % period).toInt()
                    if (diff == 0) { // si es el primer dia del periodo
                        daysCompletedOfPeriod = 0
                    }
                    if (days.contains(formatDate(day))) {
                        daysCompletedOfPeriod++
                    }
                    if (completed) {
                        daysCompletedOfPeriod++
                        if (daysCompleted != "") {
                            days += ";"
                        }
                        days += formatDate(day)//se añade el dia que se ha completado
                    } else {
                        daysCompletedOfPeriod--
                        if (days.contains(formatDate(day))) {
                            days = days.replace(
                                formatDate(day),
                                ""
                            ) //borramos el dia del string de completados
                        }
                    }
                    val c = Calendar.getInstance()
                    c.time = day
                    c.add(Calendar.DATE, 1)
                    day = c.time
                }
                progress = (daysCompletedOfPeriod.toDouble() / times.toDouble()) * 100.0
                goalProgressBar.progress = progress.toInt()
                db.collection("habits").document(habitId).update("progress", progress)
                db.collection("habits").document(habitId).update("daysCompleted", days)
            })
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { habitCalendarView.notifyDateChanged(it) }
            habitCalendarView.notifyDateChanged(date)
            habitMonthTextView.text = formatDate(date).substring(
                formatDate(date).indexOf(" "), formatDate(
                    date
                ).length
            )
        }
    }

    private fun showHabit() {
        val habitIntent = Intent(this, HabitActivity::class.java).apply { }
        startActivity(habitIntent)
    }

    private fun showHabitForm(habit: String?, form: String, progress: Int) {
        val habitFormIntent = Intent(this, HabitFormActivity::class.java)
        if (habit != null) {
            habitFormIntent.putExtra("habitId", habit)
        }
        habitFormIntent.putExtra("form", form)
        habitFormIntent.putExtra("progress", progress)
        startActivity(habitFormIntent)
    }

    private fun formatDate(date: Date): String {
        val pattern = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatDate(date: LocalDate): String {
        val pattern = "d MMMM yyyy"
        val simpleDateFormat = DateTimeFormatter.ofPattern(pattern)
        return simpleDateFormat.format(date)
    }
}