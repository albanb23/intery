package com.albaburdallo.intery

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.albaburdallo.intery.model.entities.Calendar
import com.albaburdallo.intery.model.entities.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        this.getSupportActionBar()?.hide()

        setup()
    }

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
                                "",
                                "-4590167", true
                            )
                            db.collection("calendars").document(defaultCalendar.id)
                                .set(defaultCalendar)
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
    }

    private fun validateForm(): Boolean {
        var res = true
        if (emailEditTextSignIn != null && passwordEditTextSignIn != null) {
            res = nameEditText.validator().nonEmpty().addErrorCallback { nameEditText.error = it }
                .check() &&
                    surnameEditText.validator().nonEmpty()
                        .addErrorCallback { surnameEditText.error = it }.check() &&
                    emailEditTextSignIn.validator().nonEmpty().validEmail()
                        .addErrorCallback { emailEditTextSignIn.error = it }.check() &&
                    passwordEditTextSignIn.validator().nonEmpty().atleastOneNumber()
                        .addErrorCallback { passwordEditTextSignIn.error = it }.check() &&
                    repeatPasswordEditText.validator()
                        .textEqualTo(passwordEditTextSignIn.text.toString())
                        .addErrorCallback { repeatPasswordEditText.error = it }.check()
        } else {
            Toast.makeText(
                this,
                "Se ha producido un error. Por favor revise su email y contraseña",
                Toast.LENGTH_LONG
            ).show()
        }
        return res
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error registrando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java).apply { }
        startActivity(loginIntent)
    }
}