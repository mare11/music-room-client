package com.master.musicroomclient

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.master.musicroomclient.activity.RoomActivity
import com.master.musicroomclient.adapter.UserRoomsAdapter
import com.master.musicroomclient.dialog.CreateRoomDialogFragment
import com.master.musicroomclient.dialog.CreateRoomDialogFragment.CreateRoomDialogListener
import com.master.musicroomclient.dialog.JoinRoomDialogFragment
import com.master.musicroomclient.dialog.JoinRoomDialogFragment.JoinRoomDialogListener
import com.master.musicroomclient.model.Room
import com.master.musicroomclient.model.RoomDetails
import com.master.musicroomclient.utils.ApiUtils.musicRoomApi
import com.master.musicroomclient.utils.ApiUtils.stompClientDelegate
import com.master.musicroomclient.utils.Constants.ROOM_EXTRA
import com.master.musicroomclient.utils.Constants.ROOM_REQUEST_CODE
import com.master.musicroomclient.utils.Constants.USER_NAME_EXTRA
import com.master.musicroomclient.utils.Constants.USER_NAME_PREFERENCE_KEY
import com.master.musicroomclient.utils.Constants.USER_ROOMS_PREFERENCE_KEY
import com.master.musicroomclient.utils.SnackBarUtils.showSnackBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.HashSet

class MainActivity : AppCompatActivity(), CreateRoomDialogListener, JoinRoomDialogListener,
    UserRoomsAdapter.OnItemClickListener {

    private lateinit var userRoomsAdapter: UserRoomsAdapter
    private lateinit var joinRoomInput: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        joinRoomInput = findViewById(R.id.join_room_input)
        val joinRoomLayout = findViewById<TextInputLayout>(R.id.join_room_layout)
        joinRoomLayout.setEndIconOnClickListener {
            val roomCode = joinRoomInput.text.toString()
            if (roomCode.isNotBlank()) {
                getRoomAndShowJoinRoomDialog(roomCode)
            } else {
                joinRoomInput.requestFocus()
            }
        }

        userRoomsAdapter = UserRoomsAdapter(this@MainActivity, mutableListOf())

        val userRoomsView = findViewById<RecyclerView>(R.id.user_rooms_view)
        userRoomsView.layoutManager = LinearLayoutManager(this)
        userRoomsView.adapter = userRoomsAdapter
    }

    override fun onResume() {
        super.onResume()
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val userRooms = preferences.getStringSet(USER_ROOMS_PREFERENCE_KEY, HashSet<String>())

        if (userRooms != null && userRooms.isNotEmpty()) {
            val roomCall = musicRoomApi.getRoomsByCodes(userRooms.toList())
            roomCall.enqueue(object : Callback<List<Room>> {
                override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {
                    val rooms = response.body()
                    if (response.isSuccessful && rooms != null) {
                        userRoomsAdapter.setRooms(rooms)
                    } else {
                        showSnackBar(findViewById(android.R.id.content), "Error")
                    }
                }

                override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                    showSnackBar(findViewById(android.R.id.content), "Error")
                    R.attr.colorPrimary
                }

            })
        }
    }

    override fun onCreateRoomDialogPositiveClose(room: Room) {
        showJoinRoomDialog(room)
    }

    override fun onJoinRoomDialogPositiveClose(userName: String, roomDetails: RoomDetails) {
        Toast.makeText(this, "Name from dialog: $userName", Toast.LENGTH_SHORT).show()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val userRooms = preferences.getStringSet(USER_ROOMS_PREFERENCE_KEY, null)
            ?: emptySet()
        val copyOfUserRooms = userRooms.toMutableSet()
        copyOfUserRooms.add(roomDetails.code)
        preferences.edit()
            .putStringSet(USER_ROOMS_PREFERENCE_KEY, copyOfUserRooms)
            .putString(USER_NAME_PREFERENCE_KEY, userName)
            .apply()

        startRoomActivity(userName, roomDetails)
    }

    override fun onItemClick(room: Room) {
        showJoinRoomDialog(room)
    }

    override fun onItemDeleted(room: Room) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val userRooms = preferences.getStringSet(USER_ROOMS_PREFERENCE_KEY, null) ?: emptySet()
        val copyOfUserRooms = userRooms.toMutableSet()
        copyOfUserRooms.remove(room.code)
        preferences.edit()
            .putStringSet(USER_ROOMS_PREFERENCE_KEY, copyOfUserRooms).apply()

        userRoomsAdapter.removeRoom(room)

        showSnackBar(
            findViewById(android.R.id.content),
            "Room '${room.name}' removed from favorites"
        )
    }

    private fun showJoinRoomDialog(room: Room) {
        val roomNameDialogFragment = JoinRoomDialogFragment.newInstance(room)
        roomNameDialogFragment.isCancelable = false
        roomNameDialogFragment.show(supportFragmentManager, "join_room")
    }

    private fun getRoomAndShowJoinRoomDialog(roomCode: String) {
        val roomCall = musicRoomApi.getRoomByCode(roomCode)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_create_room -> {
                val createRoomDialogFragment = CreateRoomDialogFragment(this)
                createRoomDialogFragment.isCancelable = false
                createRoomDialogFragment.show(supportFragmentManager, "create_room")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // TODO: check can this be done on some application shutdown event
    override fun onDestroy() {
        super.onDestroy()
        with(stompClientDelegate) {
            if (isInitialized()) {
                value.disconnect()
            }
        }
    }
}