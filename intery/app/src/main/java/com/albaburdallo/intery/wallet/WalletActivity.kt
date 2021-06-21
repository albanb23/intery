package com.albaburdallo.intery.wallet

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
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
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.*
import com.albaburdallo.intery.habit.HabitActivity
import com.albaburdallo.intery.util.entities.Transaction
import com.albaburdallo.intery.task.TaskActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.options.*
import kotlinx.android.synthetic.main.activity_wallet.*
import kotlinx.android.synthetic.main.activity_wallet.view.*
import kotlinx.android.synthetic.main.loading_layout.*
import kotlinx.android.synthetic.main.nav_header.*

class WalletActivity : BaseActivity(){

    private val db = FirebaseFirestore.getInstance()

    private lateinit var walletList: RecyclerView
    private lateinit var adapter: WalletAdapter
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val dialogShown = prefs.getBoolean("walletDialog", false)
        val lang = prefs.getString("language", "es")

        if (!dialogShown) {

            // creamos la vista de popup
            val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.wallet_popup, null)
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
            val walletPopUpEsp = view.findViewById<ImageView>(R.id.wallet_popup_esp)
            val walletPopUpEng = view.findViewById<ImageView>(R.id.wallet_popup_eng)
            if (lang!="" && lang=="es") {
                walletPopUpEsp.visibility = View.VISIBLE
                walletPopUpEng.visibility = View.GONE
            } else {
                walletPopUpEng.visibility = View.VISIBLE
                walletPopUpEsp.visibility = View.GONE
            }

            //boton para cerrar el popup
            val walletClosePopup = view.findViewById<TextView>(R.id.walletClose)
            walletClosePopup.setOnClickListener {
                popup.dismiss()
            }

            //enseñamos el popup en la app
            Handler().postDelayed(Runnable {
                TransitionManager.beginDelayedTransition(activity_wallet)
                popup.showAtLocation(
                    findViewById(R.id.activity_wallet), // Location to display popup window
                    Gravity.CENTER, // Exact position of layout to display popup
                    0, // X offset
                    0 // Y offset
                )
            }, 100)


            val editor = prefs.edit()
            editor.putBoolean("walletDialog", true)
            editor.apply()
        }
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
            }
        }

        moneyTextNumber.setSelection(moneyTextNumber.length())
        moneyTextNumber.doAfterTextChanged { text ->
            if (authEmail != null) {
                db.collection("common").document(authEmail).update("money", text.toString())
                prefs.putString("totalMoney",text.toString())
                prefs.apply()
            }
            moneyTextNumber.setSelection(moneyTextNumber.length())
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
                    showSettings()
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

    private fun showSettings() {
        val settingsIntent = Intent(this, SettingsActivity::class.java).apply { }
        startActivity(settingsIntent)
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