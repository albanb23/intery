package com.albaburdallo.intery.wallet

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.HomeActivity
import com.albaburdallo.intery.LoginActivity
import com.albaburdallo.intery.ProfileActivity
import com.albaburdallo.intery.R
import com.albaburdallo.intery.habit.HabitActivity
import com.albaburdallo.intery.util.entities.Transaction
import com.albaburdallo.intery.task.TaskActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.options.*
import kotlinx.android.synthetic.main.activity_wallet.*
import kotlinx.android.synthetic.main.activity_wallet.view.*
import kotlinx.android.synthetic.main.loading_layout.*
import kotlinx.android.synthetic.main.nav_header.*

class WalletActivity : AppCompatActivity(){

    private val db = FirebaseFirestore.getInstance()

    private lateinit var walletList: RecyclerView
    private lateinit var adapter: WalletAdapter
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
//        loadingLottie.setAnimation(R.raw.loading)
//        loadingLottie.playAnimation()
//        loadingLottie.repeatCount = LottieDrawable.INFINITE
    }


    override fun onStart() {
        super.onStart()
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        closeWalletImageView.setOnClickListener { showHome() }
        graphImageView.setOnClickListener { showChart() }

        if (authEmail != null) {
            db.collection("common").document(authEmail).addSnapshotListener(this) { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value!!.get("money")!=null && value.get("currency")!=null) {
                    moneyTextNumber.setText(value.get("money") as String)
                    prefs.putString("totalMoney",value.get("money") as String)
                    prefs.apply()
                    currencyTextView.text = value.get("currency") as String
                }
//                loadingLayout.visibility = View.GONE
            }
        }

        moneyTextNumber.setSelection(moneyTextNumber.length())
        moneyTextNumber.doAfterTextChanged { text ->
//            loadingLayout.visibility = View.VISIBLE
            if (authEmail != null) {
                db.collection("common").document(authEmail).set(
                    hashMapOf("money" to text.toString(), "currency" to "$")
                )
                prefs.putString("totalMoney",text.toString())
                prefs.apply()
            }
            moneyTextNumber.setSelection(moneyTextNumber.length())
//            loadingLayout.visibility = View.GONE
        }

        walletList = findViewById(R.id.walletList)

        var query = db.collection("wallet").orderBy("date", Query.Direction.DESCENDING)

        query = query.whereEqualTo("user.email", authEmail)
        query.addSnapshotListener(this) { documents, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            val options = FirestoreRecyclerOptions.Builder<Transaction>().setQuery(query, Transaction::class.java).build()
            walletList.layoutManager = LinearLayoutManager(this)
            adapter = WalletAdapter(options)
            walletList.adapter = adapter
            //setonclicklistener
            adapter.setOnItemClickListener(object: WalletAdapter.ClickListener{
                override fun onItemCLick(v: View, position: Int) {
                    val transaction = adapter.getItem(position)
                    showWalletForm(transaction, "edit")
                }
            })
            adapter.startListening()
//            query.addSnapshotListener { value, error ->
//                if (value!!.isEmpty) {
//                    noTransactionsTextView.visibility = View.VISIBLE
//                }
//            }
//            loadingLayout.visibility = View.GONE
        }

        addTransactionButton.setOnClickListener {
            prefs.putString("transactionId", null)
            prefs.apply()
            val transaction: Transaction? = null
            showWalletForm(transaction, "create")
        }

        toggleGroupMain.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when(checkedId) {
                R.id.expendituresButton -> {
                    if (isChecked) {
                        val newQuery = query.whereEqualTo("expenditure", true)
                        val newOptions = FirestoreRecyclerOptions.Builder<Transaction>()
                            .setQuery(newQuery, Transaction::class.java).build()

                        adapter.updateOptions(newOptions)
                    }
                }
                R.id.incomesButton -> {
                    if (isChecked) {
                        val newQuery = query.whereEqualTo("income", true)
                        val newOptions = FirestoreRecyclerOptions.Builder<Transaction>()
                            .setQuery(newQuery, Transaction::class.java).build()

                        adapter.updateOptions(newOptions)
                    }
                }
                R.id.allButton ->{
                    if (isChecked) {
                        val newQuery = query
                        val newOptions = FirestoreRecyclerOptions.Builder<Transaction>()
                            .setQuery(newQuery, Transaction::class.java).build()

                        adapter.updateOptions(newOptions)
                    }
                }
            }
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

        logOutButton.setOnClickListener {
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

    private fun showWalletForm(transaction: Transaction?, form: String) {
        val walletFormIntent = Intent(this, WalletFormActivity::class.java)
        if (transaction != null) {
            walletFormIntent.putExtra("transactionId", transaction.id.toString())
        }
        walletFormIntent.putExtra("form", form)
        startActivity(walletFormIntent)
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

    private fun showChart() {
        val chartIntent = Intent(this, ChartActivity::class.java).apply { }
        startActivity(chartIntent)
    }

    private fun showProfile(email: String) {
        val profileIntent = Intent(this, ProfileActivity::class.java)
        if (email != null) {
            profileIntent.putExtra("userEmail", email)
        }
        startActivity(profileIntent)
    }

}