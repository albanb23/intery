package com.albaburdallo.intery.task

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.util.entities.Task
import kotlinx.android.synthetic.main.calendar_item_layout.view.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class EventsItemAdapter(val context: Context): RecyclerView.Adapter<EventsItemAdapter.EventsViewHolder>() {

    val events: MutableList<Task> = mutableListOf()
    private var clickListener: CalendarAdapter.ClickListener? = null

    inner class EventsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        fun bind(task: Task) {
            itemView.taskNameTextView.text = task.name
            itemView.taskNotesTextView.text = task.notes
            itemView.dotCalendarImageView.drawable.setTint(task.calendar.color.toInt())
            itemView.taskTimeTextView.text = putDates(task)
        }

        private fun putDates(task: Task): String {
            var date = ""
            if (task.endDate == null || (task.endDate != null && task.startDate==task.endDate)) {
                if (task.startTime!=null ) {
                    date += formatTime(task.startTime)
                    if (task.endTime!= null && task.startTime!=task.endTime) {
                        date += " - " + formatTime(task.endTime)
                    }
                } else {
                    date += " " + context.resources.getString(R.string.taskAllDay)
                }
            } else {
                if (task.startTime!=null) {
                    date = formatTime(task.startTime) + " - " + formatDate(task.endDate) + ", " + formatTime(task.endTime)
                }
            }
            return date
        }

        private fun formatDate(date: Date): String {
            val pattern = "d MMMM yyyy"
            val simpleDateFormat = SimpleDateFormat(pattern)
            return simpleDateFormat.format(date)
        }

        private fun formatTime(date: Date): String {
            var res = ""
            val pattern = "dd/MM/yyyy HH:mm"
            val simpleDateFormat = SimpleDateFormat(pattern)
            res = simpleDateFormat.format(date)
            val index = res.indexOf(" ")+1
            res = res.substring(index)
            return res
        }

        override fun onClick(v: View?) {
            if (v!=null) {
                clickListener?.onItemClick(v, absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_item_layout, parent, false)
        return EventsViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        val task = events[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    fun setOnItemClickListener(clickListener: CalendarAdapter.ClickListener) {
        this.clickListener = clickListener
    }
}