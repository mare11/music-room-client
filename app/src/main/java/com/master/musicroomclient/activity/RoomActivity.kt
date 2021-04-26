package com.master.musicroomclient.activity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.R
import com.master.musicroomclient.adapter.MessageListAdapter
import com.master.musicroomclient.model.Message
import com.master.musicroomclient.model.Room
import com.master.musicroomclient.utils.ApiUtils
import com.master.musicroomclient.utils.Constants.ROOM_CODE_EXTRA
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer.Event
import org.videolan.libvlc.MediaPlayer.EventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime


class RoomActivity : AppCompatActivity(), EventListener {

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
    private val messageListAdapter = MessageListAdapter()

    private val seekBar by lazy { findViewById<SeekBar>(R.id.player_seek_bar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        val messageView = findViewById<RecyclerView>(R.id.message_view)
        messageView.adapter = messageListAdapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        messageView.layoutManager = linearLayoutManager

        val roomName = findViewById<TextView>(R.id.room_name)

        val roomCode = intent.getStringExtra(ROOM_CODE_EXTRA)
                ?: // TODO: show popup and handle this correctly
                throw RuntimeException("null room code!")

        val roomCall = ApiUtils.musicRoomApi.getRoomByCode(roomCode)
        roomCall.enqueue(object : Callback<Room> {
            override fun onResponse(call: Call<Room>, response: Response<Room>) {
                if (response.isSuccessful) {
                    val room = response.body()
                    Log.i("CALL SUCCESS", room.toString())
                    roomName.text = room?.name

//                    initPlayer()

                } else {
                    Log.i(
                            "CALL FAILURE",
                            "Error code: ${response.code()}, " +
                                    "error message: ${response.message()}, " +
                                    "error body: ${response.errorBody().toString()}"
                    )
                }
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {
                Log.i("CALL FAILURE", t.message ?: "error")
            }
        })

        val sendMessageText = findViewById<EditText>(R.id.send_message_text)
        val sendMessageButton = findViewById<Button>(R.id.send_message_button)
        sendMessageButton.setOnClickListener {
            val sender = if (messageListAdapter.itemCount % 2 == 0) "ME" else "Jack"
            val message = Message(sendMessageText.text.toString(), sender, LocalDateTime.now())
            messageListAdapter.addMessage(message)
            sendMessageText.text.clear()
        }
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
}