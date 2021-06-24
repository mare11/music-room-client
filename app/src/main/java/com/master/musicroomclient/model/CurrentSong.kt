package com.master.musicroomclient.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CurrentSong(val song: Song, val elapsedDuration: Long) : Parcelable
