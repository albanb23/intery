package com.albaburdallo.intery

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.albaburdallo.intery.util.entities.*
import com.albaburdallo.intery.util.entities.Calendar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.android.synthetic.main.activity_sign_in.*
import java.util.*


class SignInActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        setup()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setup() {
        //Sign in
        signInButton.setOnClickListener {
            //validamos el formulario
            if (validateForm()) {
                if (!termsCheckBox.isChecked) {
                    Toast.makeText(
                        this,
                        this.getString(R.string.termsAndConditions),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                        emailEditTextSignIn.text.toString(),
                        passwordEditTextSignIn.text.toString()
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            //Creamos el nuevo usuario
                            val user =
                                User(
                                    nameEditText.text.toString(), surnameEditText.text.toString(),
                                    emailEditTextSignIn.text.toString(), ""
                                )
                            db.collection("users").document(emailEditTextSignIn.text.toString())
                                .set(user)
                            //creamos el calendario por defecto
                            val defaultCalendar = Calendar(
                                nameEditText.text.toString() + "-" + emailEditTextSignIn.text.toString(),
                                nameEditText.text.toString(),
                                "Default",
                                "-4590167", true
                            )
                            db.collection("calendars").document(defaultCalendar.id)
                                .set(defaultCalendar)
                            val spanish = Locale("es", "ES")
                            var currency = "$"
                            if (this.resources?.configuration?.locales?.get(0)==spanish){
                                currency = "€"
                            }
                            db.collection("common").document(user.email).set(
                                hashMapOf("money" to "0.0", "currency" to currency)
                            )
                            populate(user)
                            showLogin()
                        } else {
                            showAlert()
                        }
                    }
                }
            }
        }

        //close view
        closeViewSignIn.setOnClickListener {
            showLogin()
        }

        termsTexView.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.terminos_y_condiciones))
            builder.setMessage(resources.getString(R.string.terms_conditions))
            builder.setPositiveButton(resources.getString(R.string.done), null)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun validateForm(): Boolean {
        var res = true
        if (emailEditTextSignIn != null && passwordEditTextSignIn != null) {
            res = nameEditText.validator().nonEmpty().addErrorCallback { nameEditText.error = resources.getString(R.string.nonEmptyValidation)
            nameEditText.background = AppCompatResources.getDrawable(this, R.drawable.error_input)}
                .check() &&
                    surnameEditText.validator().nonEmpty()
                        .addErrorCallback { surnameEditText.error = resources.getString(R.string.nonEmptyValidation)
                        surnameEditText.background = AppCompatResources.getDrawable(this, R.drawable.error_input)}.check() &&
                    emailEditTextSignIn.validator().nonEmpty().validEmail()
                        .addErrorCallback { emailEditTextSignIn.error = resources.getString(R.string.nonEmptyValidation)
                        emailEditTextSignIn.background = AppCompatResources.getDrawable(this, R.drawable.error_input)}.check() &&
                    passwordEditTextSignIn.validator().nonEmpty().atleastOneNumber()
                        .addErrorCallback { passwordEditTextSignIn.error = resources.getString(R.string.nonEmptyValidation)
                        passwordEditTextSignIn.background = AppCompatResources.getDrawable(this, R.drawable.error_input)}.check() &&
                    repeatPasswordEditText.validator()
                        .textEqualTo(passwordEditTextSignIn.text.toString())
                        .addErrorCallback { repeatPasswordEditText.error = resources.getString(R.string.equalToValidation)
                        repeatPasswordEditText.background = AppCompatResources.getDrawable(this, R.drawable.error_input)}.check()
        } else {
            Toast.makeText(
                this,
                resources.getString(R.string.loginError),
                Toast.LENGTH_LONG
            ).show()
        }
        return res
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(resources.getString(R.string.alertError))
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java).apply { }
        startActivity(loginIntent)
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