package com.albaburdallo.intery.task

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.util.entities.Task
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
import kotlinx.android.synthetic.main.calendar_day_layout.view.*
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import java.text.DateFormatSymbols
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

class CalendarViewActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email;

    private val events = mutableMapOf<LocalDate, List<Task>>()
    private lateinit var eventsAdapter: EventsAdapter
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        backCalendarViewImage.setOnClickListener { showTask() }

        val tasks = arrayListOf<Task>()

        db.collection("tasks").whereEqualTo("user.email", authEmail).addSnapshotListener(this) { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            for (document in value!!) {
                val name = document.get("name") as String
                val startDate = document.get("startDate") as Timestamp
                val endDate = document.get("endDate") as? Timestamp
                val startTime = document.get("startTime") as? Timestamp
                val endTime = document.get("endTime") as? Timestamp
                val allday = document.get("allDay") as Boolean
                val notifyme = document.get("notifyMe") as Boolean
                val notes = document.get("notes") as String
                val done = document.get("done") as Boolean
                val id = document.get("id") as String
                val whenNotification = document.get("when") as String
                val calendar = document.get("calendar") as HashMap<String, String>
                if (endDate != null && startTime != null && endTime != null) {
                    tasks.add(
                        Task(
                            id,
                            name,
                            startDate.toDate(),
                            startTime.toDate(),
                            endDate.toDate(),
                            endTime.toDate(),
                            allday,
                            notifyme,
                            notes,
                            done,
                            com.albaburdallo.intery.util.entities.Calendar(
                                calendar["id"],
                                calendar["name"],
                                calendar["description"],
                                calendar["color"]
                            ),
                            whenNotification
                        )
                    )
                } else {
                    tasks.add(
                        Task(
                            id,
                            name,
                            startDate.toDate(),
                            allday,
                            notifyme,
                            notes,
                            done,
                            com.albaburdallo.intery.util.entities.Calendar(
                                calendar["id"],
                                calendar["name"],
                                calendar["description"],
                                calendar["color"]
                            ),
                            whenNotification
                        )
                    )
                }
            }

            for (task in tasks) {
                val date = task.startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                events[date] = events[date].orEmpty().plus(task)
            }

        }

        eventsAdapter = EventsAdapter()
        layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        calendarRecyclerView.layoutManager = layoutManager
        calendarRecyclerView.adapter = eventsAdapter
//        calendarRecyclerView.addItemDecoration()

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        calendarView.apply {
            setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

        calendarView.post {
            selectDate(today)
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

        calendarView.dayBinder = object  : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.view.calendarDayText
                val dot1view = container.view.calendarDot1
                val dot2view = container.view.calendarDot2
                val dot3view = container.view.calendarDot3
                val plusText = container.view.calendarPlusText
                val calendarDayNotesText = container.view.calendarDayNotesText

                textView.gravity = Gravity.CENTER
                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.visibility = View.VISIBLE
                    calendarDayNotesText.visibility = View.GONE
                    when(day.date) {
                        today -> {
                            textView.setTextColor(ContextCompat.getColor(this@CalendarViewActivity, R.color.yellow))
//                            textView.setBackgroundResource(R.drawable.calendar_selected)
                        }
                        selectedDate -> {
                            textView.setTextColor(ContextCompat.getColor(this@CalendarViewActivity, R.color.white))
                            textView.setBackgroundResource(R.drawable.calendar_selected)
                        }
                        else -> {
                            textView.setTextColor(ContextCompat.getColor(this@CalendarViewActivity, R.color.black))
                            textView.background = null
                        }
                    }
                    if (events[day.date].orEmpty().size >= 4) {
                        dot1view.visibility = View.VISIBLE
                        dot1view.drawable.setTint(events[day.date]!![0].calendar.color.toInt())
                        dot2view.visibility = View.VISIBLE
                        dot2view.drawable.setTint(events[day.date]!![1].calendar.color.toInt())
                        dot3view.visibility = View.VISIBLE
                        dot3view.drawable.setTint(events[day.date]!![2].calendar.color.toInt())
                        plusText.visibility = View.VISIBLE
                    } else if (events[day.date].orEmpty().size == 3) {
                        plusText.visibility = View.GONE
                        dot1view.visibility = View.VISIBLE
                        dot1view.drawable.setTint(events[day.date]!![0].calendar.color.toInt())
                        dot2view.visibility = View.VISIBLE
                        dot2view.drawable.setTint(events[day.date]!![1].calendar.color.toInt())
                        dot3view.visibility = View.VISIBLE
                        dot3view.drawable.setTint(events[day.date]!![2].calendar.color.toInt())
                    } else if (events[day.date].orEmpty().size == 2) {
                        plusText.visibility = View.GONE
                        dot3view.visibility = View.GONE
                        dot1view.visibility = View.VISIBLE
                        dot1view.drawable.setTint(events[day.date]!![0].calendar.color.toInt())
                        dot2view.visibility = View.VISIBLE
                        dot2view.drawable.setTint(events[day.date]!![1].calendar.color.toInt())
                    } else if (events[day.date].orEmpty().size == 1) {
                        plusText.visibility = View.GONE
                        dot3view.visibility = View.GONE
                        dot2view.visibility = View.GONE
                        dot1view.visibility = View.VISIBLE
                        dot1view.drawable.setTint(events[day.date]!![0].calendar.color.toInt())
                    } else {
                        plusText.visibility = View.GONE
                        dot3view.visibility = View.GONE
                        dot2view.visibility = View.GONE
                        dot1view.visibility = View.GONE
                    }
                } else {
                    textView.visibility = View.INVISIBLE
                    dot1view.visibility = View.INVISIBLE
                    dot2view.visibility = View.INVISIBLE
                    dot3view.visibility = View.INVISIBLE
                    plusText.visibility = View.INVISIBLE
                }
            }
        }

        calendarView.monthScrollListener = {
            selectDate(it.yearMonth.atDay(1))
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = view.legendLayout
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View)= MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, textView ->
                        textView.text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.getDefault()).first().toString().capitalize()
                        textView.setTextColor(ContextCompat.getColor(this@CalendarViewActivity, R.color.black))
                    }
                }
            }
        }

        newTaskCalendarViewButton.setOnClickListener {
            var task: Task? = null
            showTaskForm(task, "create")
        }

    }

    private fun showTask() {
        val taskIntent = Intent(this, TaskActivity::class.java).apply { }
        startActivity(taskIntent)
    }

    private fun showTaskForm(task: Task?, form: String) {
        val taskFormIntent = Intent(this, TaskFormActivity::class.java)
        if (task != null) {
            taskFormIntent.putExtra("taskid", task.id.toString())
        }
        taskFormIntent.putExtra("form", form)
        startActivity(taskFormIntent)
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
            oldDate?.let { calendarView.notifyDateChanged(it) }
            calendarView.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }

    private fun updateAdapterForDate(date: LocalDate) {
        eventsAdapter.apply {
            events.clear()
            events.addAll(this@CalendarViewActivity.events[date].orEmpty())
            notifyDataSetChanged()
        }
        selectedDateText.text = formatDate(date)
        monthTextView.text = formatDate(date).substring(formatDate(date).indexOf(" "), formatDate(date).length)
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatDate(date: LocalDate): String {
        val pattern = "d MMMM yyyy"
        val simpleDateFormat = DateTimeFormatter.ofPattern(pattern)
        return simpleDateFormat.format(date)
    }
}