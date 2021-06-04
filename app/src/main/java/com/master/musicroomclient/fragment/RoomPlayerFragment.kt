package com.master.musicroomclient.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.master.musicroomclient.R
import com.master.musicroomclient.utils.Constants
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class RoomPlayerFragment : Fragment(), MediaPlayer.EventListener {

    private val libVLC by lazy {
        LibVLC(activity, ArrayList<String>().apply {
            add("--sout-keep")
            add("--rtsp-tcp")
            add("-vvv")
        })
    }

    private val mediaPlayer by lazy { MediaPlayer(libVLC) }
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private lateinit var seekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPlayer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_room_player, container, false)

        seekBar = view.findViewById(R.id.player_seek_bar)
        handler.postDelayed(updateSongTime, 1000)

        return view
    }

    override fun onStart() {
        super.onStart()
        mediaPlayer.play()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.stop()
        handler.removeCallbacks(updateSongTime)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        libVLC.release()
        handler.removeCallbacks(updateSongTime)
    }

    companion object {
        @JvmStatic
        fun newInstance() = RoomPlayerFragment()
    }

    private fun initPlayer() {
        val path = "rtsp://${Constants.SERVER_HOST}:8554/abc"
//        val path = "rtsp://192.168.1.8:5555/demo"
        val media = Media(libVLC, Uri.parse(path))
        mediaPlayer.media = media

//        seekBar.max = mediaPlayer.length.toInt()
        val powerManager = requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager
        val newWakeLock =
            powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "AppName:my tag")
        newWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    private val updateSongTime: Runnable = object : Runnable {
        override fun run() {
            val currentPosition = mediaPlayer.time.toInt()
            seekBar.progress = currentPosition
            handler.postDelayed(this, 1000)
        }
    }

    override fun onEvent(event: MediaPlayer.Event) {
        when (event.type) {
            MediaPlayer.Event.Playing -> Toast.makeText(activity, "Playing", Toast.LENGTH_SHORT)
                .show()
            MediaPlayer.Event.EncounteredError -> Toast.makeText(
                activity,
                "Error",
                Toast.LENGTH_SHORT
            ).show()
            MediaPlayer.Event.Buffering -> Toast.makeText(activity, "Buffering", Toast.LENGTH_SHORT)
                .show()
        }
    }
}