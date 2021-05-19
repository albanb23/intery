package com.albaburdallo.intery

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.albaburdallo.intery.util.entities.*
import com.albaburdallo.intery.util.entities.Calendar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class LoginActivity : BaseActivity() {

    private val GOOGLE_SIGN_IN = 100
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        this.supportActionBar?.hide()

//        db.clearPersistence()
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
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    email,
                    passwordEditText.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        showHome(it.result?.user?.email ?: "")
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
        builder.setMessage(this.getString(R.string.userError))
        builder.setPositiveButton(this.getString(R.string.accept), null)
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            try {
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val user =
                                    User(
                                        account.displayName.toString(),
                                        "",
                                        account.email.toString(),
                                        account.photoUrl.toString()
                                    )
                                var firstTime = true
                                db.collection("calendars").addSnapshotListener { value, error ->
                                    if (error != null) {
                                        return@addSnapshotListener
                                    }
                                        for (document in value!!) {
                                            val userCalendar = document.get("user") as HashMap<*, *>
                                            if (userCalendar["email"] == user.email) {
                                                firstTime = false
                                                break
                                            }
                                        }
                                        if (firstTime) {
                                            db.collection("users").document(account.email.toString()).set(
                                                user
                                            )

                                            val defaultCalendar = Calendar(
                                                account.displayName + "-" + account.email,
                                                account.displayName,
                                                "Default",
                                                "-4590167",
                                                true
                                            )

                                            val spanish = Locale("es", "ES")
                                            var currency = "$"
                                            if (this.resources?.configuration?.locales?.get(0)==spanish){
                                                currency = "€"
                                            }

                                            db.collection("calendars").document(defaultCalendar.id)
                                                .set(defaultCalendar)
                                            db.collection("common").document(user.email).set(
                                                hashMapOf("money" to "0.0", "currency" to currency)
                                            )

                                            populate(user)
                                        }
                                    }
                                showHome(account.email ?: "")
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

    private fun populate(user: User) {
        //popular tareas
        val start1 = java.util.Calendar.getInstance()
        start1.set(java.util.Calendar.DAY_OF_MONTH, 10)
        start1.set(java.util.Calendar.HOUR_OF_DAY, 8)
        start1.set(java.util.Calendar.MINUTE, 0)
        start1.set(java.util.Calendar.SECOND, 0)
        val end1 = java.util.Calendar.getInstance()
        end1.set(java.util.Calendar.DAY_OF_MONTH, 10)
        end1.set(java.util.Calendar.HOUR_OF_DAY, 10)
        end1.set(java.util.Calendar.MINUTE, 0)
        end1.set(java.util.Calendar.SECOND, 0)
        val task1 = Task(
            "00-"+resources.getString(R.string.populateTaskName1)+"-"+user.email,
            resources.getString(R.string.populateTaskName1),
            start1.time,
            start1.time,
            end1.time,
            end1.time,
            false,
            false,
            resources.getString(R.string.populateTaskDesc1),
            false,
            Calendar(
                user.name + "-" + user.email,
                user.name,
                "Default",
                "-4590167"
            ),
            "0"
        )
        db.collection("tasks").document("00-"+resources.getString(R.string.populateTaskName1)+"-"+user.email).set(task1)

        val start2 = java.util.Calendar.getInstance()
        start2.set(java.util.Calendar.DAY_OF_MONTH, 1)
        start2.set(java.util.Calendar.HOUR_OF_DAY, 8)
        start2.set(java.util.Calendar.MINUTE, 0)
        start2.set(java.util.Calendar.SECOND, 0)
        val task2 = Task(
            "00-"+resources.getString(R.string.populateTaskName2)+"-"+user.email,
            resources.getString(R.string.populateTaskName2),
            start1.time,
            true,
            false,
            "",
            true,
            Calendar(
                user.name + "-" + user.email,
                user.name,
                "Default",
                "-4590167"
            ),
            "0"
        )
        db.collection("tasks").document("00-"+resources.getString(R.string.populateTaskName2)+"-"+user.email).set(task2)

        //popular cartera
        val entity1 = Entity("Spotify-"+user.email, "Spotify")
        db.collection("entities").document("Spotify-"+user.email).set(entity1)
        val section1 = Section(resources.getString(R.string.populateSection1)+"-"+user.email, resources.getString(R.string.populateSection1))
        db.collection("sections").document(resources.getString(R.string.populateSection1)+"-"+user.email).set(section1)
        val transaction1 = Transaction("00-Spotify-"+user.email,
            true,
            false,
            "Spotify",
            14.99,
            start2.time,
            "",
            entity1,
            section1
        )
        db.collection("wallet").document("00-Spotify-"+user.email).set(transaction1)

        val entity2 = Entity(resources.getString(R.string.populateEntityName)+"-"+user.email, resources.getString(R.string.populateEntityName))
        db.collection("entities").document(resources.getString(R.string.populateEntityName)+"-"+user.email).set(entity2)
        val section2 = Section(resources.getString(R.string.populateSection2)+"-"+user.email, resources.getString(R.string.populateSection2))
        db.collection("sections").document(resources.getString(R.string.populateSection2)+"-"+user.email).set(section2)
        val transaction2 = Transaction("00-"+resources.getString(R.string.populateTransactionName)+"-"+user.email,
            true,
            false,
            resources.getString(R.string.populateTransactionName),
            54.23,
            start2.time,
            "",
            entity2,
            section2
        )
        db.collection("wallet").document("00-"+resources.getString(R.string.populateTransactionName)+"-"+user.email).set(transaction2)

        val entity3 = Entity(resources.getString(R.string.populateEntity3)+"-"+user.email, resources.getString(R.string.populateEntity3))
        db.collection("entities").document(resources.getString(R.string.populateEntity3)+"-"+user.email).set(entity3)
        val section3 = Section("Bizum-"+user.email, "Bizum")
        db.collection("sections").document("Bizum-"+user.email).set(section3)
        val transaction3 = Transaction("00-"+resources.getString(R.string.populateTransactionName2)+"-"+user.email,
            false,
            true,
            resources.getString(R.string.populateTransactionName2),
            20.00,
            start1.time,
            "",
            entity3,
            section3
        )
        db.collection("wallet").document("00-"+resources.getString(R.string.populateTransactionName2)+"-"+user.email).set(transaction3)

        //popular habitos
        val habit = Habit(user.email+"-"+resources.getString(R.string.populateHabitName),
            resources.getString(R.string.populateHabitName),
            "",
            Date(2021, 2, 29),
            "-4590167",
            false,
            7,
            7,
            50.0,
            start1.time,
            "29/03/2021;6/04/2021;18/04/2021;30/04/2021;1/05/2021")
        db.collection("habits").document(user.email+"-"+resources.getString(R.string.populateHabitName)).set(habit)
    }
}