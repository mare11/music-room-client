package com.master.musicroomclient.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.master.musicroomclient.utils.Constants.SERVER_HOST
import com.master.musicroomclient.utils.Constants.SERVER_PORT
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

object ApiUtils {
    private const val HTTP_BASE_URL = "http://$SERVER_HOST:$SERVER_PORT/api/rooms/"
    private const val WS_BASE_URL = "ws://$SERVER_HOST:$SERVER_PORT/music-rooms"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(HTTP_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(initializeOkHttpClient())
        .build()

    val musicRoomApi: MusicRoomApi by lazy {
        retrofit.create(MusicRoomApi::class.java)
    }

    private fun initializeOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }


    val stompClientDelegate = lazy {
        val stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_BASE_URL)
        stompClient.connect()
        stompClient
    }

    val musicRoomStompClient: StompClient by stompClientDelegate

    val gson: Gson by lazy { GsonBuilder().create() }

}