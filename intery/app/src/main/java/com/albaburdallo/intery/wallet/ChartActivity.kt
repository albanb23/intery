package com.albaburdallo.intery.wallet

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.LoginActivity
import com.albaburdallo.intery.ProfileActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.habit.HabitActivity
import com.albaburdallo.intery.task.TaskActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_chart.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.options.*
import java.sql.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.math.roundToInt

class ChartActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email;

    private lateinit var mainViewModel: ChartViewModel
    private lateinit var layoutManager: LinearLayoutManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            R.layout.activity_chart
        )
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
            graph()
        }

        increment_date.setOnClickListener {
            mainViewModel.selectedDate.value?.let { date ->
                val nextDate = date.plusMonths(1)
                if (mainViewModel.dates.indexOf(date) >= 0) {
                    mainViewModel.setSelectedDate(nextDate)
                    scrollToDate(nextDate)
                }
            }
            graph()
        }

        date_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    val offset = (date_list.width / date_list.width - 1) / 2
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition() + offset
                    if (position in 0 until mainViewModel.dates.size &&
                        mainViewModel.dates[position] != mainViewModel.selectedDate.value
                    ) {
                        when (position) {
                            0 -> {
                                mainViewModel.setSelectedDate(mainViewModel.dates[1])
                                scrollToDate(mainViewModel.dates[1])
                            }
                            mainViewModel.dates.size - 1 -> {
                                mainViewModel.setSelectedDate(mainViewModel.dates[position - 1])
                                scrollToDate(mainViewModel.dates[position - 1])
                            }
                            else -> {
                                mainViewModel.setSelectedDate(mainViewModel.dates[position])
                            }
                        }
                    }
                }
                graph()
            }
        })

        setup()
        graph()
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

        db.collection("users").document(authEmail!!).get().addOnSuccessListener {
            var photo = it.get("photo") as String
            if (photo == "") {
                photo = ""
            } else {
                Picasso.get().load(photo).transform(CropCircleTransformation()).into(profilePicImage)
            }
        }

        val header = nav_view.getHeaderView(0)
        val profilePicImage = header.findViewById<ImageView>(R.id.profilePicImage)
        profilePicImage.setOnClickListener {
            if (authEmail != null) {
                showProfile(authEmail)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun graph(){
        db.collection("wallet").whereEqualTo("expenditure", true).addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            val barchart = findViewById<BarChart>(R.id.barChart)
            var entries = arrayListOf<BarEntry>()
            val currDate = mainViewModel.selectedDate.value

            var moneyExp = 0.0
            var moneyInc = 0.0
            for (document in value!!) {
                val user = document.get("user") as HashMap<*, *>
                val date = (document.get("date") as com.google.firebase.Timestamp).toDate()
                val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                if (user["email"]==authEmail && (localDate.monthValue==currDate?.monthValue && localDate.year== currDate.year)) {
                    moneyExp += document.get("money") as Double
                }
            }

            db.collection("wallet").whereEqualTo("income", true).addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                for (document in value!!) {
                    val user = document.get("user") as HashMap<*, *>
                    val date = (document.get("date") as com.google.firebase.Timestamp).toDate()
                    val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    if (user["email"]==authEmail && (localDate.monthValue==currDate?.monthValue && localDate.year== currDate.year)) {
                        moneyInc+=document.get("money") as Double
                    }
                }
                val expBarEntry = BarEntry(moneyExp.toFloat(), 0)
                val incBarEntry = BarEntry(moneyInc.toFloat(), 1)
                entries.add(expBarEntry)
                entries.add(incBarEntry)
                val barDataSet = BarDataSet(entries, "")
                val colors: MutableList<Int> = arrayListOf(ContextCompat.getColor(this,R.color.red),
                    ContextCompat.getColor(this, R.color.green))
                barDataSet.colors = colors
                val labels = arrayListOf<String>()
                labels.add("")
                labels.add("")
                val data = BarData(labels, barDataSet)
                barchart.data = data
                barchart.setDescription("")
                barchart.axisLeft.setDrawGridLines(false)
                barchart.axisRight.setDrawGridLines(false)
                barchart.axisLeft.setDrawLabels(false)
                barchart.axisRight.setDrawLabels(false)
                barchart.xAxis.isEnabled = false
                barchart.axisLeft.isEnabled = false
                barchart.axisRight.isEnabled = false
                barchart.legend.isEnabled = false
                barchart.xAxis.setDrawLabels(false)
                var max = 0.0
                var totalSav = 0.0
                if (moneyExp>moneyInc) {
                    max = moneyExp + 10.0
                    totalSavings.visibility = View.GONE
                    totalNoSavings.visibility = View.VISIBLE
                    totalSav = moneyExp - moneyInc
                } else {
                    max = moneyInc + 10.0
                    totalNoSavings.visibility = View.GONE
                    totalSavings.visibility = View.VISIBLE
                    totalSav = moneyInc - moneyExp
                }
                barchart.axisLeft.setAxisMinValue(0f)
                barchart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                barDataSet.setDrawValues(false)
                barchart.fitScreen()

                barchart.animateY(5000)

                db.collection("common").document(authEmail?:"").addSnapshotListener { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    val curr = value!!.get("currency") as String
                    (" " + curr + " " + ((totalSav * 100.0).roundToInt() / 100.0)).also { moneySavings.text = it }
                    ("$curr $moneyExp").also { totalSpentMoney.text = it }
                    ("$curr $moneyInc").also { totalReceivedMoney.text = it }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeSelectedDate() {
        if (mainViewModel.selectedDate.value == null) {
            val now = mainViewModel.dates[mainViewModel.dates.size - 2]
            mainViewModel.setSelectedDate(now)
            scrollToDate(now)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scrollToDate(date: LocalDate) {
        val date_list = findViewById<RecyclerView>(R.id.date_items)
        val date_item = findViewById<LinearLayout>(R.id.date_item)
        var width = date_list.width

        if (width>0){
            val dateWidth = date_item.width
            layoutManager.scrollToPositionWithOffset(
                mainViewModel.dates.indexOf(date),
                width / 2 - dateWidth / 2
            )
        } else {
            val vto = date_list.viewTreeObserver
            vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    date_list.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    width = date_list.width
                    resources?.getDimensionPixelSize(R.dimen.date_item_width)?.let { dateWidth ->
                        layoutManager.scrollToPositionWithOffset(mainViewModel.dates.indexOf(date), width / 2 - dateWidth / 2)
//                        layoutManager.scrollToPosition(mainViewModel.dates.indexOf(date))
                    }
                }
            })
        }
        graph()
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

    private fun showProfile(email: String) {
        val profileIntent = Intent(this, ProfileActivity::class.java)
        if (email != null) {
            profileIntent.putExtra("userEmail", email)
        }
        startActivity(profileIntent)
    }
}