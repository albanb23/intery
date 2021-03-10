package com.albaburdallo.intery.wallet

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.HomeActivity
import com.albaburdallo.intery.LoginActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.habit.HabitActivity
import com.albaburdallo.intery.task.TaskActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_chart.*
import kotlinx.android.synthetic.main.activity_options.*
import java.time.LocalDate

class ChartActivity : AppCompatActivity() {

    private lateinit var dateList: RecyclerView
    private lateinit var adapter: ChartAdapter
    private lateinit var mainViewModel: ChartViewModel
    private lateinit var layoutManager: LinearLayoutManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            R.layout.activity_chart)
        val date_list = findViewById<RecyclerView>(R.id.date_items)

        layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL

        mainViewModel = ViewModelProvider(this).get(ChartViewModel::class.java)
        date_list.adapter = ChartAdapter(mainViewModel.dates, this) { date ->
            mainViewModel.setSelectedDate(date)
            scrollToDate(date)
        }
        date_list.layoutManager = layoutManager
        initializeSelectedDate()

        decrement_date.setOnClickListener {
            mainViewModel.selectedDate.value?.let { date ->
                val previousDate = date.minusMonths(1)
                if (mainViewModel.dates.indexOf(date) >= 0) {
                    mainViewModel.setSelectedDate(previousDate)
                    scrollToDate(previousDate)
                }
            }
        }

        increment_date.setOnClickListener {
            mainViewModel.selectedDate.value?.let { date ->
                val nextDate = date.plusMonths(1)
                if (mainViewModel.dates.indexOf(date) >= 0) {
                    mainViewModel.setSelectedDate(nextDate)
                    scrollToDate(nextDate)
                }
            }
        }

        date_list.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    val offset = (date_list.width / date_list.width - 1) / 2
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition() + offset
                    if (position in 0 until mainViewModel.dates.size &&
                        mainViewModel.dates[position] != mainViewModel.selectedDate.value) {
                        when (position) {
                            0 -> {
                                mainViewModel.setSelectedDate(mainViewModel.dates[1])
                                scrollToDate(mainViewModel.dates[1])
                            }
                            mainViewModel.dates.size - 1 -> {
                                mainViewModel.setSelectedDate(mainViewModel.dates[position - 1])
                                scrollToDate(mainViewModel.dates[position - 1])
                            }
                            else -> mainViewModel.setSelectedDate(mainViewModel.dates[position])
                        }
                    }
                }
            }
        })

        setup()
    }

    private fun setup() {
        chartBackImageView.setOnClickListener { showWallet() }

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.tasks_item -> {
                    showTask()
                    true
                }
                R.id.wallet_item -> {
                    showWallet()
                    true
                }
                R.id.habits_item -> {
                    showHabit()
                    true
                }
                R.id.settings_item -> {
                    showHabit()
                    true
                }
                else -> {
                    false
                }
            }
        }

        logOutButton.setOnClickListener{
            //Borrado de datos
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            //onBackPressed() //para volver a la pantalla anterior
            showLogin()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeSelectedDate() {
        if (mainViewModel.selectedDate.value == null) {
            val now = mainViewModel.dates[mainViewModel.dates.size-2]
            mainViewModel.setSelectedDate(now)
            scrollToDate(now)
        }
    }

    private fun scrollToDate(date: LocalDate) {
        val date_list = findViewById<RecyclerView>(R.id.date_items)
        val date_item = findViewById<LinearLayout>(R.id.date_item)
        var width = date_list.width

        if (width>0){
            val dateWidth = date_item.width
            layoutManager.scrollToPositionWithOffset(mainViewModel.dates.indexOf(date), width/2-dateWidth/2)
        } else {
            val vto = date_list.viewTreeObserver
            vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    date_list.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    width = date_list.width
                    resources?.getDimensionPixelSize(R.dimen.date_item_width)?.let { dateWidth ->
//                        layoutManager.scrollToPositionWithOffset(mainViewModel.dates.indexOf(date), width / 2 - dateWidth / 2)
                        layoutManager.scrollToPosition(mainViewModel.dates.indexOf(date))
                    }
                }
            })
        }
    }

    private fun showLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java).apply { }
        startActivity(loginIntent)
    }

    private fun showWallet() {
        val walletIntent = Intent(this, WalletActivity::class.java).apply { }
        startActivity(walletIntent)
    }

    private fun showTask() {
        val taskIntent = Intent(this, TaskActivity::class.java).apply { }
        startActivity(taskIntent)
    }

    private fun showHabit() {
        val habitIntent = Intent(this, HabitActivity::class.java).apply { }
        startActivity(habitIntent)
    }

    private fun showHome() {
        val homeIntent = Intent(this, HomeActivity::class.java).apply { }
        startActivity(homeIntent)
    }
}