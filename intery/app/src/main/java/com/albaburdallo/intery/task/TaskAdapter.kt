package com.albaburdallo.intery.task

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.entities.Task
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


class TaskAdapter(context: Context, val tasks: MutableList<Task>) : ArrayAdapter<Task?>(
    context,
    -1,
    tasks as List<Task?>
) {

    private val db = FirebaseFirestore.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.task_list, parent, false)
        }
        val prefs = context.getSharedPreferences(context.getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val showAll = prefs.getBoolean("showAll", false)

        val taskName = convertView!!.findViewById<View>(R.id.taskNameList) as TextView
        val taskDate = convertView!!.findViewById<View>(R.id.dateTaskList) as TextView
        val radioButton = convertView!!.findViewById<CheckBox>(R.id.taskRadioButton)
        val task = tasks[position]
        taskName.text = task.name
        radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            task.isDone = isChecked
            db.collection("tasks").document(task.id.toString()).update("done", task.isDone)
            checkWhenDone(isChecked, taskName)
            if (!showAll) {
                if (isChecked) {
                    tasks.remove(task)
                    notifyDataSetChanged()
                } else {
                    tasks.add(task)
                    notifyDataSetChanged()
                }
            }
        }
        taskDate.text = putDates(task)
        radioButton.isChecked = task.isDone
        checkWhenDone(radioButton.isChecked, taskName)

        return convertView
    }

    private fun checkWhenDone(isChecked: Boolean, taskName: TextView) {
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


}