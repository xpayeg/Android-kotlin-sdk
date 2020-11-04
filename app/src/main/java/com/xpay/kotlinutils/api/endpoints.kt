package com.xpay.kotlinutils.api


import com.xpay.kotlinutils.models.api.pay.PayResponse
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountResponse
import com.xpay.kotlinutils.models.api.transaction.TransactionResponse
import com.xpay.kotlinutils.models.api.prepare.PrepareRequestBody
import com.xpay.kotlinutils.models.api.pay.PayRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface Xpay {
    @POST("v1/payments/prepare-amount/")
    suspend fun prepareAmount(@Body category: PrepareRequestBody, @Header("x-api-key") authToken: String): Response<PrepareAmountResponse>

    @GET("v1/communities/{community_id}/transactions/{transaction_uuid}/")
    suspend fun getTransaction(
        @Header("x-api-key") authToken: String,
        @Path("community_id") id: String,
        @Path("transaction_uuid") m_id: String
    ): Response<TransactionResponse>

    @POST("v1/payments/pay/variable-amount")
    suspend fun pay(@Header("x-api-key") authToken: String, @Body category: PayRequestBody): Response<PayResponse>
}
