package com.albaburdallo.intery.task

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.Task
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(context: Context, private val tasks: List<Task>) : ArrayAdapter<Task?>(context, -1, tasks) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.task_list, parent, false)
        }
        val taskName = convertView!!.findViewById<View>(R.id.taskNameList) as TextView
        val taskDate = convertView!!.findViewById<View>(R.id.dateTaskList) as TextView
        val task = tasks[position]
        if (task.isDone) {
            taskName.paintFlags = taskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG //si la tarea esta completada se tacha
        } else {
            taskName.paintFlags = taskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        taskName.text = task.name
        taskDate.text = asignarFechas(task)

        return convertView
    }

    private fun asignarFechas(task: Task): String {
        var date = ""
       if (task.endDate == null || (task.endDate != null && task.startDate==task.endDate)) {
           date += formatearFecha(task.startDate)
           if (task.startTime!=null ) {
               date += " " + formatearHora(task.startTime)
               if (task.endTime!= null && task.startTime!=task.endTime) {
                   date += " - " + formatearHora(task.endTime)
               }
           } else {
               date += " " + R.string.taskAllDay.toString()
           }
        } else {
           if (task.startTime!=null) {
               date += formatearFecha(task.startDate) + " " + formatearHora(task.startTime) + " - " + formatearFecha(task.endDate) + " " + formatearHora(task.endTime)
           }
        }


        return date
    }

    private fun formatearFecha(date: Date): String {
        val pattern = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(date)
    }

    private fun formatearHora(date: Date): String {
        var res = ""
        val pattern = "dd/MM/yyyy HH:mm"
        val simpleDateFormat = SimpleDateFormat(pattern)
        res = simpleDateFormat.format(date)
        val index = res.indexOf(" ")+1
        res = res.substring(index)
        return res
    }


}