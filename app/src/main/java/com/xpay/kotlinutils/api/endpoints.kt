package com.xpay.kotlinutils.api


import com.xpay.kotlinutils.model.PayResponse
import com.xpay.kotlinutils.model.PrepareAmountResponse
import com.xpay.kotlin.models.Transaction
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import kotlin.collections.HashMap

interface Xpay {

    @GET("/api/users/{id}")
    fun userInfo(
        @Header("Authorization") authToken: String,
        @Path("id") id: String
    ): Call<ResponseBody>

    @GET("/api/users/{id}/bills")
    fun loadBills(
        @Header("Authorization") authToken: String,
        @Path("id") id: String
    ): Call<ResponseBody>

    @GET("/api/users/{id}/cards")
    fun loadCards(
        @Header("Authorization") authToken: String,
        @Path("id") id: String
    ): Call<ResponseBody>

    @GET("/api/users/otp/send/")
    fun login(@Body category: Map<String, Any>): Call<ResponseBody>

    @GET("/api/v1/users/{u_id}/transactions/{t_id}")
    fun getTransactionState(
        @Header("Authorization") authToken: String,
        @Path("u_id") id: String,
        @Path("t_id") tId: String
    ): Call<ResponseBody>

    @GET("/api/users/otp/verify/")
    fun verifyOtp(@Body category: Map<String, Any>): Call<ResponseBody>

    @GET("/api/communities/{comm_id}/memberships/{member_id}/bills")
    fun userBills(
        @Header("Authorization") authToken: String,
        @Path("comm_id") id: String,
        @Path("member_id") m_id: String
    ): Call<ResponseBody>

    @POST("/api/v1/payments/prepare-amount/")
    fun prepareAmount(@Body category: HashMap<String, Any>, @Header("x-api-key") authToken: String): Call<PrepareAmountResponse>

    @GET("/api/v1/communities/{community_id}/transactions/{transaction_uuid}/")
    fun getTransaction(
        @Header("x-api-key") authToken: String,
        @Path("community_id") id: String,
        @Path("transaction_uuid") m_id: String
    ): Call<Transaction>

    @POST("/api/v1/payments/pay/variable-amount")
    fun pay(@Header("x-api-key") authToken: String, @Body category: HashMap<String, Any?>): Call<PayResponse>
}