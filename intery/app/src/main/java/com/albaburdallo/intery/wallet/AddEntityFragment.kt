package com.albaburdallo.intery.wallet

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.albaburdallo.intery.R
import com.google.firebase.auth.FirebaseAuth

class AddEntityFragment: DialogFragment() {

    companion object {
        fun newInstance(): AddEntityFragment = AddEntityFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val dialogView = requireActivity().layoutInflater.inflate(R.layout.create_entity, null)
            builder.setView(dialogView)
                .setPositiveButton(R.string.save) { dialog, id ->
                    val entityName =
                        dialogView.findViewById<EditText>(R.id.entityNameEditText) as EditText
                    val id =
                        entityName.text.toString() + "-" + FirebaseAuth.getInstance().currentUser?.email
                    val entity = com.albaburdallo.intery.util.entities.Entity(
                        id,
                        entityName.text.toString()
                    )
                    addEntity(entity)
                }
                .setNegativeButton(R.string.cancel) { dialog, id ->
                    dialog.dismiss()
                }
            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }

    private fun addEntity(entity: com.albaburdallo.intery.util.entities.Entity) {
        if(!TextUtils.isEmpty(entity.name) && activity is EntityCallBackListener) {
            (activity as EntityCallBackListener).onEntityAdded(entity)
        }
    }

    interface EntityCallBackListener {
        fun onEntityAdded(entity: com.albaburdallo.intery.util.entities.Entity)
    }
}
