package com.albaburdallo.tfg

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        this.getSupportActionBar()?.hide()

        //Analytics event
        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración de Firebase")
        analytics.logEvent("InitScreen", bundle)

        setup()
    }

    //Setup
    private fun setup() {
        //Go to Sign In
        val signIn = findViewById<TextView>(R.id.signInTextView)
        signIn.setOnClickListener {
            val signInIntent = Intent(this, SignInActivity::class.java)
            startActivity(signInIntent)

            signInTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        //Log in
        logInButton.setOnClickListener {
            //comprobamos que se haya metido email y contraseña
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.text.toString(),
                passwordEditText.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful) {
                        showHome(it.result?.user?.email?:"")  //email?:"" es por si el email no existe, no pasa por it.isSuccessful
                    } else {
                        showAlert()
                    }
                }
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String) {
//        var name = ""
//        db.collection("users").get().addOnSuccessListener { result ->
//            for(document in result) {
//                name = document.data["name"].toString()
//            }
//        }
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            //donde se pasan los parametros
            putExtra("email", email)
//            putExtra("name", name)
        }
        startActivity(homeIntent)
    }
}