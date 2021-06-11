package com.master.musicroomclient.fragment

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.master.musicroomclient.R
import com.master.musicroomclient.model.Room
import com.master.musicroomclient.model.Song
import com.master.musicroomclient.utils.ApiUtils
import com.master.musicroomclient.utils.ApiUtils.gson
import com.master.musicroomclient.utils.ApiUtils.musicRoomStompClient
import com.master.musicroomclient.utils.Constants
import com.master.musicroomclient.utils.Constants.SERVER_HOST
import com.master.musicroomclient.utils.Constants.SERVER_STREAM_PORT
import com.master.musicroomclient.utils.SnackBarUtils
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.naiksoftware.stomp.dto.StompMessage
import java.time.Duration


class RoomPlayerFragment(private val roomCode: String) : Fragment()
//    , MediaPlayer.EventListener
{

    private val libVLC by lazy {
        LibVLC(activity, ArrayList<String>().apply {
//            add("--sout-keep")
//            add("--rtsp-tcp")
            add("-vvv")
        })
    }

    private val mediaPlayer by lazy { MediaPlayer(libVLC) }
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private lateinit var songNameText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var songCurrentTimeText: TextView
    private lateinit var songTotalTimeText: TextView
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPlayer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_room_player, container, false)

        songNameText = view.findViewById(R.id.song_name_text)
        songCurrentTimeText = view.findViewById(R.id.song_current_time_text)
        songTotalTimeText = view.findViewById(R.id.song_total_time_text)

        seekBar = view.findViewById(R.id.player_seek_bar)
        seekBar.setOnTouchListener { _, _ -> true }

        val addSongFileButton = view.findViewById<Button>(R.id.add_song_file_button)
        addSongFileButton.setOnClickListener {
            val getFileIntent = Intent(Intent.ACTION_GET_CONTENT)
            getFileIntent.type = "audio/*"
            startActivityForResult(getFileIntent, Constants.FILE_REQUEST_CODE)
        }
        connectToStreamTopic()

        val playButton = view.findViewById<Button>(R.id.play_button)
        playButton.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.play()
            }
        }

        return view
    }

    private fun connectToStreamTopic() {
        val topicDisposable = musicRoomStompClient.topic("/topic/room/${this.roomCode}/stream")
            .subscribe { topicMessage: StompMessage ->
                val nextSong = gson.fromJson(topicMessage.payload, Song::class.java)
                println("Got next song:${nextSong.name} with duration:${nextSong.duration}")
                requireActivity().runOnUiThread {
                    songNameText.text = nextSong.name
                    seekBar.progress = 0
                    seekBar.max = nextSong.duration.toInt()
                    songCurrentTimeText.text = formatDuration(0L)
                    songTotalTimeText.text = formatDuration(nextSong.duration)
                }
                handler.postDelayed(updateSongTime, 1000)
            }
        compositeDisposable.add(topicDisposable)
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
        compositeDisposable.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.FILE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            data?.data?.let { uri -> uploadFile(uri) }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(roomCode: String) = RoomPlayerFragment(roomCode)
    }

    private fun initPlayer() {
        val path = "rtsp://$SERVER_HOST:$SERVER_STREAM_PORT/$roomCode"
        val media = Media(libVLC, Uri.parse(path))
        mediaPlayer.media = media

        val powerManager = requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager
        val newWakeLock =
            powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "AppName:my tag")
        newWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    private val updateSongTime: Runnable = object : Runnable {
        override fun run() {
            val currentPosition = mediaPlayer.time
            println("Current player time:$currentPosition")
            seekBar.progress = currentPosition.toInt()
            songCurrentTimeText.text = formatDuration(currentPosition)
            handler.postDelayed(this, 1000)
        }
    }

//    override fun onEvent(event: MediaPlayer.Event) {
//        when (event.type) {ing -> Toast.makeText(activity, "Playing", Toast.LENGTH_SHORT)
////                .show()
//            MediaPlayer.Event.Play
//            MediaPlayer.Event.EncounteredError -> Toast.makeText(
//                activity,
//                "Error",
//                Toast.LENGTH_SHORT
//            ).show()
//            MediaPlayer.Event.Buffering -> Toast.makeText(activity, "Buffering", Toast.LENGTH_SHORT)
//                .show()
//        }
//    }

    private fun uploadFile(fileUri: Uri) {
        val fileName = getFileName(fileUri)
        val fileDuration = getFileDuration(fileUri)
        requireActivity().contentResolver.openInputStream(fileUri)?.use { inputStream ->
            val bytes = inputStream.readBytes()
            val fileRequest = RequestBody.create(MediaType.parse("audio/mpeg"), bytes)
            val filePart =
                MultipartBody.Part.createFormData("file", "", fileRequest) // TODO: check filename
            val namePart = RequestBody.create(MediaType.parse("text/plain"), fileName)
            val durationPart =
                RequestBody.create(MediaType.parse("text/plain"), fileDuration.toString())
            val roomCall = ApiUtils.musicRoomApi.uploadSong("123", filePart, namePart, durationPart)

            roomCall.enqueue(object : Callback<Room> {
                override fun onResponse(call: Call<Room>, response: Response<Room>) {
                    if (response.isSuccessful) {
//                        SnackBarUtils.showSnackBar(
//                            requireView().findViewById(android.R.id.content),
//                            "Success uploading file"
//                        )
                        Log.i("UPLOAD FILEE", "success!!!")
                        if (!mediaPlayer.isPlaying) {
                            mediaPlayer.play()
                        }
                    } else {
                        SnackBarUtils.showSnackBar(
                            requireView().findViewById(android.R.id.content),
                            "Error uploading file"
                        )
                    }
                }

                override fun onFailure(call: Call<Room>, t: Throwable) {
                    SnackBarUtils.showSnackBar(
                        requireView().findViewById(android.R.id.content),
                        "Error uploading file"
                    )
                }
            })
        } ?: SnackBarUtils.showSnackBar(
            requireView().findViewById(android.R.id.content),
            "Error opening the file"
        )
    }

    private fun getFileName(fileUri: Uri): String {
        return requireActivity().contentResolver.query(fileUri, null, null, null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                val fileName = cursor.getString(nameIndex)
                removeFileExtension(fileName)
            } ?: Constants.DEFAULT_FILE_NAME
    }

    private fun getFileDuration(fileUri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(requireContext(), fileUri)
        val fileDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        return fileDuration?.toLong() ?: Constants.DEFAULT_FILE_DURATION

    }

    private fun removeFileExtension(fileName: String): String {
        val extensionIndex = fileName.lastIndexOf(".")
        if (extensionIndex != -1) {
            return fileName.substring(0, extensionIndex)
        }
        return fileName
    }

    private fun formatDuration(millisecondsDuration: Long): String {
        val duration = Duration.ofMillis(millisecondsDuration)
        val minutes = duration.toMinutes()
        val seconds = duration.minusMinutes(minutes).seconds
        return "$minutes:$seconds"
    }
}