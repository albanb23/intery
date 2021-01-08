package com.albaburdallo.intery

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.inputsLinearLayout
import kotlinx.android.synthetic.main.activity_sign_in.*

class LoginActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100
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
        session()
    }

    override fun onStart() {
        super.onStart()

        inputsLinearLayout.visibility = View.VISIBLE
        buttonsLinearLayout.visibility = View.VISIBLE
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
//        val name = prefs.getString("name", null)

        if (email != null) {
            inputsLinearLayout.visibility = View.INVISIBLE
            buttonsLinearLayout.visibility = View.INVISIBLE
            showHome(email)
        }
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
                val email = emailEditText.text.toString()
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email,
                passwordEditText.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful) {
                            showHome(it.result?.user?.email?: "")
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        //Log in with Google
        logInGoogleButton.setOnClickListener {
            //Config
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
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
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            //donde se pasan los parametros
            putExtra("email", email)
        }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            try {
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val user = hashMapOf("name" to account.displayName.toString())
                            db.collection("users").document(account.email.toString()).set(user)
                            showHome(account.email?: "")
                        } else {
                            showAlert()
                        }
                    }
                }
            } catch (e: ApiException) {
                showAlert()
            }
        }
    }
}