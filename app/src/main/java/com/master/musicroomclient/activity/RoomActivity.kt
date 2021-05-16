package com.master.musicroomclient.activity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
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
import com.master.musicroomclient.model.Listener
import com.master.musicroomclient.model.Room
import com.master.musicroomclient.utils.ApiUtils
import com.master.musicroomclient.utils.Constants.ROOM_CODE_EXTRA
import com.master.musicroomclient.utils.Constants.USER_NAME_PREFERENCE_KEY
import com.master.musicroomclient.utils.Constants.USER_ROOMS_PREFERENCE_KEY
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer.Event
import org.videolan.libvlc.MediaPlayer.EventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection


class RoomActivity : AppCompatActivity(), EventListener, JoinRoomDialogListener {

    private val libVLC by lazy {
        LibVLC(this, ArrayList<String>().apply {
            add("--no-drop-late-frames")
            add("--no-skip-frames")
//            add("--sout-keep")
            add("--rtsp-tcp")
            add("-vvv")
        })
    }
    private val mediaPlayer by lazy {
        org.videolan.libvlc.MediaPlayer(libVLC)
    }
    private val handler = Handler()
    private val seekBar by lazy { findViewById<SeekBar>(R.id.player_seek_bar) }
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
//                    initPlayer()
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

    private fun initPlayer() {
//        val libVLC = LibVLC(this, ArrayList<String>().apply {
//            add("--no-drop-late-frames")
//            add("--no-skip-frames")
//            add("--rtsp-tcp")
//            add("-vvv")
//        })
//        val mediaPlayer = org.videolan.libvlc.MediaPlayer(libVLC)
//        val path = "rtsp://192.168.1.9:5555/demo"
        val path = "rtsp://192.168.1.9:8554/abc"
        Media(libVLC, Uri.parse(path)).apply {
            setHWDecoderEnabled(true, false)
            addOption(":network-caching=150")
            addOption(":clock-jitter=0")
            addOption(":clock-synchro=0")
            mediaPlayer.media = this

        }.release()

        mediaPlayer.play()
//        seekBar.max = mediaPlayer.length.toInt()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val newWakeLock =
            powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "AppName:my tag")
        newWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
        handler.postDelayed(updateSongTime, 1000)
    }

    private val updateSongTime: Runnable = object : Runnable {
        override fun run() {
            val currentPosition = mediaPlayer.time.toInt()
            seekBar.progress = currentPosition
            handler.postDelayed(this, 1000)
        }
    }

    override fun onEvent(event: Event) {
        when (event.type) {
            Event.Playing -> Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show()
            Event.EncounteredError -> Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            Event.Buffering -> Toast.makeText(this, "Buffering", Toast.LENGTH_SHORT).show()
        }
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
                    val userRooms =
                        defaultSharedPreferences.getStringSet(
                            USER_ROOMS_PREFERENCE_KEY,
                            HashSet<String>()
                        )
                    userRooms?.add(this@RoomActivity.room.code)
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