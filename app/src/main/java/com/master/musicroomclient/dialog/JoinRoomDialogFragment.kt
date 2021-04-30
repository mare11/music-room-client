package com.master.musicroomclient.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.master.musicroomclient.R

class JoinRoomDialogFragment(private val dialogListener: JoinRoomDialogListener) : DialogFragment() {

    interface JoinRoomDialogListener {
        fun onDialogPositiveClose(name: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activityIt ->
            val dialog = AlertDialog.Builder(activityIt)
                    .setView(R.layout.join_room_dialog_layout)
                    .setPositiveButton("OK", null)
                    .setNegativeButton("Cancel", null)
                    .create()
            dialog.setOnShowListener {
                val nameInput = dialog.findViewById<EditText>(R.id.join_room_name_text)
                // focus the field and show the keyboard
                nameInput.requestFocus()
                nameInput.postDelayed({
                    val inputManager = activityIt.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.showSoftInput(nameInput, InputMethodManager.SHOW_IMPLICIT)
                }, 100)

                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setOnClickListener {
                    val name = nameInput.text.toString()
                    if (name.isNotBlank()) {
                        dialog.dismiss()
                        dialogListener.onDialogPositiveClose(name)
                    } else {
                        nameInput.error = "Enter name"
                        nameInput.requestFocus()
                    }
                }

                val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                negativeButton.setOnClickListener {
                    dialog.dismiss()
                    activityIt.setResult(Activity.RESULT_OK)
                    activityIt.finish()
                }
            }
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}