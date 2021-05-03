package com.albaburdallo.intery.habit

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
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
import kotlinx.android.synthetic.main.activity_habit_show.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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

        @SuppressLint("ResourceAsColor")
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(habit: Habit) {
            habitName.text = habit.name
            habitDate.text = formatDate(habit.startDate)
            //cambiar color de la progress bar

            //progress bar
            progressBar.max = 100
            progressBar.progress = habit.progress.toInt()
            progressBar.progressTintList = ColorStateList.valueOf(habit.color.toInt())
            progressBar.backgroundTintList = ColorStateList.valueOf(R.color.gray_back)
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