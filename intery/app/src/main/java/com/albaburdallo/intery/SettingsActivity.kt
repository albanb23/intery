package com.albaburdallo.intery

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*


class SettingsActivity : BaseActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email
    private lateinit var context: Context
    private lateinit var res: Resources

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()

        closeSettingsImageView.setOnClickListener { showHome() }

        val languages = arrayListOf<String>()
        languages.add(resources.getString(R.string.english)) //inglés
        languages.add(resources.getString(R.string.spanish)) //español
        val languageSpinner: Spinner = findViewById(R.id.languageSpinner)
        val languageAdapter = ArrayAdapter<String>(
            applicationContext,
            android.R.layout.simple_spinner_item,
            languages
        )
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = languageAdapter

        languageSpinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position==0) {
                    //se cambia a inglés
                    setLocale("en")
                } else {
                    //se cambia a español
                    setLocale("es")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        })
        val langSelection =
            when(this.resources?.configuration?.locales?.get(0)) {
                Locale.ENGLISH -> 0
                else -> 1
            }
        languageSpinner.setSelection(langSelection)

        val currencies = arrayListOf("$", "€")
        val currencySpinner = findViewById<Spinner>(R.id.currencySpinner)
        val currencyAdapter = ArrayAdapter<String>(
            applicationContext,
            android.R.layout.simple_spinner_item,
            currencies
        )
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = currencyAdapter
        currencySpinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> db.collection("common").document(authEmail!!).update("currency", "$")
                    else -> db.collection("common").document(authEmail!!).update("currency", "€")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })
        db.collection("common").document(authEmail!!).get().addOnSuccessListener {
            val curr = it.get("currency") as String
            var currSelection = if (curr=="$") {
                0
            } else {
                1
            }

            currencySpinner.setSelection(currSelection)
        }

        privacyPolicy.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.privacyPolicy))
            builder.setMessage(resources.getString(R.string.privacy_policy))
            builder.setPositiveButton(resources.getString(R.string.done), null)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun setLocale(lang: String?) {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("language", lang)
        prefs.apply()
        val myLocale = Locale(lang)
        var refresh = false
        if (resources?.configuration?.locales?.get(0)!=myLocale) {
            refresh = true
        }
        val res = resources
        val dm = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.setLocale(myLocale)
        res.updateConfiguration(conf, dm)
        if (refresh) {
            restartView()
        }
    }

    private fun restartView() {
        val intent = Intent(this, SettingsActivity::class.java)
        finish()
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun showHome() {
        val homeIntent = Intent(this, HomeActivity::class.java).apply { }
        startActivity(homeIntent)
    }

}