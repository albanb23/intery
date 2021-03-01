package com.albaburdallo.intery.wallet

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.HomeActivity
import com.albaburdallo.intery.LoginActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.habit.HabitActivity
import com.albaburdallo.intery.model.entities.Entity
import com.albaburdallo.intery.model.entities.Section
import com.albaburdallo.intery.model.entities.Transaction
import com.albaburdallo.intery.task.TaskActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_options.*
import kotlinx.android.synthetic.main.activity_wallet.*
import java.util.HashMap

class WalletActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var walletList: RecyclerView
    private lateinit var adapter: WalletAdapter
    private lateinit var transactions: MutableList<Transaction>
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
        setup()
    }

    private fun setup() {

        closeWalletImageView.setOnClickListener { showHome() }

        walletList = findViewById(R.id.walletList)
        transactions = arrayListOf()

        db.collection("wallet").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val user = document.get("user") as HashMap<*, *>
                if (user["email"] == authEmail) {
                    val isExpenditure = document.get("id") as Boolean
                    val isIncome = document.get("id") as Boolean
                    val concept = document.get("id") as String
                    val money = document.get("id") as Double
                    val date = document.get("id") as Timestamp
                    val notes = document.get("id") as String
                    val entity = document.get("id") as HashMap<String, String>
                    val section = document.get("id") as HashMap<String, String>
                    transactions.add(Transaction(isExpenditure, isIncome, concept, money,
                        date.toDate(), notes, Entity(entity["name"], entity["description"]),
                        Section(section["name"], section["description"], section["color"])
                    ))
                }
            }

            walletList.layoutManager = LinearLayoutManager(this)
            adapter = WalletAdapter(transactions)
            walletList.adapter = adapter
            //setonclicklistener
        }

        addTransactionButton.setOnClickListener {

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