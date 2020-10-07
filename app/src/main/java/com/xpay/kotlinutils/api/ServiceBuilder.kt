package com.xpay.kotlinutils.api

import com.xpay.kotlinutils.models.ServerSetting
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ServiceBuilder(serverSetting: ServerSetting = ServerSetting.TEST) {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://${if (serverSetting == ServerSetting.LIVE) "community" else "new-dev"}.xpay.app/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun <T> xpayService(service: Class<T>): T {
        return retrofit.create(service)
    }
}