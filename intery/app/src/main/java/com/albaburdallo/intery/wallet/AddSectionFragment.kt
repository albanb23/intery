package com.albaburdallo.intery.wallet

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.entities.Section
import com.google.firebase.auth.FirebaseAuth
import java.lang.IllegalStateException

class AddSectionFragment: DialogFragment() {

    companion object {
        fun newInstance(): AddSectionFragment = AddSectionFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val dialogView = requireActivity().layoutInflater.inflate(R.layout.create_section, null)
            builder.setView(dialogView)
                .setPositiveButton(R.string.save) { dialog, id ->
                    val name =
                        dialogView.findViewById<EditText>(R.id.sectionNameEditText) as EditText
                    val id =
                        name.text.toString() + "-" + FirebaseAuth.getInstance().currentUser?.email
                    val section = Section(id, name.text.toString())
                    addSection(section)
                }
                .setNegativeButton(R.string.cancel) { dialog, id ->
                    dialog.dismiss()
                }
            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }

    private fun addSection(section: Section) {
        if (!TextUtils.isEmpty(section.name) && activity is SectionCallBackListener) {
            (activity as SectionCallBackListener).onSectionAdded(section)
        }
    }

    interface SectionCallBackListener {
        fun onSectionAdded(section: Section)
    }
}