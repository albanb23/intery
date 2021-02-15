package com.albaburdallo.intery

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ActionMode
import androidx.core.view.GravityCompat
import com.albaburdallo.intery.habit.HabitActivity
import com.albaburdallo.intery.task.TaskActivity
import com.albaburdallo.intery.wallet.WalletActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_options.*

class HomeActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        this.getSupportActionBar()?.hide()

        val bundle = intent.extras
        val email = bundle?.getString("email")
//        val name = bundle?.getString("name")

        setup(email?:"")

        //Guardado de datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
//        prefs.putString("name", userNameTextView.text.toString())
        prefs.apply()
    }

    //Setup
    private fun setup(email: String) {
//        userEmailTextView.text = email
//        db.collection("users").document(email).get().addOnSuccessListener {
//            userNameTextView.text = " " + it.get("name") as String? + "!"
//        }

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.tasks_item -> {
                    showTask()
                    val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
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
            drawerLayout.openDrawer(GravityCompat.START);
        }

        walletLayout.setOnClickListener {
            showWallet()
        }

        taskFrame.setOnClickListener {
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



}