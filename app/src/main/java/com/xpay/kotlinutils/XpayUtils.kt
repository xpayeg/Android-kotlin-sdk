package com.xpay.kotlinutils

import android.content.Context
import android.widget.Toast
import api.ServiceBuilder
import com.xpay.kotlin.models.*
import com.xpay.kotlinutils.api.Xpay
import com.xpay.kotlinutils.model.TotalAmount
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object XpayUtils {

    var apiKey: String? = null
    var variableAmountID: Number? = null
    var iframeUrl: String? = null
    var totalAmount: TotalAmount? = null
        private set
    var communityId: String? = null
    var payUsing: String? = null
        private set
    var currency: String? = "EGP"
        private set
    var user: User? = null
    var amount: Number? = null
        private set

    fun welcomeMessage(context: Context) {
        Toast.makeText(context, "Welcome To Xpay Sdk", Toast.LENGTH_LONG).show()
    }


    fun prepareAmount(
        amount: Number,
        onSuccess: (PrepareAmount) -> Unit,
        onFail: (String) -> Unit
    ) {
        val hashMap: HashMap<String, Any> = HashMap<String, Any>()
        hashMap["amount"] = amount
        hashMap["community_id"] = communityId.toString()
        val request = ServiceBuilder.xpayService(Xpay::class.java)
        apiKey?.let { request.prepareAmount(hashMap, it) }
            ?.enqueue(object : Callback<PrepareAmount> {
                override fun onResponse(
                    call: Call<PrepareAmount>,
                    response: Response<PrepareAmount>
                ) {
                    if (response.body() != null && response.isSuccessful && response.code() != 404) {
                        onSuccess(response.body()!!)

                        if (response.body()!!.data != null) {
                            val res = response.body()!!.data
                            totalAmount = TotalAmount(
                                res.total_amount,
                                res.cASH.total_amount,
                                res.kIOSK.total_amount
                            )
                            payUsing = "CARD"
                        }

                    } else {
                        response.body()?.status?.errors?.get(0)?.let { onFail(it) }
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
                if (response.body() != null && response.isSuccessful && response.code() != 404) {
                    onSuccess(response.body()!!)
                } else {
                    response.body()?.status?.message?.let { onFail(it) }
                }
            }

            override fun onFailure(call: Call<Transaction>, t: Throwable) {
                onFail(t.message.toString())
            }
        })
    }

    fun pay(
        onSuccess: (PayResponse) -> Unit,
        onFail: (String) -> Unit
    ) {
        val user: User = user!!
        val billingData: HashMap<String, Any> = HashMap()
        val requestBody: HashMap<String, Any> = HashMap()
        
        when (payUsing) {
            "CARD" -> totalAmount?.card
            "CASH" -> totalAmount?.cash
            "KIOSK" -> totalAmount?.kiosk
        }
        billingData["name"] = user.name
        billingData["email"] = user.email
        billingData["phone_number"] = user.phone
        requestBody["amount"] = amount!!
        currency?.let { requestBody.put("currency", it) }
        variableAmountID?.let { requestBody.put("variable_amount_id", it) }
        communityId?.let { requestBody.put("community_id", it) }
        payUsing.let {
            if (it != null) {
                requestBody["pay_using"] = it
            }
        }
        requestBody["billing_data"] = billingData

        val request = ServiceBuilder.xpayService(Xpay::class.java)
        apiKey?.let { request.pay(it, requestBody) }?.enqueue(object : Callback<PayResponse> {
            override fun onResponse(call: Call<PayResponse>, response: Response<PayResponse>) {
                if (response.body() != null && response.isSuccessful && response.code() != 404) {
                    onSuccess(response.body()!!)
                } else {
                    response.body()?.status?.message?.let { onFail(it) }
                }
            }

            override fun onFailure(call: Call<PayResponse>, t: Throwable) {
                onFail(t.message.toString())
            }
        })
    }


}