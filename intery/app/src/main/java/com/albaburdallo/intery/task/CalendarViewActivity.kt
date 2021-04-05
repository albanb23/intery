package com.albaburdallo.intery.task

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.util.entities.Task
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.activity_calendar_view.*
import kotlinx.android.synthetic.main.calendar_day_layout.view.*
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

class CalendarViewActivity : AppCompatActivity() {

    private val events = mutableMapOf<LocalDate, List<Task>>()
    private lateinit var eventsAdapter: EventsAdapter
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        backCalendarViewImage.setOnClickListener { showTask() }

        eventsAdapter = EventsAdapter()
        calendarRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
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

                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.visibility = View.VISIBLE
                    when(day.date) {
                        today -> {
                            textView.setTextColor(ContextCompat.getColor(this@CalendarViewActivity, R.color.yellow))
//                            textView.setBackgroundResource(R.drawable.)
                        }
                        selectedDate -> {
                            textView.setTextColor(ContextCompat.getColor(this@CalendarViewActivity, R.color.red))
//                            textView.setBackgroundResource(R.drawable.)
                        }
                        else -> {
                            textView.setTextColor(ContextCompat.getColor(this@CalendarViewActivity, R.color.black))
                            textView.background = null
                            dot1view.isVisible = events[day.date].orEmpty().isNotEmpty()
//                            dot2view.isVisible = events[day.date].orEmpty().isNotEmpty()
//                            dot3view.isVisible = events[day.date].orEmpty().isNotEmpty()
//                            plusText.isVisible = events[day.date].orEmpty().isNotEmpty()
                        }
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
//            homeActivityToolbar.title = if (it.year == today.year) {
//                titleSameYearFormatter.format(it.yearMonth)
//            } else {
//                titleFormatter.format(it.yearMonth)
//            }
//
//            // Select the first day of the month when
//            // we scroll to a new month.
//            selectDate(it.yearMonth.atDay(1))
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
                        textView.text = daysOfWeek[index].name.first().toString()
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
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatDate(date: LocalDate): String {
        val pattern = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(date)
    }
}