package com.master.musicroomclient.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class RoomNameDialogFragment(private val dialogListener: RoomNameDialogListener) : DialogFragment() {

    interface RoomNameDialogListener {
        fun onDialogPositiveClose(name: String)
    }

    override
    fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val nameInput = EditText(it)
            nameInput.inputType = InputType.TYPE_CLASS_TEXT
            
            val dialog = AlertDialog.Builder(it)
                    .setMessage("Choose name")
                    .setView(nameInput)
                    .setPositiveButton("OK") { _, _ ->
                        dialogListener.onDialogPositiveClose(nameInput.text.toString())
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                        Toast.makeText(it, "Canceled", Toast.LENGTH_SHORT).show()
                    }
                    .create()
            dialog.setOnShowListener { // TODO: fix, show keyboard on and focus field
                nameInput.isFocusableInTouchMode = true
                nameInput.requestFocus()
            }
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}