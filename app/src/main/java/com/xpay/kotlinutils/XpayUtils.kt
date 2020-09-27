package com.xpay.kotlinutils

import android.content.Context
import android.widget.Toast
import api.ServiceBuilder
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.xpay.kotlin.models.*
import com.xpay.kotlinutils.api.Xpay
import com.xpay.kotlinutils.model.TotalAmount
import com.xpay.kotlinutils.model.CustomField
import com.xpay.kotlinutils.model.Info
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object XpayUtils {

    var apiKey: String? = null
    var variableAmountID: Number? = null
    var iframeUrl: String? = null
    var totalAmount: TotalAmount? = null
        private set
    var communityId: String? = null
    var payUsing: String? = null
    var paymentOptions: ArrayList<String> = ArrayList()
        private set
    var currency: String? = "EGP"
        private set
    var customFields = mutableListOf<CustomField>()
        private set
    var user: User? = null
    var shippingInfo: Info? = null
    var amount: Number? = null
//        private set

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
                            if (res.total_amount != null) {
                                paymentOptions.add("CARD")
                            }
                            if (res.CASH != null) {
                                paymentOptions.add("CASH")
                            }
                            if (res.KIOSK != null) {
                                paymentOptions.add("KIOSK")
                            }
                            totalAmount = TotalAmount(
                                res.total_amount,
                                res.CASH.total_amount,
                                res.KIOSK.total_amount
                            )

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
        val customBody: List<CustomField>

        billingData["name"] = user.name
        billingData["email"] = user.email
        billingData["phone_number"] = user.phone
        requestBody["amount"] = amount!!
        currency?.let { requestBody.put("currency", it) }
        variableAmountID?.let { requestBody.put("variable_amount_id", it) }
        communityId?.let { requestBody.put("community_id", it) }
        payUsing.let {
            if (it != null && it.toUpperCase(Locale.ROOT) in paymentOptions) {
                requestBody["pay_using"] = it
            }
        }
        if (customFields.size > 0) {
            customBody = customFields
            requestBody["custom_fields"] = customBody
        }
        if (payUsing == "cash") {
            if (shippingInfo != null) {
                billingData["country"] = "EG"
                billingData["apartment"] = shippingInfo!!.apartment
                billingData["city"] = shippingInfo!!.city
                billingData["state"] = shippingInfo!!.state
                billingData["country"] = shippingInfo!!.country
                billingData["floor"] = shippingInfo!!.floor
                billingData["street"] = shippingInfo!!.street
                billingData["building"] = shippingInfo!!.building
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

    fun addCustomField(fieldName: String, fieldValue: String) {
        customFields.add(CustomField(fieldName, fieldValue))
    }

    fun clearCustomField() {
        customFields.clear()
    }


}