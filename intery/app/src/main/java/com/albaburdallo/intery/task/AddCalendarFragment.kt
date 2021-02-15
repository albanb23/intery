package com.albaburdallo.intery.task

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.entities.Calendar
import com.google.firebase.firestore.FirebaseFirestore
import dev.sasikanth.colorsheet.ColorSheet
import kotlinx.android.synthetic.main.create_calendar.*


class AddCalendarFragment: DialogFragment() {

    companion object {
        private const val COLOR_SELECTED = "selectedColor"
        private const val NO_COLOR_OPTION = "noColorOption"
        fun newInstance(): AddCalendarFragment = AddCalendarFragment()
    }

    private val db = FirebaseFirestore.getInstance()
    private var selectedColor: Int = ColorSheet.NO_COLOR
    private var noColorOption = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val dialogview = requireActivity().layoutInflater.inflate(R.layout.create_calendar, null)
            val colors = this.resources.getIntArray(R.array.colors)
            selectedColor = savedInstanceState?.getInt(COLOR_SELECTED)?:colors.first()
            noColorOption = savedInstanceState?.getBoolean(NO_COLOR_OPTION)?:false

            val colorButton = dialogview?.findViewById(R.id.calendarColorPoint2) as ImageView
            colorButton.setOnClickListener {
                ColorSheet().cornerRadius(8)
                    .colorPicker(colors = colors,
                        noColorOption = noColorOption,
                        selectedColor = selectedColor,
                        listener = { color ->
                            selectedColor = color
                            colorButton.drawable.setTint(color)
                        })
                    .show(requireActivity().supportFragmentManager)
            }

            builder.setView(dialogview)
                .setPositiveButton(R.string.save, DialogInterface.OnClickListener { dialog, id ->
                    val titleText = dialogview?.findViewById(R.id.calendarNameEditText) as EditText
                    val descriptionText = dialogview?.findViewById(R.id.calendarDescriptionEditText) as EditText
                    val title = titleText.text.toString()
                    val description = descriptionText.text.toString()
                    val color = selectedColor.toString()

                    val calendar = Calendar(title, description, color)
                    //add calendar
                    addCalendar(calendar)
                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, it ->
                    //cancelar
                    dialog.dismiss()
                })
            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }

//    private fun colorNames(color: String): String? {
//        val res = null
//        when {
//            color == -"25950" -> {
//                res =
//            }
//        }
//        return res
//    }

    private fun addCalendar(calendar: Calendar){
        if (!TextUtils.isEmpty(calendar.name) && activity is CalendarCallbackListener) {
            (activity as CalendarCallbackListener)?.onCalendarAdded(calendar)
        }
    }

    interface CalendarCallbackListener {
        fun onCalendarAdded(calendar: Calendar)
    }
}