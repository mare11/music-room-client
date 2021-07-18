package com.master.musicroomclient.utils

import java.time.Duration

object Constants {

    const val SERVER_HOST = "192.168.1.5"
    const val SERVER_PORT = 8008
    const val SERVER_STREAM_PORT = 5555

    const val ROOM_EXTRA = "room"
    const val USER_NAME_EXTRA = "userName"
    const val ROOM_REQUEST_CODE = 11

    const val USER_ROOMS_PREFERENCE_KEY = "userRooms"
    const val USER_NAME_PREFERENCE_KEY = "userName"

    const val FILE_REQUEST_CODE = 12
    const val DEFAULT_FILE_NAME = "Unknown title"
    const val DEFAULT_FILE_DURATION = 0L

    const val ROOM_CODE = "ROOM_CODE"

    private const val SECONDS_DIGITS = 2

    fun formatDurationToMinutesAndSeconds(millisecondsDuration: Long): String {
        val duration = Duration.ofMillis(millisecondsDuration)
        val minutes = duration.toMinutes()
        val seconds = ("0" + duration.minusMinutes(minutes).seconds).takeLast(SECONDS_DIGITS)
        return "$minutes:$seconds"
    }

    fun formatDurationToHoursAndMinutes(millisecondsDuration: Long): String {
        val duration = Duration.ofMillis(millisecondsDuration)
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()
        return if (hours == 0L && minutes == 0L) {
            "< min"
        } else if (hours == 0L) {
            "$minutes min"
        } else {
            "$hours h $minutes min"
        }
    }
}