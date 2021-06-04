package com.master.musicroomclient.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.master.musicroomclient.R
import com.master.musicroomclient.adapter.TabAdapter
import com.master.musicroomclient.dialog.JoinRoomDialogFragment
import com.master.musicroomclient.dialog.JoinRoomDialogFragment.JoinRoomDialogListener
import com.master.musicroomclient.fragment.RoomPlayerFragment
import com.master.musicroomclient.model.Listener
import com.master.musicroomclient.model.Room
import com.master.musicroomclient.utils.ApiUtils
import com.master.musicroomclient.utils.Constants.ROOM_CODE_EXTRA
import com.master.musicroomclient.utils.Constants.USER_NAME_PREFERENCE_KEY
import com.master.musicroomclient.utils.Constants.USER_ROOMS_PREFERENCE_KEY
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection


class RoomActivity : AppCompatActivity(), JoinRoomDialogListener {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var userName: String
    private lateinit var room: Room
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        val roomName = findViewById<TextView>(R.id.room_name)
        val roomCode = intent.getStringExtra(ROOM_CODE_EXTRA)
            ?: // TODO: show popup and handle this correctly
            throw RuntimeException("null room code!")

        val roomCall = ApiUtils.musicRoomApi.getRoomByCode(roomCode)
        roomCall.enqueue(object : Callback<Room> {
            override fun onResponse(call: Call<Room>, response: Response<Room>) {
                val room = response.body()
                if (response.isSuccessful && room != null) {
                    this@RoomActivity.room = room
                    roomName.text = room.name

                    val roomNameDialogFragment = JoinRoomDialogFragment(this@RoomActivity)
                    roomNameDialogFragment.isCancelable = false
                    roomNameDialogFragment.show(supportFragmentManager, "join_room")
                } else {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {
                setResult(RESULT_CANCELED)
                finish()
            }
        })
    }

    override fun onDestroy() {
        if (this::userName.isInitialized) {
            val listener = Listener(this.userName)
            val roomCall =
                ApiUtils.musicRoomApi.disconnectListener(this.room.code!!, listener) // FIXME
            roomCall.enqueue(object : Callback<Room> {
                override fun onResponse(call: Call<Room>, response: Response<Room>) {
                    Log.i("ON DESTROZ", "SUCCESS!!")
                }

                override fun onFailure(call: Call<Room>, t: Throwable) {
                    Log.i("ON DESTROZ", "ERROR!!")
                }

            })
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            setResult(RESULT_OK)
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Click BACK again to exit the room", Toast.LENGTH_SHORT).show()
        handler.postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    override fun onDialogPositiveClose(name: String) {
        Toast.makeText(this, "Name from dialog: $name", Toast.LENGTH_SHORT).show()
        this.userName = name

        val listener = Listener(this.userName)
        val roomCall = ApiUtils.musicRoomApi.connectListener(this.room.code!!, listener) // FIXME
        roomCall.enqueue(object : Callback<Room> {
            override fun onResponse(call: Call<Room>, response: Response<Room>) {
                val room = response.body()
                if (response.isSuccessful && room != null) {
                    this@RoomActivity.room = room

                    supportFragmentManager.beginTransaction().replace(
                        R.id.music_player_container,
                        RoomPlayerFragment.newInstance()
                    ).commit()

                    val tabAdapter = TabAdapter(
                        this@RoomActivity,
                        this@RoomActivity.supportFragmentManager,
                        this@RoomActivity.room,
                        this@RoomActivity.userName
                    )
                    val viewPager = findViewById<ViewPager>(R.id.view_pager)
                    val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
                    runOnUiThread {
                        viewPager.adapter = tabAdapter
                        tabLayout.setupWithViewPager(viewPager)
                    }

                    val defaultSharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(this@RoomActivity)
                    val userRooms = HashSet(
                        defaultSharedPreferences.getStringSet(
                            USER_ROOMS_PREFERENCE_KEY,
                            HashSet<String>()
                        )
                    )
                    userRooms.add(this@RoomActivity.room.code)
                    defaultSharedPreferences.edit()
                        .putStringSet(USER_ROOMS_PREFERENCE_KEY, userRooms).apply()
                    defaultSharedPreferences.edit()
                        .putString(USER_NAME_PREFERENCE_KEY, this@RoomActivity.userName).apply()

                } else {
                    val code = response.code()
                    if (code == HttpURLConnection.HTTP_CONFLICT) {
                        runOnUiThread {
                            Toast.makeText(this@RoomActivity, "Name taken!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {
                setResult(RESULT_CANCELED)
                finish()
            }
        })
    }
}