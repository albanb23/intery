package com.albaburdallo.intery.habit

import android.content.Context
import android.content.Intent
import android.icu.number.IntegerWidth
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.HomeActivity
import com.albaburdallo.intery.LoginActivity
import com.albaburdallo.intery.ProfileActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.task.TaskActivity
import com.albaburdallo.intery.util.entities.Habit
import com.albaburdallo.intery.wallet.WalletActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_habit.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.options.*

class HabitActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email
    private lateinit var habitList: RecyclerView
    private lateinit var adapter: HabitAdapter
    private lateinit var habits: MutableList<Habit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit)
    }

    override fun onStart() {
        super.onStart()

        habitsCloseImage.setOnClickListener { showHome() }

        habitList = findViewById(R.id.habitsList)
        habits = arrayListOf()

//        db.clearPersistence()
        db.collection("habits").whereEqualTo("user.email", authEmail).addSnapshotListener { value, error ->
            if (error!=null) {
                return@addSnapshotListener
            }

            var endDate: Timestamp?
            habits.clear()
            for(document in value!!) {
                val id = document.get("id") as String
                val name = document.get("name") as String
                val notes = document.get("description") as String
                val startDate = document.get("startDate") as Timestamp
                endDate = document.get("endDate") as? Timestamp
                val color = document.get("color") as String
                val notifyMe = document.get("notifyMe") as Boolean
                val whenHabit = document.get("when") as? Timestamp
                val frequency = document.get("frequency") as Long
                val progress = document.get("progress") as Long
                val updated = document.get("updated") as Timestamp
                if (endDate != null && whenHabit!= null) {
                    habits.add(Habit(id, name, notes, startDate.toDate(), endDate.toDate(), color, notifyMe, whenHabit.toDate(), frequency.toInt(), progress.toInt(), updated.toDate()))
                } else if (whenHabit != null){
                    habits.add(Habit(id, name, notes, startDate.toDate(), color, notifyMe, whenHabit.toDate(), frequency.toInt(), progress.toInt(), updated.toDate()))
                } else if (endDate!=null){
                    habits.add(Habit(id, name, notes, startDate.toDate(), endDate.toDate(), color, notifyMe, frequency.toInt(), progress.toInt(), updated.toDate()))
                } else {
                    habits.add(Habit(id, name, notes, startDate.toDate(), color, notifyMe, frequency.toInt(), progress.toInt(), updated.toDate()))
                }
            }

            habitList.layoutManager = LinearLayoutManager(this)
            adapter = HabitAdapter(habits)
            habitList.adapter = adapter
            adapter.setOnItemClickListener(object: HabitAdapter.ClickListener{
                override fun onItemClick(v: View, position: Int) {
                    val habit = habits[position]
                    showHabit(habit)
                }

            })
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
            if (photo != "") {
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