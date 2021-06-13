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

class RoomPlaylistFragment : Fragment() {
    private lateinit var playlist: List<Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            bundle.getParcelableArrayList<Song>(ARG_PLAYLIST)?.also { playlist = it }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_room_playlist, container, false)

        if (view is RecyclerView && this::playlist.isInitialized) {
            with(view) {
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = SongListAdapter(playlist)
            }
        }

        return view
    }

    companion object {
        private const val ARG_PLAYLIST = "playlist"

        @JvmStatic
        fun newInstance(playlist: List<Song>) =
            RoomPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PLAYLIST, ArrayList(playlist))
                }
            }
    }
}