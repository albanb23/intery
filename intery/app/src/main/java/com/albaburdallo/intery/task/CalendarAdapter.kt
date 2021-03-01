package com.albaburdallo.intery.task

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.entities.Calendar
import com.albaburdallo.intery.model.entities.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.create_calendar.view.*
import java.util.HashMap

class CalendarAdapter(val calendars: MutableList<Calendar>) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    private var clickListener: ClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val db = FirebaseFirestore.getInstance()
        val context: Context = itemView.context

        private val calendarName = itemView.findViewById<View>(R.id.calendarNameList) as TextView
        private val calendarRemainingTasks = itemView.findViewById<View>(R.id.tasksRemaining) as TextView
        private val calendarDescription = itemView.findViewById<View>(R.id.calendarDescriptionList) as TextView
        private val colorPoint = itemView.findViewById<LinearLayout>(R.id.calendarColorPoint) as ImageView

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(calendar: Calendar, calendars: MutableList<Calendar>) {
            val unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_circle)
            val calendarDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
            DrawableCompat.setTint(calendarDrawable, Integer.parseInt(calendar.color))

            colorPoint.setImageDrawable(calendarDrawable);

            calendarName.text = calendar.name
            calendarDescription.text = calendar.description
            var remaining = hashSetOf<String>()
            db.collection("tasks").whereEqualTo("done", false).get().addOnSuccessListener { documents ->
                for(document in documents) {
                    val taskCalendar =  document.get("calendar") as HashMap<String, String>
                    if (taskCalendar["name"] == calendar.name) {
                        remaining.add(document.get("name") as String)
                    }
                }

                if (remaining.isNotEmpty()) {
                    calendarRemainingTasks.text = remaining.size.toString() + " " + context.getString(R.string.remainingTasks)
                } else {
                    calendarRemainingTasks.text = "0 " + context.getString(R.string.remainingTasks)
                }

            }
        }

        override fun onClick(v: View?) {
            if (v!=null){
                clickListener?.onItemClick(v, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarAdapter.ViewHolder, position: Int) {
        val calendar = calendars[position]
        holder.bind(calendar, calendars)
    }

    override fun getItemCount(): Int {
        return calendars.size
    }

    fun setOnItemClickListener(clickListener: CalendarAdapter.ClickListener) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onItemClick(v: View,position: Int)
    }

}