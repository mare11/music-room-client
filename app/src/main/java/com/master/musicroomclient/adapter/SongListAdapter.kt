package com.master.musicroomclient.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.R
import com.master.musicroomclient.model.Song
import com.master.musicroomclient.utils.Constants.formatDurationToMinutesAndSeconds

class SongListAdapter(
    private val values: MutableList<Song>
) : RecyclerView.Adapter<SongListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_room_song_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = values[position]
        holder.songNameText.text = song.name
        holder.songDurationText.text = formatDurationToMinutesAndSeconds(song.duration)
        holder.songUploaderText.text = song.uploader
    }

    override fun getItemCount(): Int = values.size

    fun addSong(song: Song) {
        values.add(song)
        notifyItemInserted(values.size - 1)
    }

    fun removeFirstSong() {
        val removedSong = values.removeFirstOrNull()
        if (removedSong != null) {
            notifyItemRemoved(0)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songNameText: TextView = view.findViewById(R.id.song_name)
        val songDurationText: TextView = view.findViewById(R.id.song_duration)
        val songUploaderText: TextView = view.findViewById(R.id.song_uploader)
    }
}