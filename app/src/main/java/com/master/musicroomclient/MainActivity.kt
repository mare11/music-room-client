package com.master.musicroomclient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.activity.RoomActivity
import com.master.musicroomclient.adapter.UserRoomsAdapter
import com.master.musicroomclient.dialog.CreateRoomDialogFragment
import com.master.musicroomclient.dialog.CreateRoomDialogFragment.CreateRoomDialogListener
import com.master.musicroomclient.utils.ApiUtils
import com.master.musicroomclient.utils.Constants.ROOM_CODE_EXTRA
import com.master.musicroomclient.utils.Constants.ROOM_REQUEST_CODE
import com.master.musicroomclient.utils.Constants.USER_ROOMS_PREFERENCE_KEY
import com.master.musicroomclient.utils.SnackBarUtils.showSnackBar

class MainActivity : AppCompatActivity(), CreateRoomDialogListener,
    UserRoomsAdapter.OnItemClickListener {

    private lateinit var userRoomsAdapter: UserRoomsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val roomCodeText = findViewById<EditText>(R.id.join_room_code_text)
        val createRoomButton = findViewById<Button>(R.id.create_room_button)

        createRoomButton.setOnClickListener {
            val createRoomDialogFragment = CreateRoomDialogFragment(this)
            createRoomDialogFragment.isCancelable = false
            createRoomDialogFragment.show(supportFragmentManager, "create_room")
        }

        val joinRoomButton = findViewById<Button>(R.id.join_room_button)
        joinRoomButton.setOnClickListener {
            val roomCode = roomCodeText.text.toString()
            if (roomCode.isNotBlank()) {
                startRoomActivity(roomCode)
            } else {
                roomCodeText.error = "Enter room code"
                roomCodeText.requestFocus()
            }
        }

        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val userRooms =
            defaultSharedPreferences.getStringSet(USER_ROOMS_PREFERENCE_KEY, HashSet<String>())
        val userRoomsView = findViewById<RecyclerView>(R.id.user_rooms_view)
        userRoomsView.layoutManager = LinearLayoutManager(this)
        if (userRooms != null) {
            userRoomsAdapter = UserRoomsAdapter(this, userRooms.toList())
            userRoomsView.adapter = userRoomsAdapter
        }
    }

    override fun onDialogPositiveClose(code: String) {
        startRoomActivity(code)
    }


    override fun onItemClick(position: Int) {
        val roomCode = userRoomsAdapter.getRooms()[position]
        startRoomActivity(roomCode)
    }

    private fun startRoomActivity(code: String) {
        val roomActivityIntent = Intent(this, RoomActivity::class.java)
        roomActivityIntent.putExtra(ROOM_CODE_EXTRA, code)
        startActivityForResult(roomActivityIntent, ROOM_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ROOM_REQUEST_CODE && resultCode != RESULT_OK) {
            showSnackBar(findViewById(android.R.id.content), "Error")
        }
    }

    // TODO: check can this be done on some application shutdown event
    override fun onDestroy() {
        super.onDestroy()
        with(ApiUtils.stompClientDelegate) {
            if (isInitialized()) {
                value.disconnect()
            }
        }
    }
}