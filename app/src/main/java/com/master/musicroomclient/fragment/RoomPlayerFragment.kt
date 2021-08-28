package com.master.musicroomclient.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.fragment.app.Fragment
import com.master.musicroomclient.R
import com.master.musicroomclient.model.CurrentSong
import com.master.musicroomclient.model.RoomDetails
import com.master.musicroomclient.model.Song
import com.master.musicroomclient.utils.ApiUtils.gson
import com.master.musicroomclient.utils.ApiUtils.musicRoomApi
import com.master.musicroomclient.utils.ApiUtils.musicRoomStompClient
import com.master.musicroomclient.utils.Constants
import com.master.musicroomclient.utils.Constants.ARG_CURRENT_SONG
import com.master.musicroomclient.utils.Constants.ARG_ROOM_CODE
import com.master.musicroomclient.utils.Constants.ARG_USER_NAME
import com.master.musicroomclient.utils.Constants.ROOM_CODE
import com.master.musicroomclient.utils.Constants.SERVER_HOST
import com.master.musicroomclient.utils.Constants.SERVER_STREAM_PORT
import com.master.musicroomclient.utils.Constants.formatDurationToMinutesAndSeconds
import com.master.musicroomclient.utils.SnackBarUtils.showSnackBar
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


class RoomPlayerFragment : Fragment(), MediaPlayer.EventListener {

    private val libVLC by lazy {
        LibVLC(activity, ArrayList<String>().apply {
            add("-vvv")
        })
    }
    private val mediaPlayer by lazy { MediaPlayer(libVLC) }

