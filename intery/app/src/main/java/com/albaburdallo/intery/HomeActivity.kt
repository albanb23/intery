package com.albaburdallo.intery

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.albaburdallo.intery.habit.HabitActivity
import com.albaburdallo.intery.model.entities.Task
import com.albaburdallo.intery.model.entities.Transaction
import com.albaburdallo.intery.task.TaskActivity
import com.albaburdallo.intery.task.TaskAdapter
import com.albaburdallo.intery.task.TaskFormActivity
import com.albaburdallo.intery.wallet.WalletActivity
import com.albaburdallo.intery.wallet.WalletFormActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.taskFrame
import kotlinx.android.synthetic.main.activity_options.*
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.loading_layout.*
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var todayTasks: MutableList<Task>
    private lateinit var tomorrowTasks: MutableList<Task>
    private lateinit var nextDayTasks: MutableList<Task>
    private lateinit var adapter: TaskAdapter
    private lateinit var todayTaskList: RecyclerView
    private lateinit var tomorrowTaskList: RecyclerView
    private lateinit var nextDayTaskList: RecyclerView

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        this.getSupportActionBar()?.hide()

        val bundle = intent.extras
        val email = bundle?.getString("email")

        loadingLottie.setAnimation(R.raw.loading)
        loadingLottie.playAnimation()
        loadingLottie.repeatCount = LottieDrawable.INFINITE

        setup()

        //Guardado de datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.apply()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setup() {
        val authEmail = FirebaseAuth.getInstance().currentUser?.email;

        floatingCreateTask.setOnClickListener { showTaskForm(null, "create") }
        floatingCreateTransaction.setOnClickListener { showWalletForm(null, "create") }

        db.collection("common").document(authEmail ?: "").addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value!!.get("money")!=null && value.get("currency")!=null) {
                moneyHomeTextView.text = value.get("money") as String
                currencyHomeTextView.text = value.get("currency") as String
            }
            loadingLayout.visibility = View.GONE
        }

        todayTasks = arrayListOf()
        tomorrowTasks = arrayListOf()
        nextDayTasks = arrayListOf()
        todayTaskList = findViewById(R.id.todayTaskList)
        tomorrowTaskList = findViewById(R.id.tomorrowTaskList)
        nextDayTaskList = findViewById(R.id.nextDayTaskList)

        val pattern = "EEEE, dd MMMM"
        val simpleDateFormat = SimpleDateFormat(pattern,  this.resources?.configuration?.locales?.get(0))
        val cal = Calendar.getInstance()
        val today = simpleDateFormat.format(cal.time)
        cal.add(Calendar.DATE, 1)
        val tomorrow = simpleDateFormat.format(cal.time)
        cal.add(Calendar.DATE, 1)
        val nextDay = simpleDateFormat.format(cal.time)
        todayTextView.text = today
        tomorrowTextView.text = tomorrow
        nextDayTextView.text = nextDay

        var query = db.collection("tasks").whereEqualTo("done", false)
        query.orderBy("created", Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            for (document in value!!) {
                val user = document.get("user") as HashMap<String, String>
                if (user["email"] == authEmail) {
                    val name = document.get("name") as String
                    val startDate = document.get("startDate") as Timestamp
                    val endDate = document.get("endDate") as? Timestamp
                    val startTime = document.get("startTime") as? Timestamp
                    val endTime = document.get("endTime") as? Timestamp
                    val allday = document.get("allDay") as Boolean
                    val notifyme = document.get("notifyMe") as Boolean
                    val notes = document.get("notes") as String
                    val done = document.get("done") as Boolean
                    val id = document.get("id") as String
                    val calendar = document.get("calendar") as HashMap<String, String>
                    val task: Task = if (endDate != null && startTime != null && endTime != null) {
                        Task(
                            id,
                            name,
                            startDate.toDate(),
                            startTime.toDate(),
                            endDate.toDate(),
                            endTime.toDate(),
                            allday,
                            notifyme,
                            notes,
                            done,
                            com.albaburdallo.intery.model.entities.Calendar(
                                calendar["id"],
                                calendar["name"],
                                calendar["description"],
                                calendar["color"]
                            )
                        )
                    } else {
                        Task(
                            id, name, startDate.toDate(), allday, notifyme, notes, done,
                            com.albaburdallo.intery.model.entities.Calendar(
                                calendar["id"],
                                calendar["name"],
                                calendar["description"],
                                calendar["color"]
                            )
                        )
                    }
                    if (simpleDateFormat.format(task.startDate.time) == today) {
                        todayTasks.add(task)
                    } else if (simpleDateFormat.format(task.startDate.time) == tomorrow) {
                        tomorrowTasks.add(task)
                    } else if (simpleDateFormat.format(task.startDate.time) == nextDay) {
                        nextDayTasks.add(task)
                    }
                }
            }

            //today
            todayTaskList.layoutManager = LinearLayoutManager(this)
            adapter = if (todayTasks.size>3) {
                TaskAdapter(todayTasks.subList(0, 3))
            } else {
                TaskAdapter(todayTasks)
            }
            todayTaskList.adapter = adapter
            todayTaskList.isEnabled = false
            if (todayTasks.isEmpty()) {
                noTasksForTodayTextView.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            } else {
                noTasksForTodayTextView.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }

            //tomorrow
            tomorrowTaskList.layoutManager = LinearLayoutManager(this)
            adapter = if (tomorrowTasks.size>2) {
                TaskAdapter(tomorrowTasks.subList(0, 2))
            } else {
                TaskAdapter(tomorrowTasks)
            }
            tomorrowTaskList.adapter = adapter
            tomorrowTaskList.isEnabled = false
            if (tomorrowTasks.isEmpty()) {
                noTasksForTomorrowTextView.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            } else {
                noTasksForTomorrowTextView.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }

            //nextday
            nextDayTaskList.layoutManager = LinearLayoutManager(this)
            adapter = if(nextDayTasks.size>1) {
                TaskAdapter(nextDayTasks.subList(0, 1))
            } else {
                TaskAdapter(nextDayTasks)
            }
            nextDayTaskList.adapter = adapter
            nextDayTaskList.isEnabled = false
            if (nextDayTasks.isEmpty()) {
                noTasksForNextDayTextView.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            } else {
                noTasksForNextDayTextView.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }
            loadingLayout.visibility = View.GONE
        }

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.tasks_item -> {
                    showTask()
                    val prefs = getSharedPreferences(
                        getString(R.string.prefs_file),
                        Context.MODE_PRIVATE
                    ).edit()
                    prefs.putString("calendar", null)
                    prefs.apply()
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

        optionsImage.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        walletLayout.setOnClickListener {
            showWallet()
        }

        taskFrame.setOnClickListener {
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.putString("calendar", null)
            prefs.apply()
            showTask()
        }

        habitLayout.setOnClickListener {
            showHabit()
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

    private fun showTaskForm(task: Task?, form: String) {
        val taskFormIntent = Intent(this, TaskFormActivity::class.java)
        if (task != null) {
            taskFormIntent.putExtra("taskid", task.id.toString())
        }
        taskFormIntent.putExtra("form", form)
        startActivity(taskFormIntent)
    }

    private fun showWalletForm(transaction: Transaction?, form: String) {
        val walletFormIntent = Intent(this, WalletFormActivity::class.java)
        if (transaction != null) {
            walletFormIntent.putExtra("transactionId", transaction.id.toString())
        }
        walletFormIntent.putExtra("form", form)
        startActivity(walletFormIntent)
    }




}