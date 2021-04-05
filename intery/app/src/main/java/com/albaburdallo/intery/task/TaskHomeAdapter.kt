package com.albaburdallo.intery.task

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.util.entities.Task
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TaskHomeAdapter(val tasks: MutableList<Task>): RecyclerView.Adapter<TaskHomeAdapter.ViewHolder>() {

    private var clickListener: ClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val db = FirebaseFirestore.getInstance()
        val context: Context = itemView.context
        val prefs = context.getSharedPreferences(
            context.getString(R.string.prefs_file),
            Context.MODE_PRIVATE
        )
        private val showAll = prefs.getBoolean("showAll", false)

        private val taskName = itemView.findViewById<TextView>(R.id.taskNameListHome)
        private val taskDate = itemView.findViewById<TextView>(R.id.dateTaskListHome)
        private val caldraw = itemView.findViewById<TextView>(R.id.calendarDrawHome)
        val task: Task? = null


        fun bind(task: Task, tasks: MutableList<Task>) {
            taskName.text = task.name
            val unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.calendar)
            val calendarDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
            DrawableCompat.setTint(calendarDrawable, Integer.parseInt(task.calendar.color))
            caldraw.background = calendarDrawable
            if (task.calendar.name.length > 10) {
                (task.calendar.name.substring(0, 10) + "...").also { caldraw.text = it }
            } else {
                caldraw.text = task.calendar.name
            }
            taskDate.text = putDates(task)
        }

        private fun putDates(task: Task): String {
            var date = ""
            if (task.endDate == null || (task.endDate != null && task.startDate==task.endDate)) {
                date += formatDate(task.startDate)
                if (task.startTime!=null ) {
                    date += " " + formatTime(task.startTime)
                    if (task.endTime!= null && task.startTime!=task.endTime) {
                        date += " - " + formatTime(task.endTime)
                    }
                } else {
                    date += " " + context.resources.getString(R.string.taskAllDay)
                }
            } else {
                if (task.startTime!=null) {
                    date += formatDate(task.startDate) + " " + formatTime(task.startTime) + " - " + formatDate(
                        task.endDate
                    ) + " " + formatTime(task.endTime)
                }
            }
            return date
        }

        private fun formatDate(date: Date): String {
            val pattern = "dd/MM/yyyy"
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

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHomeAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskHomeAdapter.ViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task, tasks)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun setOnItemClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onItemClick(v: View,position: Int)
    }

}