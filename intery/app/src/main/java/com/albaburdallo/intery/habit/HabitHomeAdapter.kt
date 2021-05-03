package com.albaburdallo.intery.habit

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.util.entities.Habit

class HabitHomeAdapter(val habits: MutableList<Habit>): RecyclerView.Adapter<HabitHomeAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val context: Context = itemView.context
        private val habitname = itemView.findViewById<TextView>(R.id.homeHabitName)
        private val habitBar = itemView.findViewById<ProgressBar>(R.id.homeHabitProgress)

        @SuppressLint("ResourceAsColor")
        fun bind(habit: Habit) {
            if (habit.name.length > 10) {
                (habit.name.substring(0, 10) + "...").also { habitname.text = it }
            } else {
                habitname.text = habit.name
            }
            //progress bar
            habitBar.max = 100
            habitBar.progress = habit.progress.toInt()
            habitBar.progressTintList = ColorStateList.valueOf(habit.color.toInt())
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.habit_list_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val habit = habits[position]
        holder.bind(habit)
    }

    override fun getItemCount(): Int {
        return habits.size
    }

}