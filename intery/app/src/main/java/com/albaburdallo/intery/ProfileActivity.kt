package com.albaburdallo.intery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.albaburdallo.intery.util.entities.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.nav_header.*
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class ProfileActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private val storage = FirebaseStorage.getInstance()
    private val storageReference = storage.reference
    private var profilePic: String = ""

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
                profilePic = photo
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
            val new = User(profileNameEditText.text.toString(), profileSurnameEditText.text.toString(), authEmail, profilePic)
            if (user != null && profileNewPassEditText.text.toString() != "") {
                changePassword(user, authEmail)
            }
            db.collection("users").document(authEmail).set(new)
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(profileNameEditText.text.toString())
                .setPhotoUri(Uri.parse(profilePic))
                .build()
            user?.updateProfile(profileUpdates)?.addOnSuccessListener {
                showHome()
            }
        }

        backProfileImage.setOnClickListener { onBackPressed() }

        editProfileImageView.setOnClickListener { launchGallery() }

        uploadImage.setOnClickListener { uploadImage() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null || data.data == null) {
                return
            }

            filePath = data.data
            try {
                Picasso.get().load(filePath.toString()).transform(CropCircleTransformation()).into(editProfileImageView)
                uploadImage.visibility = View.VISIBLE
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
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

    private fun launchGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PICK_IMAGE_REQUEST)
    }

    private fun uploadImage() {
        if (filePath != null) {
            val ref = storageReference.child("uploads/" + UUID.randomUUID().toString())
            val uploadTask = ref.putFile(filePath!!)

            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    Picasso.get().load(downloadUri.toString()).transform(CropCircleTransformation()).into(editProfileImageView)
                    uploadImage.visibility = View.GONE
                    profilePic = downloadUri.toString()
                } else {
                    Toast.makeText(this, resources.getString(R.string.fileError), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showHome() {
        val homeIntent = Intent(this, HomeActivity::class.java)
        startActivity(homeIntent)
    }
}