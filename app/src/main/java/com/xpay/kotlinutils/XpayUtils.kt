package com.xpay.kotlinutils

import android.content.Context
import android.widget.Toast
import com.xpay.kotlinutils.api.ServiceBuilder
import com.xpay.kotlin.models.*
import com.xpay.kotlinutils.api.Xpay
import com.xpay.kotlinutils.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object XpayUtils {

    // API required settings
    var apiKey: String? = null
    var communityId: String? = null
    var variableAmountID: Number? = null

    var iframeUrl: String? = null
    var totalAmount: TotalAmount? = null
        private set
    var payUsing: String? = null
    var paymentOptions: ArrayList<String> = ArrayList()
        private set
    private val currency: String? = "EGP"
    var customFields = mutableListOf<CustomField>()
        private set
    var userInfo: User? = null
    var shippingInfo: Info? = null
    var amount: Number? = null

    private fun throwError(message: String): Nothing {
        throw IllegalArgumentException(message)
    }

    fun welcomeMessage(context: Context) {
        Toast.makeText(context, "Welcome To XPay Sdk", Toast.LENGTH_LONG).show()
    }


    fun prepareAmount(
        amount: Number,
        onSuccess: (PrepareData) -> Unit,
        onFail: (String) -> Unit
    ) {
        checkAPISettings()

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["community_id"] = communityId.toString()
        hashMap["amount"] = amount

        val request = ServiceBuilder.xpayService(Xpay::class.java)

        apiKey?.let { request.prepareAmount(hashMap, it) }
            ?.enqueue(object : Callback<PrepareAmount> {
                override fun onResponse(
                    call: Call<PrepareAmount>,
                    response: Response<PrepareAmount>
                ) {
                    if (response.body() != null && response.isSuccessful) {

                        if (response.body()!!.data != null) {
                            val preparedData = response.body()!!.data
                            onSuccess(preparedData)
                            preparedData.total_amount.let { paymentOptions.add("CARD") }
                            preparedData.CASH.let { paymentOptions.add("CASH") }
                            preparedData.KIOSK.let { paymentOptions.add("KIOSK") }

                            totalAmount = TotalAmount(
                                preparedData.total_amount,
                                preparedData.CASH.total_amount,
                                preparedData.KIOSK.total_amount
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
        onSuccess: (PayData) -> Unit,
        onFail: (String) -> Unit
    ) {
        // check for API settings
        checkAPISettings()

        val requestBody: HashMap<String, Any> = HashMap()
        variableAmountID?.let { requestBody.put("variable_amount_id", it) }
        communityId?.let { requestBody.put("community_id", it) }

        payUsing?.let {
            if (it.toUpperCase(Locale.ROOT) in paymentOptions ?: throwError("payment options is not set")) {
                requestBody["pay_using"] = it
            }
        }

        val user: User = userInfo ?: throwError("User information is not set")
        val billingData: HashMap<String, Any> = HashMap()
        billingData["name"] = user.name
        billingData["email"] = user.email
        billingData["phone_number"] = user.phone

        val customBody: List<CustomField>
        if (customFields.size > 0) {
            customBody = customFields
            requestBody["custom_fields"] = customBody
        }

        requestBody["amount"] =
            totalAmount ?: throwError("Total amount is not set, call prepareAmount method")
        currency?.let { requestBody.put("currency", it) }

        if (payUsing == "cash") {
            shippingInfo?.let {
                billingData["country"] = "EG"
                billingData["apartment"] = it.apartment
                billingData["city"] = it.city
                billingData["state"] = it.state
                billingData["country"] = it.country
                billingData["floor"] = it.floor
                billingData["street"] = it.street
                billingData["building"] = it.building
            }
        }
        requestBody["billing_data"] = billingData

        val request = ServiceBuilder.xpayService(Xpay::class.java)
        apiKey?.let { request.pay(it, requestBody) }?.enqueue(object : Callback<PayResponse> {
            override fun onResponse(call: Call<PayResponse>, response: Response<PayResponse>) {
                if (response.body()?.data != null && response.isSuccessful) {
                    onSuccess(response.body()!!.data)
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

    private fun checkAPISettings() {
        apiKey ?: throwError("API key is not set")
        communityId ?: throwError("Community ID is not set")
        variableAmountID ?: throwError("API Payment ID is not set")
    }
}