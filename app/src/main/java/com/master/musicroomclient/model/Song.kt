package com.master.musicroomclient.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(val name: String, val duration: Long) : Parcelable
