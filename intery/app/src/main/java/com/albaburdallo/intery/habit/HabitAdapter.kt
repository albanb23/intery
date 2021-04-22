package com.albaburdallo.intery.habit

import android.content.Context
import android.os.Build
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.task.CalendarAdapter
import com.albaburdallo.intery.util.entities.Habit
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HabitAdapter(private val habits: MutableList<Habit>): RecyclerView.Adapter<HabitAdapter.ViewHolder>() {

    private var clickListener: ClickListener? = null

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{
        private val db = FirebaseFirestore.getInstance()
        val context: Context = itemView.context

        private val habitName = itemView.findViewById<TextView>(R.id.habitNameTextView)
        private val habitDate = itemView.findViewById<TextView>(R.id.startDateTextView)
        private val progressBar = itemView.findViewById<ProgressBar>(R.id.habitProgressBar)

        init {
            itemView.setOnClickListener(this)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(habit: Habit) {
            habitName.text = habit.name
            habitDate.text = formatDate(habit.startDate)
            //cambiar color de la progress bar

            //progress bar
            val handler = Handler()
            var start = habit.startDate.time.toInt() //0%
            val end = habit.endDate.time.toInt() // 100%
            println("======start=======" + start)
            println("======end=======" + end)

            progressBar.min = start
            progressBar.max = end

            start = progressBar.progress
            Thread {
                while (start < end) {
                    handler.post(Runnable {
                        progressBar.progress = start
                        println("porgress====" + start.toString() + "/" + progressBar.max)
                    })
                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }.start()

        }

        override fun onClick(v: View?) {
            if (v!=null){
                clickListener?.onItemClick(v, absoluteAdapterPosition)
            }
        }

        private fun formatDate(date: Date): String {
            val pattern = "d MMMM yyyy"
            val simpleDateFormat = SimpleDateFormat(pattern)
            return simpleDateFormat.format(date)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.habit_list, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: HabitAdapter.ViewHolder, position: Int) {
        val habit = habits[position]
        holder.bind(habit)
    }

    override fun getItemCount(): Int {
        return habits.size
    }

    fun setOnItemClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onItemClick(v: View,position: Int)
    }


}