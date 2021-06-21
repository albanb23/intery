package com.albaburdallo.intery

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

open class BaseActivity: AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "es")
        val myLocale = Locale(lang)
        val res = resources
        val dm = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.setLocale(myLocale)
        res.updateConfiguration(conf, dm)
    }
}