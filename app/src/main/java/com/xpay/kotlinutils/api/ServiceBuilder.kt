package com.xpay.kotlinutils.api

import com.xpay.kotlinutils.models.ServerSetting
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class ServiceBuilder(
    serverSetting: ServerSetting = ServerSetting.TEST,
    private val Test: Boolean = false
) {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(if (!Test) "https://${if (serverSetting == ServerSetting.LIVE) "community" else "staging"}.xpay.app/api/" else "http://127.0.0.1:8080")
//        .baseUrl("http://127.0.0.1:8080")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun <T> xpayService(service: Class<T>): T {
        return retrofit.create(service)
    }
}

