package com.master.musicroomclient.utils

import com.master.musicroomclient.model.Listener
import com.master.musicroomclient.model.Room
import retrofit2.Call
import retrofit2.http.*

interface MusicRoomApi {

    @GET("{code}")
    fun getRoomByCode(@Path("code") code: String): Call<Room>

    @POST(value = ".")
    fun createRoom(@Body room: Room): Call<Room>

    @PUT(value = "{code}/connect")
    fun connectListener(@Path("code") code: String, @Body listener: Listener): Call<Room>

    @PUT(value = "{code}/disconnect")
    fun disconnectListener(@Path("code") code: String, @Body listener: Listener): Call<Room>

}