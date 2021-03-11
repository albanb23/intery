package com.albaburdallo.intery.wallet

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

class ChartAdapter(private val dates: List<LocalDate>, private val context: Context?,
private val onClick: ((LocalDate) -> Unit)?) : RecyclerView.Adapter<ChartAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val monthLabel = itemView.findViewById<TextView>(R.id.monthLabel)
        val yearLabel = itemView.findViewById<TextView>(R.id.yearLabel)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(date: LocalDate, position: Int) {
            if (position in 1 until (itemCount - 1)) {
                val month = date.month.getDisplayName(TextStyle.FULL,
                    context?.resources?.configuration?.locales?.get(0)
                )
                val year = date.year.toString()
                monthLabel.text = month
                yearLabel.text = year
                itemView.setOnClickListener {
                    onClick?.invoke(dates[position])
                }
            } else {
                monthLabel.text = ""
                yearLabel.text = ""
                monthLabel.contentDescription = ""
                yearLabel.contentDescription = ""
                itemView.setOnClickListener {  }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.horizontal_picker_item, parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = dates[position]
        holder.bind(date, position)
    }

    override fun getItemCount(): Int {
        return dates.size
    }

}