package com.master.musicroomclient.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.master.musicroomclient.R
import com.master.musicroomclient.adapter.TabAdapter
import com.master.musicroomclient.fragment.RoomPlayerFragment
import com.master.musicroomclient.model.RoomDetails
import com.master.musicroomclient.utils.ApiUtils.musicRoomApi
import com.master.musicroomclient.utils.Constants.ROOM_EXTRA
import com.master.musicroomclient.utils.Constants.USER_NAME_EXTRA
import com.master.musicroomclient.utils.TabLayoutBadgeListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RoomActivity
    : AppCompatActivity(), TabLayoutBadgeListener {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
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
                RoomPlayerFragment.newInstance(roomDetails.code, roomDetails.currentSong, userName)
            ).commit()

            val tabAdapter = TabAdapter(
                this@RoomActivity.roomDetails,
                this@RoomActivity.userName,
                this@RoomActivity,
                this@RoomActivity.supportFragmentManager
            )
            viewPager = findViewById<ViewPager>(R.id.view_pager)
            viewPager.adapter = tabAdapter
            viewPager.offscreenPageLimit = 2
            tabLayout = findViewById<TabLayout>(R.id.tab_layout)
            tabLayout.setupWithViewPager(viewPager)
            // TODO: setup icons and badges for tabs
            tabLayout.getTabAt(0)?.icon =
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_people_on_primary_24)
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }


    override fun onDestroy() {
        if (this::roomDetails.isInitialized && this::userName.isInitialized) {
            val roomCall = musicRoomApi.disconnectListener(this.roomDetails.code, this.userName)
            roomCall.enqueue(object : Callback<Void> {

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.i("ON DESTROZ", "SUCCESS!!")
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
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
        Toast.makeText(this, getString(R.string.double_back_press), Toast.LENGTH_SHORT).show()
        handler.postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    override fun onTabEvent(tabIndex: Int) {
        // when current tab is not the same as one triggering the event
        if (viewPager.currentItem != tabIndex) {
            // increment badge number and show badge if it is hidden
            tabLayout.getTabAt(tabIndex)?.orCreateBadge?.let {
                it.number++
                it.isVisible = true
            }
        }
    }

    override fun onTabResume(tabIndex: Int) {
        // reset badge number and hide badge
        tabLayout.getTabAt(tabIndex)?.badge?.let {
            it.number = 0
            it.isVisible = false
        }
    }
}