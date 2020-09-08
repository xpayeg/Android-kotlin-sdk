package api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {
    private val client = OkHttpClient.Builder().build()

    private val retrofit1 = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://new-dev.xpay.app/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit1.create(service)
    }

    fun<T> xpayService(service: Class<T>): T{
        return retrofit.create(service)
    }
}