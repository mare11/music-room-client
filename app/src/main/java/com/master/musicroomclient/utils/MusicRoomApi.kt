package com.master.musicroomclient.utils

import com.master.musicroomclient.model.Room
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MusicRoomApi {

    @GET("{code}")
    fun getRoomByCode(@Path("code") code: String): Call<Room>

    @POST(value = ".")
    fun createRoom(@Body room: Room): Call<Room>
}