    private lateinit var roomCode: String
    private lateinit var userName: String
    private var currentSong: CurrentSong? = null
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private lateinit var songNameText: TextView
    private lateinit var songUploaderText: TextView
    private lateinit var songUploaderPrefixText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var songCurrentTimeText: TextView
    private lateinit var songTotalTimeText: TextView
    private lateinit var addSongButton: Button
    private lateinit var addSongProgressBar: ProgressBar
    private lateinit var nextSongButton: Button
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            bundle.getString(ARG_ROOM_CODE)?.also {
                roomCode = it
                initPlayer()
                connectToSongTopic()
            }
            bundle.getParcelable<CurrentSong>(ARG_CURRENT_SONG)?.also { currentSong = it }
            bundle.getString(ARG_USER_NAME)?.also { userName = it }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_room_player, container, false)

        if (!this::roomCode.isInitialized || !this::userName.isInitialized) {
            showSnackBar(view, "View could not be loaded")
            return view
        }

        songNameText = view.findViewById(R.id.song_name_text)
        songUploaderText = view.findViewById(R.id.song_uploader_text)
        songUploaderPrefixText = view.findViewById(R.id.song_uploader_prefix)
        songCurrentTimeText = view.findViewById(R.id.song_current_time_text)
        songTotalTimeText = view.findViewById(R.id.song_total_time_text)

        seekBar = view.findViewById(R.id.player_seek_bar)
        seekBar.setOnTouchListener { _, _ -> true } // FIXME

        addSongButton = view.findViewById(R.id.add_song_button)
        addSongButton.setOnClickListener {
            val getFileIntent = Intent(Intent.ACTION_GET_CONTENT)
            getFileIntent.type = "audio/*"
            startActivityForResult(getFileIntent, Constants.FILE_REQUEST_CODE)
        }
        addSongProgressBar = view.findViewById<ProgressBar>(R.id.add_song_progress_bar)

        val copyRoomCodeButton = view.findViewById<Button>(R.id.copy_room_code_button)
        copyRoomCodeButton.setOnClickListener {
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(ROOM_CODE, roomCode)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), getString(R.string.room_code_copy), Toast.LENGTH_SHORT)
                .show()
        }

        nextSongButton = view.findViewById<Button>(R.id.next_song_button)
        nextSongButton.isEnabled = false
        nextSongButton.setOnClickListener {
            val roomCall = musicRoomApi.nextSong(roomCode)
            roomCall.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.i("NEXT_SONG", "Success!!")
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.i("NEXT_SONG", "Error!!")
                }
            })
        }

        currentSong?.let {
            Log.i(
                "PLAYER FRAGMENT",
                "Found current song with name: ${it.song.name} and elapsed duration: ${it.elapsedDuration}"
            )
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.play()
            }
            songNameText.text = it.song.name
            songUploaderText.text = it.song.uploader
            songUploaderPrefixText.visibility = View.VISIBLE
            seekBar.max = it.song.duration.toInt()
            seekBar.progress = it.elapsedDuration.toInt()
            songCurrentTimeText.text = formatDurationToMinutesAndSeconds(0L)
            songTotalTimeText.text = formatDurationToMinutesAndSeconds(it.song.duration)
            nextSongButton.isEnabled = it.song.uploader == userName
            handler.postDelayed(updateSongTime, 1000)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        currentSong?.also {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.play()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        if (currentSong == null) {
            handler.removeCallbacks(updateSongTime)
        }
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
        if (requestCode == Constants.FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri -> uploadFile(uri) }
        }
    }

    private fun initPlayer() {
        val path = "rtsp://$SERVER_HOST:$SERVER_STREAM_PORT/$roomCode"
        val media = Media(libVLC, Uri.parse(path))
        mediaPlayer.media = media
        mediaPlayer.setEventListener(this)
    }

    private fun connectToSongTopic() {
        val topicDisposable = musicRoomStompClient.topic("/topic/room/$roomCode/song/next")
            .subscribe { topicMessage: StompMessage ->
                val nextSong = gson.fromJson(topicMessage.payload, Song::class.java)
                println("Got next song:${nextSong.name} with duration:${nextSong.duration}")
                currentSong = CurrentSong(nextSong, 0L)
                if (!mediaPlayer.isPlaying) {
                    Log.i("NEXT_SONG_PLAYER", "Start playing")
                    initPlayer()
                    mediaPlayer.play()
                    handler.postDelayed(updateSongTime, 1000)
                }
                requireActivity().runOnUiThread {
                    songNameText.text = nextSong.name
                    songUploaderText.text = nextSong.uploader
                    songUploaderPrefixText.visibility = View.VISIBLE
                    seekBar.max = nextSong.duration.toInt()
                    seekBar.progress = 0
                    songCurrentTimeText.text = formatDurationToMinutesAndSeconds(0L)
                    songTotalTimeText.text =
                        formatDurationToMinutesAndSeconds(nextSong.duration)
                    nextSongButton.isEnabled = nextSong.uploader == userName
                }
            }
        compositeDisposable.add(topicDisposable)
    }

    private val updateSongTime: Runnable = object : Runnable {
        override fun run() {
            seekBar.progress += 1000
            songCurrentTimeText.text =
                formatDurationToMinutesAndSeconds(seekBar.progress.toLong())
            handler.postDelayed(this, 1000)
        }
    }

    override fun onEvent(event: MediaPlayer.Event) {
        when (event.type) {
            MediaPlayer.Event.EndReached -> {
                Log.i("LIBVLC_EVENTS", "EndReached")
                songNameText.text = ""
                songUploaderText.text = ""
                songUploaderPrefixText.visibility = View.GONE
                seekBar.max = 0
                seekBar.progress = 0
                songCurrentTimeText.text = ""
                songTotalTimeText.text = ""
                nextSongButton.isEnabled = false
                handler.removeCallbacks(updateSongTime)
            }
        }
    }

    private fun uploadFile(fileUri: Uri) {
        val fileName = getFileName(fileUri)
        val fileDuration = getFileDuration(fileUri)
        requireActivity().contentResolver.openInputStream(fileUri)?.use { inputStream ->
            val bytes = inputStream.readBytes()
            val fileRequest = RequestBody.create(MediaType.parse("audio/mpeg"), bytes)
            val filePart =
                MultipartBody.Part.createFormData(
                    "file",
                    "",
                    fileRequest
                ) // filename is empty because it's sent as additional parameter
            val namePart = RequestBody.create(MediaType.parse("text/plain"), fileName)
            val durationPart =
                RequestBody.create(MediaType.parse("text/plain"), fileDuration.toString())
            val uploaderPart =
                RequestBody.create(MediaType.parse("text/plain"), userName)
            val roomCall = musicRoomApi.uploadSong(
                roomCode,
                filePart,
                namePart,
                durationPart,
                uploaderPart
            )

            addSongButton.isEnabled = false
            addSongProgressBar.visibility = View.VISIBLE
            roomCall.enqueue(object : Callback<RoomDetails> {
                override fun onResponse(
                    call: Call<RoomDetails>,
                    response: Response<RoomDetails>
                ) {
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
                        showSnackBar(
                            requireView().findViewById(android.R.id.content),
                            "Error uploading file"
                        )
                    }
                    addSongButton.isEnabled = true
                    addSongProgressBar.visibility = View.GONE
                }

                override fun onFailure(call: Call<RoomDetails>, t: Throwable) {
                    showSnackBar(
                        requireView().findViewById(android.R.id.content), "Error uploading file"
                    )
                    addSongButton.isEnabled = true
                    addSongProgressBar.visibility = View.GONE
                }
            })
        } ?: showSnackBar(
            requireView().findViewById(android.R.id.content), "Error opening the file"
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
        val fileDuration =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
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

    companion object {
        @JvmStatic
        fun newInstance(roomCode: String, currentSong: CurrentSong?, userName: String) =
            RoomPlayerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ROOM_CODE, roomCode)
                    putParcelable(ARG_CURRENT_SONG, currentSong)
                    putString(ARG_USER_NAME, userName)
                }
            }
    }
}