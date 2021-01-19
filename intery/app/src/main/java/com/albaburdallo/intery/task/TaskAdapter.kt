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
//        taskDate.text = task.start.toString()
        return convertView
    }
}