package com.master.musicroomclient.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Room(val code: String, val name: String, val numberOfListeners: Int) : Parcelable