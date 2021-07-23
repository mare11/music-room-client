package com.master.musicroomclient.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.R
import com.master.musicroomclient.adapter.SongListAdapter
import com.master.musicroomclient.adapter.TabAdapter.Companion.TabIndexes.ROOM_PLAYLIST
import com.master.musicroomclient.model.Song
import com.master.musicroomclient.utils.ApiUtils.gson
import com.master.musicroomclient.utils.ApiUtils.musicRoomStompClient
import com.master.musicroomclient.utils.Constants.ARG_PLAYLIST
import com.master.musicroomclient.utils.Constants.ARG_ROOM_CODE
import com.master.musicroomclient.utils.SnackBarUtils.showSnackBar
import com.master.musicroomclient.utils.TabLayoutBadgeListener
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.dto.StompMessage

class RoomPlaylistFragment : Fragment() {

    private lateinit var roomCode: String
    private lateinit var playlist: List<Song>
    private lateinit var adapter: SongListAdapter
    private val compositeDisposable = CompositeDisposable()
    private lateinit var tabListener: TabLayoutBadgeListener

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
            showSnackBar(view, "View could not be loaded")
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = requireActivity()
        if (activity is TabLayoutBadgeListener) {
            tabListener = activity
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::tabListener.isInitialized) {
            tabListener.onTabResume(ROOM_PLAYLIST.ordinal)
        }
    }

    private fun connectToSongTopics() {
        val addSongTopicDisposable = musicRoomStompClient.topic("/topic/room/$roomCode/song/add")
            .subscribe { topicMessage: StompMessage ->
                val newSong = gson.fromJson(topicMessage.payload, Song::class.java)
                requireActivity().runOnUiThread {
                    adapter.addSong(newSong)
                    tabListener.onTabEvent(ROOM_PLAYLIST.ordinal)
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