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
import androidx.core.content.ContextCompat
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

            if (completed || formatDate(updated) == formatDate(today)) {
                completeHabitButton.visibility = View.GONE
                nocompleteHabitButton.visibility = View.VISIBLE
            } else {
                completeHabitButton.visibility = View.VISIBLE
                nocompleteHabitButton.visibility = View.GONE
            }

            completeHabitButton.setOnClickListener { v ->
                completed = true
                db.collection("habits").document(habitId).update("updated", today)
                calculateProgress(start, today, period.toInt(), times.toInt(), completed, daysCompleted, habitProgress)
                completeHabitButton.visibility = View.GONE
            }

            nocompleteHabitButton.setOnClickListener {
                completed = false
                val cal = Calendar.getInstance()
                cal.time = today
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = cal.time
                db.collection("habits").document(habitId).update("updated", yesterday)
                calculateProgress(start, today, period.toInt(), times.toInt(), completed, daysCompleted, habitProgress)
                nocompleteHabitButton.visibility = View.GONE
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
                        val daysCompleted = it.get("daysCompleted") as String
                        val dates = daysCompleted.split(";")
                        if (dates.contains(formatDate2(day.date))) {
                            textView.setBackgroundResource(R.drawable.day_completed_background)
                            textView.background.setTint(ColorUtils.blendARGB(color.toInt(), Color.BLACK, 0.4f))
                        } else {
                            textView.setBackgroundResource(R.drawable.day_background)
                            textView.background.setTint(color.toInt())
                        }
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
        daysCompleted: String,
        habitProgress: Double
    ) {
        goalProgressBar.max = 100
        var progress: Double
        val handler = Handler()
        var days = daysCompleted
        var done = habitProgress
        Thread {
            handler.post(Runnable {
                var daysCompletedOfPeriod = 0
                    val diff = (((today.time - start.time) / 1000 * 60 * 60 * 24) % period).toInt()
                    if (diff == 0) { // si es el primer dia del periodo
                        println("es el primer dia!!!!!!!!!!!!!!!!!!!!!======")
                        daysCompletedOfPeriod = 0
                        done = 0.0
                    }
                    if (completed) {
                        daysCompletedOfPeriod++
                        if (daysCompleted != "") {
                            days += ";"
                        }
                        days += formatDate(today)//se añade el dia que se ha completado
                    } else {
                        daysCompletedOfPeriod--
                        println("days======" + days)
                        println("formatDate(today)======" + formatDate(today))
                        println("days.contains(formatDate(today)======" + days.contains(formatDate(today)))
                        if (days.contains(formatDate(today))) {
                            days = days.replace(
                                formatDate(today),
                                ""
                            ) //borramos el dia del string de completados
                        }
                    }
                println("days======" + days)
                println("done======" + done)
                println("period======" + period)
                println("times======" + times)
                println("days completed of period======" + daysCompletedOfPeriod)
                progress = done + ((daysCompletedOfPeriod.toDouble() / times.toDouble()) * 100.0)
                println("progress======" + progress)
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

    @SuppressLint("SimpleDateFormat")
    private fun formatDate2(date: LocalDate): String {
        val pattern = "dd/MM/yyyy"
        val simpleDateFormat = DateTimeFormatter.ofPattern(pattern)
        return simpleDateFormat.format(date)
    }
}