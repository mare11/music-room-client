package com.master.musicroomclient.utils

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors

object ApiUtils {

    private const val BASE_URL = "http://${Constants.SERVER_HOST}:8008/api/rooms/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(initializeOkHttpClient())
        .callbackExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))
        .build()

    private fun initializeOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    val musicRoomApi: MusicRoomApi by lazy {
        retrofit.create(MusicRoomApi::class.java)
    }

}