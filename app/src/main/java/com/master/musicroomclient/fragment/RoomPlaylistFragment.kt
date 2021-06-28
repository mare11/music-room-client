package com.master.musicroomclient.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.R
import com.master.musicroomclient.adapter.SongListAdapter
import com.master.musicroomclient.model.Song
import com.master.musicroomclient.utils.ApiUtils.gson
import com.master.musicroomclient.utils.ApiUtils.musicRoomStompClient
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.dto.StompMessage

class RoomPlaylistFragment : Fragment() {

    private lateinit var roomCode: String
    private lateinit var playlist: List<Song>
    private lateinit var adapter: SongListAdapter
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            bundle.getString(ARG_ROOM_CODE)?.also { roomCode = it }
            bundle.getParcelableArrayList<Song>(ARG_PLAYLIST)?.also { playlist = it }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_room_playlist, container, false)

        if (!this::roomCode.isInitialized || !this::playlist.isInitialized) {
            // TODO: show some error message
            return view
        }
        adapter = SongListAdapter(playlist.toMutableList())
        if (view is RecyclerView && this::playlist.isInitialized) {
            with(view) {
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = this@RoomPlaylistFragment.adapter
            }
        }
        connectToSongTopics()
        return view
    }

    private fun connectToSongTopics() {
        val addSongTopicDisposable = musicRoomStompClient.topic("/topic/room/$roomCode/song/add")
            .subscribe { topicMessage: StompMessage ->
                val newSong = gson.fromJson(topicMessage.payload, Song::class.java)
                requireActivity().runOnUiThread {
                    adapter.addSong(newSong)
                    println("Song '${newSong.name}' added to playlist by '${newSong.uploader}'!")
                }
            }
        compositeDisposable.add(addSongTopicDisposable)

        val endSongTopicDisposable = musicRoomStompClient.topic("/topic/room/$roomCode/song/end")
            .subscribe {
                requireActivity().runOnUiThread {
                    adapter.removeFirstSong()
                    println("Song finished")
                }
            }
        compositeDisposable.add(endSongTopicDisposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    companion object {
        private const val ARG_ROOM_CODE = "roomCode"
        private const val ARG_PLAYLIST = "playlist"

        @JvmStatic
        fun newInstance(roomCode: String, playlist: List<Song>) =
            RoomPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ROOM_CODE, roomCode)
                    putParcelableArrayList(ARG_PLAYLIST, ArrayList(playlist))
                }
            }
    }
}