package com.master.musicroomclient.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Listener(val name: String, val connectedAt: String) : Parcelable
