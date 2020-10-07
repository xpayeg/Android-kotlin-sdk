package com.xpay.kotlinutils.api


import com.xpay.kotlinutils.model.PayResponse
import com.xpay.kotlinutils.model.PrepareAmountResponse
import com.xpay.kotlin.models.Transaction
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import kotlin.collections.HashMap

interface Xpay {

    @POST("v1/payments/prepare-amount/")
    fun prepareAmount(@Body category: HashMap<String, Any>, @Header("x-api-key") authToken: String): Call<PrepareAmountResponse>

    @GET("v1/communities/{community_id}/transactions/{transaction_uuid}/")
    fun getTransaction(
        @Header("x-api-key") authToken: String,
        @Path("community_id") id: String,
        @Path("transaction_uuid") m_id: String
    ): Call<Transaction>

    @POST("v1/payments/pay/variable-amount")
    fun pay(@Header("x-api-key") authToken: String, @Body category: HashMap<String, Any?>): Call<PayResponse>
}