package com.master.musicroomclient.utils

import java.time.Duration

object Constants {

    const val SERVER_HOST = "192.168.1.8"
    const val SERVER_PORT = 8008
    const val SERVER_STREAM_PORT = 5555

    const val ROOM_CODE_EXTRA = "roomCode"
    const val ROOM_REQUEST_CODE = 11

    const val USER_ROOMS_PREFERENCE_KEY = "userRooms"
    const val USER_NAME_PREFERENCE_KEY = "userName"

    const val FILE_REQUEST_CODE = 12
    const val DEFAULT_FILE_NAME = "Unknown title"
    const val DEFAULT_FILE_DURATION = 0L

    fun formatDuration(millisecondsDuration: Long): String {
        val duration = Duration.ofMillis(millisecondsDuration)
        val minutes = duration.toMinutes()
        val seconds = duration.minusMinutes(minutes).seconds
        return "$minutes:$seconds"
    }
}