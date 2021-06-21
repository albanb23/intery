package com.albaburdallo.intery.habit

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.*
import com.albaburdallo.intery.task.TaskActivity
import com.albaburdallo.intery.util.entities.Habit
import com.albaburdallo.intery.wallet.WalletActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_habit.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.habit_popup.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.options.*


class HabitActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email
    private lateinit var habitList: RecyclerView
    private lateinit var adapter: HabitAdapter
    private lateinit var habits: MutableList<Habit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val dialogShown = prefs.getBoolean("habitDialog", false)
        val lang = prefs.getString("language", "es")

        if (!dialogShown) {

            // creamos la vista de popup
            val inflater:LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.habit_popup, null)
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
            val habitPopUpEsp = view.findViewById<ImageView>(R.id.habit_popup_esp)
            val habitPopUpEng = view.findViewById<ImageView>(R.id.habit_popup_eng)
            if (lang!="" && lang=="es") {
                habitPopUpEsp.visibility = View.VISIBLE
                habitPopUpEng.visibility = View.GONE
            } else {
                habitPopUpEng.visibility = View.VISIBLE
                habitPopUpEsp.visibility = View.GONE
            }

            //boton para cerrar el popup
            val habitClosePopup = view.findViewById<TextView>(R.id.habitClose)
            habitClosePopup.setOnClickListener {
                popup.dismiss()
            }

            //enseñamos el popup en la app
            Handler().postDelayed(Runnable {
                TransitionManager.beginDelayedTransition(activity_habit)
                popup.showAtLocation(
                    findViewById(R.id.activity_habit), // Location to display popup window
                    Gravity.CENTER, // Exact position of layout to display popup
                    0, // X offset
                    0 // Y offset
                )
            }, 100)


            val editor = prefs.edit()
            editor.putBoolean("habitDialog", true)
            editor.apply()
        }
    }

    override fun onStart() {
        super.onStart()

        habitsCloseImage.setOnClickListener { showHome() }

        habitList = findViewById(R.id.habitsList)
        habits = arrayListOf()

        //populamos la lista del recycler view
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
                    habits.add(
                        Habit(
                            id,
                            name,
                            notes,
                            startDate.toDate(),
                            color,
                            notifyMe,
                            whenHabit.toDate(),
                            period.toInt(),
                            times.toInt(),
                            progress,
                            updated.toDate(),
                            days
                        )
                    )
                } else {
                    habits.add(
                        Habit(
                            id,
                            name,
                            notes,
                            startDate.toDate(),
                            color,
                            notifyMe,
                            period.toInt(),
                            times.toInt(),
                            progress,
                            updated.toDate(),
                            days
                        )
                    )
                }
            }

            //adapter del recycler view
            habitList.layoutManager = LinearLayoutManager(this)
            adapter = HabitAdapter(habits)
            habitList.adapter = adapter
            adapter.setOnItemClickListener(object : HabitAdapter.ClickListener {
                override fun onItemClick(v: View, position: Int) {
                    val habit = habits[position]
                    showHabit(habit)
                }

            })

            //si no hay hábitos mostramos el mensaje
            if (habits.isEmpty()) {
                noHabitsTextView.visibility = View.VISIBLE
            } else {
                noHabitsTextView.visibility = View.GONE
            }
        }

        createHabitButton.setOnClickListener { showHabitForm(null, "create") }

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
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            //onBackPressed() //para volver a la pantalla anterior
            showLogin()
        }

        // al pulsar sobre la foto de perfil del menu se va al perfil
        val header = nav_view.getHeaderView(0)
        val profilePicImage = header.findViewById<ImageView>(R.id.profilePicImage)
        profilePicImage.setOnClickListener {
            showProfile(authEmail!!)
        }

        db.collection("users").document(authEmail!!).get().addOnSuccessListener {
            var photo = it.get("photo") as String
            if (photo != "") {
                Picasso.get().load(photo).transform(CropCircleTransformation()).into(profilePicImage)
            } else {
                Picasso.get()
                    .load("https://global-uploads.webflow.com/5bcb46130508ef456a7b2930/5f4c375c17865e08a63421ac_drawkit-og.png")
                    .transform(CropCircleTransformation()).into(profilePicImage)
            }
        }


    }

    private fun showHabit(habit: Habit) {
        val habitIntent = Intent(this, HabitShowActivity::class.java)
        if (habit!=null) {
            habitIntent.putExtra("habitId", habit.id.toString())
        }
        startActivity(habitIntent)
    }

    private fun showHabitForm(habit: Habit?, form: String) {
        val habitIntent = Intent(this, HabitFormActivity::class.java)
        if (habit!=null) {
            habitIntent.putExtra("habitId", habit.id.toString())
        }
        habitIntent.putExtra("form", form)
        startActivity(habitIntent)
    }

    private fun showLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java).apply { }
        startActivity(loginIntent)
    }

    private fun showSettings() {
        val settingsIntent = Intent(this, SettingsActivity::class.java).apply { }
        startActivity(settingsIntent)
    }

    private fun showHome() {
        val homeIntent = Intent(this, HomeActivity::class.java).apply { }
        startActivity(homeIntent)
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