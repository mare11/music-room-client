package com.master.musicroomclient.dialog

import android.app.Activity.RESULT_CANCELED
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.master.musicroomclient.R
import com.master.musicroomclient.model.Room
import com.master.musicroomclient.model.RoomDetails
import com.master.musicroomclient.utils.ApiUtils.musicRoomApi
import com.master.musicroomclient.utils.Constants.USER_NAME_PREFERENCE_KEY
import com.master.musicroomclient.utils.SnackBarUtils.showSnackBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection

class JoinRoomDialogFragment : DialogFragment() {

    private lateinit var dialogListener: JoinRoomDialogListener
    private lateinit var room: Room

    interface JoinRoomDialogListener {
        fun onJoinRoomDialogPositiveClose(userName: String, roomDetails: RoomDetails)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = requireActivity()
        if (activity is JoinRoomDialogListener) {
            dialogListener = activity
        } else {
            activity.setResult(RESULT_CANCELED)
            activity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            bundle.getParcelable<Room>(ARG_ROOM)?.also { room = it }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (!this::room.isInitialized) {
            throw IllegalStateException("No room data present")
        }
        return activity?.let { activityIt ->
            val dialog = AlertDialog.Builder(activityIt)
                .setView(R.layout.join_room_dialog_layout)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .create()
            val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activityIt)
            val userName = defaultSharedPreferences.getString(USER_NAME_PREFERENCE_KEY, "")
            dialog.setOnShowListener {

                val roomNameText = dialog.findViewById<TextView>(R.id.room_name_text)
                roomNameText.text = room.name

                val nameInput = dialog.findViewById<EditText>(R.id.join_room_name_text)
                nameInput.setText(userName)
                nameInput.setSelection(nameInput.text.length)
                // focus the field and show the keyboard
                nameInput.requestFocus()
                nameInput.postDelayed({
                    val inputManager =
                        activityIt.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.showSoftInput(nameInput, InputMethodManager.SHOW_IMPLICIT)
                }, 100)

                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setOnClickListener {
                    val name = nameInput.text.toString()
                    if (name.isNotBlank()) {
                        val roomCall = musicRoomApi.connectListener(room.code, name)
                        roomCall.enqueue(object : Callback<RoomDetails> {
                            override fun onResponse(
                                call: Call<RoomDetails>,
                                response: Response<RoomDetails>
                            ) {
                                val roomDetails = response.body()
                                if (response.isSuccessful && roomDetails != null) {
                                    dialog.dismiss()
                                    dialogListener.onJoinRoomDialogPositiveClose(name, roomDetails)
                                } else {
                                    val code = response.code()
                                    if (code == HttpURLConnection.HTTP_CONFLICT) {
                                        showSnackBar(
                                            activityIt.findViewById(android.R.id.content),
                                            "Name taken!"
                                        )
                                    } else {
                                        showSnackBar(
                                            activityIt.findViewById(android.R.id.content),
                                            "Error!"
                                        )
                                    }
                                }
                            }

                            override fun onFailure(call: Call<RoomDetails>, t: Throwable) {
                                showSnackBar(
                                    dialog.findViewById(android.R.id.content),
                                    "Error!"
                                )
                            }
                        })
                    } else {
                        nameInput.error = "Enter name"
                        nameInput.requestFocus()
                    }
                }

                val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                negativeButton.setOnClickListener {
                    dialog.dismiss()
                }
            }
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        private const val ARG_ROOM = "room"

        @JvmStatic
        fun newInstance(room: Room) =
            JoinRoomDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ROOM, room)
                }
            }
    }
}