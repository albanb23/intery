package com.albaburdallo.intery.task

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.util.entities.Task
import kotlinx.android.synthetic.main.calendar_day_layout.view.*
import java.time.LocalDate

class EventsAdapter(): RecyclerView.Adapter<EventsAdapter.EventsViewHolder>() {

    val events: MutableList<Task> = mutableListOf()

    inner class EventsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        init {

        }

        fun bind(task: Task) {
            itemView.calendarDayText.gravity = Gravity.START
            itemView.calendarDayText.text = task.name
            itemView.calendarDayNotesText.visibility = View.VISIBLE
            itemView.calendarDayNotesText.text = task.notes
            itemView.calendarDot1.visibility = View.INVISIBLE
            itemView.calendarDot2.visibility = View.INVISIBLE
            itemView.calendarDot3.visibility = View.INVISIBLE
            itemView.calendarPlusText.visibility = View.INVISIBLE

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_day_layout, parent, false)
        return EventsViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        val task = events[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int {
        return events.size
    }
}