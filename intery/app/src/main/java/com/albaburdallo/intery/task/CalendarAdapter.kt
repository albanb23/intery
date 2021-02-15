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
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.entities.Calendar
import com.albaburdallo.intery.model.entities.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.create_calendar.view.*
import java.util.HashMap

class CalendarAdapter(context: Context, val calendars: MutableList<Calendar>) : ArrayAdapter<Calendar?>(
    context,
    -1,
    calendars as List<Calendar?>
) {

    private val db = FirebaseFirestore.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.calendar_list, parent, false)
        }

        val calendarName = convertView!!.findViewById<View>(R.id.calendarNameList) as TextView
        val calendarRemainingTasks = convertView!!.findViewById<View>(R.id.tasksRemaining) as TextView
        val item = convertView!!.findViewById<LinearLayout>(R.id.calendarItem) as LinearLayout
        val colorPoint = convertView!!.findViewById<LinearLayout>(R.id.calendarColorPoint) as ImageView
        val calendar = calendars[position]

        val unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_circle)
        val calendarDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
        DrawableCompat.setTint(calendarDrawable, Integer.parseInt(calendar.color))

        colorPoint.setImageDrawable(calendarDrawable);

        calendarName.text = calendar.name
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

        return convertView
    }

}