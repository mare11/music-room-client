package com.master.musicroomclient.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.master.musicroomclient.R
import com.master.musicroomclient.adapter.TabAdapter
import com.master.musicroomclient.fragment.RoomPlayerFragment
import com.master.musicroomclient.model.Listener
import com.master.musicroomclient.model.RoomDetails
import com.master.musicroomclient.utils.ApiUtils.musicRoomApi
import com.master.musicroomclient.utils.Constants.ROOM_EXTRA
import com.master.musicroomclient.utils.Constants.USER_NAME_EXTRA
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RoomActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var userName: String
    private lateinit var roomDetails: RoomDetails
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        intent.getStringExtra(USER_NAME_EXTRA)?.also { userName = it }
        intent.getParcelableExtra<RoomDetails>(ROOM_EXTRA)?.also { roomDetails = it }

        if (this::userName.isInitialized && this::roomDetails.isInitialized) {
            val roomName = findViewById<TextView>(R.id.room_name_header)
            roomName.text = roomDetails.name

            supportFragmentManager.beginTransaction().replace(
                R.id.music_player_container,
                RoomPlayerFragment.newInstance(roomDetails.code)
            ).commit()

            val tabAdapter = TabAdapter(
                this@RoomActivity.roomDetails,
                this@RoomActivity.userName,
                this@RoomActivity,
                this@RoomActivity.supportFragmentManager
            )
            val viewPager = findViewById<ViewPager>(R.id.view_pager)
            val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
            viewPager.adapter = tabAdapter
            tabLayout.setupWithViewPager(viewPager)
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }


    override fun onDestroy() {
        if (this::roomDetails.isInitialized && this::userName.isInitialized) {
            val listener = Listener(this.userName)
            val roomCall = musicRoomApi.disconnectListener(this.roomDetails.code, listener)
            roomCall.enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    Log.i("ON DESTROZ", "SUCCESS!!")
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
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
}