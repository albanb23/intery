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
import com.albaburdallo.intery.BaseActivity
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
import kotlinx.android.synthetic.main.activity_habit_show.habitCalendarView
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.habit_calendar_day.*
import kotlinx.android.synthetic.main.habit_calendar_day.view.*
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

class HabitShowActivity : BaseActivity() {

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

        //populamos la vista
        db.collection("habits").document(habitId).get().addOnSuccessListener {
            val start = (it.get("startDate") as Timestamp).toDate() //0%
            val updated = (it.get("updated") as Timestamp).toDate()
            var habitProgress = it.get("progress") as Double
            val period = it.get("period") as Long
            val times = it.get("times") as Long
            val daysCompleted = it.get("daysCompleted") as String
            val color = it.get("color") as String

            val diff = ((today.time - start.time) / (1000*60*60*24)) //dias desde que empezo hasta hoy
            println("diff============" + diff)
            println("period============" + period)
            if (diff >= period) { //si la ultima vez que se actualizo fue fuera del perdiodo, el progreso se actualiza
                habitProgress = 0.0
            }

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
                //se pone el dia en el calendario mas oscuro
                habitCalendarDayTextView.setBackgroundResource(R.drawable.day_completed_background)
                habitCalendarDayTextView.background.setTint(ColorUtils.blendARGB(color.toInt(), Color.BLACK, 0.4f))
                habitCalendarView.notifyCalendarChanged()

                completed = true
                db.collection("habits").document(habitId).update("updated", today)
                calculateProgress(start, today, period.toInt(), times.toInt(), completed, daysCompleted, habitProgress)
                completeHabitButton.visibility = View.GONE
            }

            nocompleteHabitButton.setOnClickListener {
                //se pone el dia en el calendario mas claro
                habitCalendarDayTextView.setBackgroundResource(R.drawable.day_background)
                habitCalendarDayTextView.background.setTint(color.toInt())
                habitCalendarView.notifyCalendarChanged()

                completed = false
                //se actualiza el atributo update a el día anterior
                val cal = Calendar.getInstance()
                cal.time = today
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = cal.time
                db.collection("habits").document(habitId).update("updated", yesterday)
                calculateProgress(start, today, period.toInt(), times.toInt(), completed, daysCompleted, habitProgress)
                nocompleteHabitButton.visibility = View.GONE
            }

            goalProgressBar.progressTintList = ColorStateList.valueOf(color.toInt())
            habitEditImageView.setOnClickListener { showHabitForm(
                habitId,
                "edit",
                goalProgressBar.progress
            ) }
        }

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        //se crea el calendario
        habitCalendarView.apply {
            //se especifican el primer mes (actual-10), el ultimo (actual+10) y el primer dia de la semana según el locale
            setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
            scrollToMonth(currentMonth)//lleva al mes actual
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
                textView.text = day.date.dayOfMonth.toString() //se ponen todos los dias del mes
                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.visibility = View.VISIBLE
                    db.collection("habits").document(habitId).get().addOnSuccessListener {
                        val color = it.get("color") as String
                        val daysCompleted = it.get("daysCompleted") as String
                        val dates = daysCompleted.split(";")
                        if (dates.contains(formatDate2(day.date))) { //se ponen mas oscuros los dias en los que el habito ha sido completado
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
            selectDate(it.yearMonth.atDay(1)) //se selecciona el mes actual
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = view.legendLayout
        }

        habitCalendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    //se ponen los dias de la semana segun el locale
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, textView ->
                        textView.text = daysOfWeek[index].getDisplayName(
                            TextStyle.SHORT_STANDALONE,
                            resources?.configuration?.locales?.get(0)
                        ).toString().replace(".", "")
                    }
                }
            }
            override fun create(view: View) = MonthViewContainer(view)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
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
                        if (days.contains(formatDate(today))) {
                            days = days.replace(
                                formatDate(today),
                                ""
                            ) //borramos el dia del string de completados
                        }
                    }
                progress = done + ((daysCompletedOfPeriod.toDouble() / times.toDouble()) * 100.0)
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(this.resources?.configuration?.locales?.get(0)).firstDayOfWeek
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
            habitMonthTextView.text = formatMonth(date)
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun formatDate(date: Date): String {
        val pattern = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern, resources?.configuration?.locales?.get(0))
        return simpleDateFormat.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SimpleDateFormat")
    private fun formatDate(date: LocalDate): String {
        val pattern = "d MMMM yyyy"
        val simpleDateFormat = DateTimeFormatter.ofPattern(pattern,resources?.configuration?.locales?.get(0))
        return simpleDateFormat.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SimpleDateFormat")
    private fun formatMonth(date: LocalDate): String {
        val pattern = "MMMM"
        val simpleDateFormat = DateTimeFormatter.ofPattern(pattern,resources?.configuration?.locales?.get(0))
        return simpleDateFormat.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SimpleDateFormat")
    private fun formatDate2(date: LocalDate): String {
        val pattern = "dd/MM/yyyy"
        val simpleDateFormat = DateTimeFormatter.ofPattern(pattern,resources?.configuration?.locales?.get(0))
        return simpleDateFormat.format(date)
    }
}