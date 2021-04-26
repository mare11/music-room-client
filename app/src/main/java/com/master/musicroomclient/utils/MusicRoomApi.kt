package com.master.musicroomclient.utils

import com.master.musicroomclient.model.Room
import retrofit2.Call
import retrofit2.http.*

interface MusicRoomApi {

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type:application/json"
    )
    @GET("{code}")
    fun getRoomByCode(@Path("code") code: String): Call<Room>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type:application/json"
    )
    @POST
    fun createRoom(@Body room: Room): Call<Room>
}