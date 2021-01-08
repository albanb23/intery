package com.albaburdallo.tfg

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        this.getSupportActionBar()?.hide()

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val name = bundle?.getString("name")

        setup(email?:"", name?:"")
    }

    //Setup
    private fun setup(email: String, name: String) {
        userEmailTextView.text = email
        userNameTextView.text = name

        logOutButton.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            //onBackPressed() //para volver a la pantalla anterior
            showLogin()
        }
    }

    private fun showLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java).apply { }
        startActivity(loginIntent)
    }
}