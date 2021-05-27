package com.albaburdallo.intery

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.habit.HabitActivity
import com.albaburdallo.intery.habit.HabitHomeAdapter
import com.albaburdallo.intery.task.TaskActivity
import com.albaburdallo.intery.task.TaskFormActivity
import com.albaburdallo.intery.task.TaskHomeAdapter
import com.albaburdallo.intery.util.entities.Habit
import com.albaburdallo.intery.util.entities.Task
import com.albaburdallo.intery.util.entities.Transaction
import com.albaburdallo.intery.wallet.WalletActivity
import com.albaburdallo.intery.wallet.WalletFormActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_habit.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.taskFrame
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.habit_list_home.*
import kotlinx.android.synthetic.main.loading_layout.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.options.*
import kotlinx.android.synthetic.main.task_list.*
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : BaseActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var todayTasks: MutableList<Task>
    private lateinit var tomorrowTasks: MutableList<Task>
    private lateinit var nextDayTasks: MutableList<Task>
    private lateinit var adapter: TaskHomeAdapter
    private lateinit var todayTaskList: RecyclerView
    private lateinit var tomorrowTaskList: RecyclerView
    private lateinit var nextDayTaskList: RecyclerView
    private lateinit var habits: MutableList<Habit>
    private lateinit var habitAdapter: HabitHomeAdapter
    private lateinit var habitList: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bundle = intent.extras
        val email = bundle?.getString("email")

        setup()

        //Guardado de datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.apply()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val dialogShown = prefs.getBoolean("homeDialog", false)
        val lang = prefs.getString("language", "")

        if (!dialogShown) {

            // creamos la vista de popup
            val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.home_popup, null)
            val popup = PopupWindow(
                view,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            popup.elevation = 10.0F
            //animacion del popup
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.LEFT
            popup.enterTransition = slideIn

            val slideOut = Slide()
            slideOut.slideEdge = Gravity.RIGHT
            popup.exitTransition = slideOut

            //idioma
            val homePopUpEsp = view.findViewById<ImageView>(R.id.home_popup_esp)
            val homePopUpEng = view.findViewById<ImageView>(R.id.home_popup_eng)
            if (lang!="" && lang=="es") {
                homePopUpEsp.visibility = View.VISIBLE
                homePopUpEng.visibility = View.GONE
            } else {
                homePopUpEng.visibility = View.VISIBLE
                homePopUpEsp.visibility = View.GONE
            }

            //boton para cerrar el popup
            val homeClosePopup = view.findViewById<TextView>(R.id.homeClose)
            homeClosePopup.setOnClickListener {
                popup.dismiss()
            }

            //enseñamos el popup en la app
            Handler().postDelayed(Runnable {
                TransitionManager.beginDelayedTransition(drawerLayout)
                popup.showAtLocation(
                    findViewById(R.id.drawerLayout), // Location to display popup window
                    Gravity.CENTER, // Exact position of layout to display popup
                    0, // X offset
                    0 // Y offset
                )
            }, 100)


            val editor = prefs.edit()
            editor.putBoolean("homeDialog", true)
            editor.apply()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setup() {
        val authEmail = FirebaseAuth.getInstance().currentUser?.email

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
        }

        todayTasks = arrayListOf()
        tomorrowTasks = arrayListOf()
        nextDayTasks = arrayListOf()
        todayTaskList = findViewById(R.id.todayTaskList)
        tomorrowTaskList = findViewById(R.id.tomorrowTaskList)
        nextDayTaskList = findViewById(R.id.nextDayTaskList)

        val pattern = "EEEE, dd MMMM"
        val simpleDateFormat = SimpleDateFormat(
            pattern, this.resources?.configuration?.locales?.get(
                0
            )
        )
        val cal = Calendar.getInstance()
        val today = simpleDateFormat.format(cal.time)
        cal.add(Calendar.DATE, 1)
        val tomorrow = simpleDateFormat.format(cal.time)
        cal.add(Calendar.DATE, 1)
        val nextDay = simpleDateFormat.format(cal.time)
        todayTextView.text = today.substring(0,1).toUpperCase() + today.substring(1)
        tomorrowTextView.text = tomorrow.substring(0,1).toUpperCase() + tomorrow.substring(1)
        nextDayTextView.text = nextDay.substring(0,1).toUpperCase() + nextDay.substring(1)

        val query = db.collection("tasks").whereEqualTo("done", false)
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
                    val whenNotification  = document.get("when") as String
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
                            com.albaburdallo.intery.util.entities.Calendar(
                                calendar["id"],
                                calendar["name"],
                                calendar["description"],
                                calendar["color"]
                            ),
                            whenNotification
                        )
                    } else {
                        Task(
                            id, name, startDate.toDate(), allday, notifyme, notes, done,
                            com.albaburdallo.intery.util.entities.Calendar(
                                calendar["id"],
                                calendar["name"],
                                calendar["description"],
                                calendar["color"]
                            ),
                            whenNotification
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
                TaskHomeAdapter(todayTasks.subList(0, 3))
            } else {
                TaskHomeAdapter(todayTasks)
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
                TaskHomeAdapter(tomorrowTasks.subList(0, 2))
            } else {
                TaskHomeAdapter(tomorrowTasks)
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
                TaskHomeAdapter(nextDayTasks.subList(0, 1))
            } else {
                TaskHomeAdapter(nextDayTasks)
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
        }

        db.clearPersistence()
        habitList = findViewById(R.id.habitListView)
        habits = arrayListOf()
        db.collection("habits").whereEqualTo("user.email", authEmail).addSnapshotListener { value, error ->
            if (error!=null) {
                return@addSnapshotListener
            }

            habits.clear()
            for(document in value!!) {
                val id = document.get("id") as String
                val name = document.get("name") as String
                val notes = document.get("notes") as String
                val startDate = document.get("startDate") as Timestamp
                val color = document.get("color") as String
                val notifyMe = document.get("notifyMe") as Boolean
                val whenHabit = document.get("when") as? Timestamp
                val period = document.get("period") as Long
                val times = document.get("times") as Long
                val progress = document.get("progress") as Double
                val updated = document.get("updated") as Timestamp
                val days = document.get("daysCompleted") as String
                if (whenHabit!=null) {
                    habits.add(Habit(id, name, notes, startDate.toDate(), color, notifyMe, whenHabit.toDate(), period.toInt(), times.toInt(), progress, updated.toDate(), days))
                } else {
                    habits.add(Habit(id, name, notes, startDate.toDate(), color, notifyMe, period.toInt(), times.toInt(), progress, updated.toDate(), days))
                }
            }

            val habitsList = if (habits.isEmpty()) {
                arrayListOf()
            } else if (habits.size >=3) {
                habits.subList(0,3)
            } else {
                habits
            }
            layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            habitList.layoutManager = layoutManager
            habitAdapter = HabitHomeAdapter(habitsList)
            habitList.adapter = habitAdapter
            if (habits.isEmpty()) {
                noHabits.visibility = View.VISIBLE
            } else {
                noHabits.visibility = View.GONE
            }
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
                    showSettings()
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
//            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
//            prefs.clear()
//            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            //onBackPressed() //para volver a la pantalla anterior
            showLogin()
        }

        db.collection("users").document(authEmail!!).get().addOnSuccessListener {
            var photo = it.get("photo") as String
            if (photo == "") {
                Picasso.get()
                    .load("https://global-uploads.webflow.com/5bcb46130508ef456a7b2930/5f4c375c17865e08a63421ac_drawkit-og.png")
                    .transform(CropCircleTransformation()).into(profilePicImage)
            } else {
                Picasso.get().load(photo).transform(CropCircleTransformation()).into(profilePicImage)
            }

        }

        val header = nav_view.getHeaderView(0)
        val profilePicImage = header.findViewById<ImageView>(R.id.profilePicImage)
        profilePicImage.setOnClickListener {
            showProfile(authEmail)
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

    private fun showSettings() {
        val settingsIntent = Intent(this, SettingsActivity::class.java).apply { }
        startActivity(settingsIntent)
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

    private fun showProfile(email: String) {
        val profileIntent = Intent(this, ProfileActivity::class.java)
        if (email != null) {
            profileIntent.putExtra("userEmail", email)
        }
        startActivity(profileIntent)
    }


}