package com.master.musicroomclient.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RoomDetails(
    val id: Long,
    val name: String,
    val code: String,
    val listeners: List<Listener>,
    val playlist: List<Song>,
    val currentSong: CurrentSong?
) : Parcelable
