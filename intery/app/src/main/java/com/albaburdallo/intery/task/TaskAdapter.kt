package com.albaburdallo.intery.task

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.entities.Task
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(val tasks: MutableList<Task>): RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    private var clickListener: ClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val db = FirebaseFirestore.getInstance()
        val context: Context = itemView.context
        val prefs = context.getSharedPreferences(
            context.getString(R.string.prefs_file),
            Context.MODE_PRIVATE
        )
        private val showAll = prefs.getBoolean("showAll", false)

        private val taskName = itemView.findViewById<View>(R.id.taskNameList) as TextView
        private val taskDate = itemView.findViewById<View>(R.id.dateTaskList) as TextView
        private val radioButton = itemView.findViewById<CheckBox>(R.id.taskRadioButton)
        private val caldraw = itemView.findViewById<View>(R.id.calendarDraw) as TextView
        val task: Task? = null

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(task: Task, tasks: MutableList<Task>) {
            taskName.text = task.name
            val unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.calendar)
            val calendarDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
            DrawableCompat.setTint(calendarDrawable, Integer.parseInt(task.calendar.color))
            caldraw.text = task.calendar.name
            caldraw.background = calendarDrawable

            radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
                task.isDone = isChecked
                db.collection("tasks").document(task.id.toString()).update("done", task.isDone)
                checkWhenDone(isChecked, taskName, tasks, task)
                if (!showAll) {
                    if (isChecked) {
                        itemView.visibility = View.GONE
                        val params = itemView.layoutParams
                        params.height = 0
                        params.width = 0
                        itemView.layoutParams = params
                    } else {
                        itemView.visibility = View.VISIBLE
                    }
                }
            }
            taskDate.text = putDates(task)
            radioButton.isChecked = task.isDone
            checkWhenDone(radioButton.isChecked, taskName, tasks, task)
        }

        private fun checkWhenDone(
            isChecked: Boolean,
            taskName: TextView,
            tasks: MutableList<Task>,
            task: Task
        ) {
            if (isChecked) {
                taskName.paintFlags = taskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                taskName.paintFlags = taskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
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

        override fun onClick(v: View?) {
            if (v!=null){
                clickListener?.onItemClick(v, adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskAdapter.ViewHolder, position: Int) {
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