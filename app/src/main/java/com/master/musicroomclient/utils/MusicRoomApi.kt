package com.master.musicroomclient.utils

import com.master.musicroomclient.model.Room
import com.master.musicroomclient.model.RoomDetails
import com.master.musicroomclient.model.RoomRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface MusicRoomApi {

    @GET("{code}")
    fun getRoomByCode(@Path("code") code: String): Call<Room>

    @GET(".")
    fun getRoomsByCodes(@Query("codes") codes: List<String>): Call<List<Room>>

    @POST(".")
    fun createRoom(@Body roomRequest: RoomRequest): Call<Room>

    @PUT("{code}/connect")
    fun connectListener(
        @Path("code") code: String,
        @Query("listener") listener: String
    ): Call<RoomDetails>

    @PUT("{code}/disconnect")
    fun disconnectListener(
        @Path("code") code: String,
        @Query("listener") listener: String
    ): Call<Void>

    @Multipart
    @POST("{code}/upload")
    fun uploadSong(
        @Path("code") code: String,
        @Part file: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part("uploader") uploader: RequestBody
    ): Call<RoomDetails>
}