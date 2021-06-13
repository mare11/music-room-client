package com.master.musicroomclient.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.R
import com.master.musicroomclient.model.Song
import com.master.musicroomclient.utils.Constants.formatDuration

class SongListAdapter(
    private val values: List<Song>
) : RecyclerView.Adapter<SongListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_room_song_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = values[position]
        holder.songNameText.text = song.name
        holder.songDurationText.text = formatDuration(song.duration)
    }

    override fun getItemCount(): Int = values.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songNameText: TextView = view.findViewById(R.id.song_name)
        val songDurationText: TextView = view.findViewById(R.id.song_duration)
    }
}