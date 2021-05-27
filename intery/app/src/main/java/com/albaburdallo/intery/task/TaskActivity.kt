package com.albaburdallo.intery.task

import android.app.AlertDialog
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
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.*
import com.albaburdallo.intery.habit.HabitActivity
import com.albaburdallo.intery.util.entities.Task
import com.albaburdallo.intery.wallet.WalletActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_habit.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.options.*
import kotlinx.android.synthetic.main.task_popup.*
import java.util.*
import kotlin.collections.HashMap


open class TaskActivity : BaseActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email
    private lateinit var taskList: RecyclerView
    private lateinit var createTaskButton: Button
    private lateinit var adapter: TaskAdapter
    private lateinit var tasks: MutableList<Task>
    private var showAll = false
    private var calendarId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val dialogShown = prefs.getBoolean("taskDialog", false)
        val lang = prefs.getString("language", "")

        if (!dialogShown) {

            // creamos la vista de popup
            val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.task_popup, null)
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
            val taskPopUpEsp = view.findViewById<ImageView>(R.id.tasks_popup_esp)
            val taskPopUpEng = view.findViewById<ImageView>(R.id.tasks_popup_eng)
            if (lang!="" && lang=="es") {
                taskPopUpEsp.visibility = View.VISIBLE
                taskPopUpEng.visibility = View.GONE
            } else {
                taskPopUpEng.visibility = View.VISIBLE
                taskPopUpEsp.visibility = View.GONE
            }

            //boton para cerrar el popup
            val taskClosePopup = view.findViewById<TextView>(R.id.taskClose)
            taskClosePopup.setOnClickListener {
                popup.dismiss()
            }

            //enseñamos el popup en la app
            Handler().postDelayed(Runnable {
                TransitionManager.beginDelayedTransition(taskFrame)
                popup.showAtLocation(
                    findViewById(R.id.taskFrame), // Location to display popup window
                    Gravity.CENTER, // Exact position of layout to display popup
                    0, // X offset
                    0 // Y offset
                )
            }, 100)


            val editor = prefs.edit()
            editor.putBoolean("taskDialog", true)
            editor.apply()
        }
    }

    override fun onStart() {
        super.onStart()
        //Guardado de datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val prefsEdit = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()

        tasks = arrayListOf()

        calendarId = prefs.getString("calendar", "").toString()
        if (calendarId != "") {
            showAll = true
            eyeClosedIcon.visibility = View.GONE
            eyeOpenIcon.visibility = View.GONE
            calendarIcon.visibility = View.GONE
            bookmarkIcon.visibility = View.GONE

            val query = db.collection("calendars")

            query.document(calendarId).addSnapshotListener(this) { value, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                if (value!!.get("def")==null || value.get("def")=="" || (!(value.get("def") as Boolean))) {
                    trashCalendarImageView.visibility = View.VISIBLE
                }
            }

            trashCalendarImageView.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(this.resources.getString(R.string.deleteCalendar))
                    .setCancelable(false)
                    .setPositiveButton(this.resources.getString(R.string.yes)) { dialog, id ->
                        //se borran todas las tareas del calendario
                        db.collection("tasks").addSnapshotListener(this) { value, error ->
                            if (error != null) {
                                return@addSnapshotListener
                            }
                            for(document in value!!) {
                                val cal = document.get("calendar") as HashMap<*, *>
                                if (cal["id"] == calendarId) {
                                    db.collection("tasks").document(document.id).delete()
                                }
                            }
                        }
                        //se borra el calendario
                        query.document(calendarId).delete()
                        prefsEdit.putString("calendar", null)
                        prefsEdit.apply()
                        showCalendar()
                    }
                    .setNegativeButton(this.resources.getString(R.string.no)) { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
        }

        calendarIcon.setOnClickListener { showCalendarView() }

        showAll = prefs.getBoolean("showAll", false)

        taskList = findViewById(R.id.taskList)
        createTaskButton = findViewById(R.id.createTaskButton)
//        tasks = arrayListOf()

        if (calendarId=="") {
            if (showAll) {
                eyeClosedIcon.visibility = View.GONE
                eyeOpenIcon.visibility = View.VISIBLE
                trashCalendarImageView.visibility = View.GONE
            } else {
                eyeClosedIcon.visibility = View.VISIBLE
                eyeOpenIcon.visibility = View.GONE
                trashCalendarImageView.visibility = View.GONE
            }
        }

        eyeClosedIcon.setOnClickListener {
            prefsEdit.putBoolean("showAll", true)
            prefsEdit.apply()
            restartView()
        }

        eyeOpenIcon.setOnClickListener {
            prefsEdit.putBoolean("showAll", false)
            prefsEdit.apply()
            restartView()
        }

        var taskcoll: Query = if (showAll || calendarId!="") {
            db.collection("tasks").orderBy("done", Query.Direction.ASCENDING)
        } else {
            db.collection("tasks").whereEqualTo("done", false)
        }

        taskcoll = taskcoll.orderBy("created", Query.Direction.DESCENDING)
        taskcoll.whereEqualTo("user.email", authEmail).addSnapshotListener(this) { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            for (document in value!!) {
                val name = document.get("name") as String
                val startDate = document.get("startDate") as Timestamp
                val endDate = document.get("endDate") as? Timestamp
                val startTime = document.get("startTime") as? Timestamp
                val endTime = document.get("endTime") as? Timestamp
                val allday = document.get("allDay") as Boolean
                val notifyme = document.get("notifyMe") as Boolean
                val notes = document.get("notes") as String
                val done = document.get("done") as Boolean
                val id  = document.get("id") as String
                val whenNotification  = document.get("when") as String
                val calendar = document.get("calendar") as HashMap<String, String>
                if (endDate != null && startTime != null && endTime != null) {
                    tasks.add(
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
                    )
                } else {
                    tasks.add(
                        Task(
                            id,
                            name,
                            startDate.toDate(),
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
                    )
                }
            }

            taskList.layoutManager = LinearLayoutManager(this)
            adapter = TaskAdapter(tasks)
            taskList.adapter = adapter
            adapter.setOnItemClickListener(object : TaskAdapter.ClickListener {
                override fun onItemClick(v: View, position: Int) {
                    val task = tasks[position]
                    showTaskForm(task, "edit")
                }
            })

            if (tasks.isEmpty()) {
                noTasksTextView.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            } else {
                noTasksTextView.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }

            //para que salgan solo las task del calendario
            val borrar = arrayListOf<Task>()
            for (t in tasks) {
                if(calendarId!="" && calendarId!=t.calendar.id) {
                    borrar.add(t)
                }
            }
            tasks.removeAll(borrar)
        }

        createTaskButton.setOnClickListener {
            prefsEdit.putString("taskid", null)
            prefsEdit.apply()
            var task: Task? = null
            showTaskForm(task, "create")
        }

        closeImageView.setOnClickListener {
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.putString("calendar", null)
            prefs.apply()
            showHome()
        }

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
                    showSettings()
                    true
                }
                else -> {
                    false
                }
            }
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

        bookmarkIcon.setOnClickListener {
            showCalendar()
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
            if (authEmail != null) {
                showProfile(authEmail)
            }
        }
    }

    private fun restartView() {
        val intent = Intent(this, TaskActivity::class.java)
        finish()
        startActivity(intent)
        //quitar animación
        overridePendingTransition(0, 0)
    }

    private fun showTaskForm(task: Task?, form: String) {
        val taskFormIntent = Intent(this, TaskFormActivity::class.java)
        if (task != null) {
            taskFormIntent.putExtra("taskid", task.id.toString())
        }
        taskFormIntent.putExtra("form", form)
        startActivity(taskFormIntent)
    }

    private fun showSettings() {
        val settingsIntent = Intent(this, SettingsActivity::class.java).apply { }
        startActivity(settingsIntent)
    }

    private fun showHome() {
        val homeIntent = Intent(this, HomeActivity::class.java)
        startActivity(homeIntent)
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

    private fun showCalendar() {
        val calendarIntent = Intent(this, CalendarActivity::class.java).apply { }
        startActivity(calendarIntent)
    }

    private fun showCalendarView() {
        val calendarviewIntent = Intent(this, CalendarViewActivity::class.java).apply { }
        startActivity(calendarviewIntent)
    }

    private fun showProfile(email: String) {
        val profileIntent = Intent(this, ProfileActivity::class.java)
        if (email != null) {
            profileIntent.putExtra("userEmail", email)
        }
        startActivity(profileIntent)
    }

}