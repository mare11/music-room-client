package com.master.musicroomclient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.activity.RoomActivity
import com.master.musicroomclient.adapter.UserRoomsAdapter
import com.master.musicroomclient.dialog.CreateRoomDialogFragment
import com.master.musicroomclient.dialog.CreateRoomDialogFragment.CreateRoomDialogListener
import com.master.musicroomclient.dialog.JoinRoomDialogFragment
import com.master.musicroomclient.dialog.JoinRoomDialogFragment.JoinRoomDialogListener
import com.master.musicroomclient.model.Room
import com.master.musicroomclient.model.RoomDetails
import com.master.musicroomclient.utils.ApiUtils
import com.master.musicroomclient.utils.Constants
import com.master.musicroomclient.utils.Constants.ROOM_EXTRA
import com.master.musicroomclient.utils.Constants.ROOM_REQUEST_CODE
import com.master.musicroomclient.utils.Constants.USER_NAME_EXTRA
import com.master.musicroomclient.utils.Constants.USER_ROOMS_PREFERENCE_KEY
import com.master.musicroomclient.utils.SnackBarUtils.showSnackBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), CreateRoomDialogListener, JoinRoomDialogListener,
    UserRoomsAdapter.OnItemClickListener {

    private lateinit var userRoomsAdapter: UserRoomsAdapter

    // TODO: extract some of this code to onCreateView
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
                getRoomAndShowJoinRoomDialog(roomCode)
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

    override fun onCreateRoomDialogPositiveClose(room: Room) {
        showJoinRoomDialog(room)
    }

    override fun onJoinRoomDialogPositiveClose(userName: String, roomDetails: RoomDetails) {
        Toast.makeText(this, "Name from dialog: $userName", Toast.LENGTH_SHORT).show()

        val defaultSharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)
        val userRooms = HashSet(
            defaultSharedPreferences.getStringSet(
                USER_ROOMS_PREFERENCE_KEY,
                HashSet<String>()
            )
        )
        userRooms.add(roomDetails.code)
        defaultSharedPreferences.edit()
            .putStringSet(USER_ROOMS_PREFERENCE_KEY, userRooms).apply()
        defaultSharedPreferences.edit()
            .putString(Constants.USER_NAME_PREFERENCE_KEY, userName).apply()

        startRoomActivity(userName, roomDetails)
    }

    override fun onItemClick(position: Int) {
        val roomCode = userRoomsAdapter.getRooms()[position]
        getRoomAndShowJoinRoomDialog(roomCode)
    }

    private fun showJoinRoomDialog(room: Room) {
        val roomNameDialogFragment = JoinRoomDialogFragment.newInstance(room)
        roomNameDialogFragment.isCancelable = false
        roomNameDialogFragment.show(supportFragmentManager, "join_room")
    }

    private fun getRoomAndShowJoinRoomDialog(roomCode: String) {
        val roomCall = ApiUtils.musicRoomApi.getRoomByCode(roomCode)
        roomCall.enqueue(object : Callback<Room> {
            override fun onResponse(call: Call<Room>, response: Response<Room>) {
                val room = response.body()
                if (response.isSuccessful && room != null) {
                    showJoinRoomDialog(room)
                } else {
                    showSnackBar(findViewById(android.R.id.content), "Error")
                }
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {
                showSnackBar(findViewById(android.R.id.content), "Error")
            }
        })
    }

    private fun startRoomActivity(userName: String, roomDetails: RoomDetails) {
        val roomActivityIntent = Intent(this, RoomActivity::class.java)
        roomActivityIntent.putExtra(USER_NAME_EXTRA, userName)
        roomActivityIntent.putExtra(ROOM_EXTRA, roomDetails)
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