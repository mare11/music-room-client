package com.master.musicroomclient.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.master.musicroomclient.R
import com.master.musicroomclient.model.Room
import com.master.musicroomclient.model.RoomRequest
import com.master.musicroomclient.utils.ApiUtils
import com.master.musicroomclient.utils.SnackBarUtils.showSnackBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CreateRoomDialogFragment(private val dialogListener: CreateRoomDialogListener) :
    DialogFragment() {

    interface CreateRoomDialogListener {
        fun onCreateRoomDialogPositiveClose(room: Room)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let { activity ->
            val dialog = AlertDialog.Builder(activity)
                .setView(R.layout.create_room_dialog_layout)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .create()
            dialog.setOnShowListener {
                val roomNameInput = dialog.findViewById<EditText>(R.id.create_room_name_text)
                // focus the field and show the keyboard
                roomNameInput.requestFocus()
                roomNameInput.postDelayed({
                    val inputManager =
                        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.showSoftInput(roomNameInput, InputMethodManager.SHOW_IMPLICIT)
                }, 100)

                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setOnClickListener {
                    val roomName = roomNameInput.text.toString().trim()
                    if (roomName.isNotBlank()) {
                        val newRoom = RoomRequest(roomName)
                        val createRoomCall = ApiUtils.musicRoomApi.createRoom(newRoom)

                        createRoomCall.enqueue(object : Callback<Room> {
                            override fun onResponse(call: Call<Room>, response: Response<Room>) {
                                val room = response.body()
                                if (response.isSuccessful && room != null) {
                                    dialog.dismiss()
                                    dialogListener.onCreateRoomDialogPositiveClose(room)
                                } else {
                                    showSnackBar(
                                        dialog.findViewById(android.R.id.content),
                                        "Error!"
                                    )
                                }
                            }

                            override fun onFailure(call: Call<Room>, t: Throwable) {
                                showSnackBar(dialog.findViewById(android.R.id.content), "Error!")
                            }
                        })
                    } else {
                        roomNameInput.error = "Enter room name"
                        roomNameInput.requestFocus()
                    }
                }
            }

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}