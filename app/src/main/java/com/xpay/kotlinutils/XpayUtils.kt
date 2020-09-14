package com.xpay.kotlinutils

import android.content.Context
import android.widget.Toast
import api.ServiceBuilder
import com.xpay.kotlin.models.*
import com.xpay.kotlinutils.api.Xpay
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object XpayUtils {

    var apiKey: String? = null
    var variableAmountID: Number? = null
    var communityId: String? = null
    var payUsing: String? = "card"
    var amount: Number? = null
    var currency: String? = "EGP"
    var user: User? = null

    fun welcomeMessage(context: Context) {
        Toast.makeText(context, "Welcome To Xpay Sdk", Toast.LENGTH_LONG).show()
    }


    fun prepareAmount(
        amount: Number,
        communityID: String,
        onSuccess: (PrepareAmount) -> Unit,
        onFail: (String) -> Unit
    ) {
        val hashMap: HashMap<String, Any> = HashMap<String, Any>()
        hashMap.put("amount", amount)
        hashMap.put("community_id", communityID)
        val request = ServiceBuilder.xpayService(Xpay::class.java)
        val call = request.prepareAmount(hashMap, "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss")
        call.enqueue(object : Callback<PrepareAmount> {
            override fun onResponse(call: Call<PrepareAmount>, response: Response<PrepareAmount>) {
                if (response.body() != null && response.isSuccessful) {
                    onSuccess(response.body()!!)
                }
            }

            override fun onFailure(call: Call<PrepareAmount>, t: Throwable) {
                onFail(t.message.toString())
            }
        })
    }

    fun getTransaction(
        token: String,
        communityID: String,
        transactionUid: String,
        onSuccess: (Transaction) -> Unit,
        onFail: (String) -> Unit
    ) {
        val request = ServiceBuilder.xpayService(Xpay::class.java)
        val call = request.getTransaction(token, communityID, transactionUid)
        call.enqueue(object : Callback<Transaction> {
            override fun onResponse(call: Call<Transaction>, response: Response<Transaction>) {
                if (response.body() != null && response.isSuccessful) {
                    onSuccess(response.body()!!)
                }
            }

            override fun onFailure(call: Call<Transaction>, t: Throwable) {
                onFail(t.message.toString())
            }
        })
    }

    fun payBill(
        maount: Number,
        token: String,
        onSuccess: (PayResponse) -> Unit,
        onFail: (String) -> Unit
    ) {
        val user:User= XpayUtils.user!!
        val billingData: HashMap<String, Any> = HashMap<String, Any>()
        val requestBody: HashMap<String, Any> = HashMap<String, Any>()

        billingData.put("name", user.name)
        billingData.put("email", user.email)
        billingData.put("phone_number", user.phone)
        amount?.let { requestBody.put("amount", it) }
        XpayUtils.currency?.let { requestBody.put("currency", it) }
        XpayUtils.variableAmountID?.let { requestBody.put("variable_amount_id", it) }
        XpayUtils.communityId?.let { requestBody.put("community_id", it) }
        XpayUtils.payUsing?.let { requestBody.put("pay_using", it) }
        requestBody.put("billing_data", billingData)

        val request = ServiceBuilder.xpayService(Xpay::class.java)
        val call = request.payBill(token, requestBody)
        call.enqueue(object : Callback<PayResponse> {
            override fun onResponse(call: Call<PayResponse>, response: Response<PayResponse>) {
                if (response.body() != null && response.isSuccessful) {
                    onSuccess(response.body()!!)
                }
            }

            override fun onFailure(call: Call<PayResponse>, t: Throwable) {
                onFail(t.message.toString())
            }
        })
    }


}