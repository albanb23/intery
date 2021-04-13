package com.albaburdallo.intery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.Toast
import com.albaburdallo.intery.util.entities.User
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.nav_header.*

class ProfileActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
    }

    override fun onStart() {
        super.onStart()

        val user = FirebaseAuth.getInstance().currentUser
        db.collection("users").document(authEmail!!).get().addOnSuccessListener {
            val name = it.get("name") as String
            val surname = it.get("surname") as String
            val photo = it.get("photo") as String

            if (photo!="") {
                Picasso.get().load(photo).transform(CropCircleTransformation()).into(editProfileImageView)
            }
            profileNameEditText.setText(name)
            profileSurnameEditText.setText(surname)
            profileEmailEditText.setText(authEmail)
        }

        forgotPassText.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(authEmail).addOnSuccessListener {
                Toast.makeText(this, resources.getString(R.string.resetEmail), Toast.LENGTH_LONG).show()
            }
        }

        saveProfileButton.setOnClickListener {
            val new = User(profileNameEditText.text.toString(), profileSurnameEditText.text.toString(), authEmail, "")
            if (user != null && profileNewPassEditText.text.toString() != "") {
                changePassword(user, authEmail)
            }
            db.collection("users").document(authEmail).set(new)
        }

        backProfileImage.setOnClickListener { onBackPressed() }
    }

    fun changePassword(user: FirebaseUser, authEmail: String) {
        if (authEmail!="" && validatePassword()) {
            val credential =
                EmailAuthProvider.getCredential(authEmail, profileOldPassEditText.text.toString())

            user.reauthenticate(credential).addOnCompleteListener {
                if (it.isSuccessful) {
                    val newPass = findViewById<EditText>(R.id.profileNewPassEditText).text
                    user.updatePassword(newPass.toString()).addOnSuccessListener {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.passUpdated),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.oldPassNotValid),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun validatePassword(): Boolean {
        var res = true

        if (profileNewPassEditText.text.toString()!="") {
            res = profileOldPassEditText.validator().nonEmpty()
                .addErrorCallback { profileOldPassEditText.error = it }.check()
                    && profileRepeatNewPassEditText.validator().nonEmpty()
                .addErrorCallback { profileRepeatNewPassEditText.error = it }.check()
                    && profileOldPassEditText.validator()
                .textNotEqualTo(profileNewPassEditText.text.toString())
                .addErrorCallback { profileNewPassEditText.error = it }.check()
                    && profileNewPassEditText.validator()
                .textEqualTo(profileRepeatNewPassEditText.text.toString())
                .addErrorCallback { profileRepeatNewPassEditText.error = it }.check()
        }
        return res
    }

    private fun showHome() {
        val homeIntent = Intent(this, HomeActivity::class.java)
        startActivity(homeIntent)
    }
}