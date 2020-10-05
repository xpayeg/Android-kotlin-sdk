package com.xpay.kotlinutil

import android.content.Context
import android.widget.Toast
import com.xpay.kotlinutils.api.ServiceBuilder
import com.xpay.kotlin.models.*
import com.xpay.kotlinutils.api.Xpay
import com.xpay.kotlinutils.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.HashMap

object XpayUtils {

    // API required settings
    var apiKey: String? = null
    var communityId: String? = null
    var variableAmountID: Number? = null

    // Payment methods data
    var PaymentOptionsTotalAmounts: PaymentOptionsTotalAmounts? = null
        private set
    var payUsing: PaymentMethods? = null

    // Pay request body
    private var body: RequestBody? = null
    var activePaymentMethods = mutableListOf<PaymentMethods>()
        private set
    private val currency: String? = "EGP"
    var customFields = mutableListOf<CustomField>()
        private set

    // User data
    var userInfo: User? = null
    var shippingInfo: Info? = null


    fun welcomeMessage(context: Context) {
        Toast.makeText(context, "Welcome To XPay Sdk", Toast.LENGTH_LONG).show()
    }

    // Payments related methods

    fun prepareAmount(
        amount: Number,
        onSuccess: (PreparedAmounts) -> Unit,
        onFail: (String) -> Unit
    ) {
        checkAPISettings()

        val body = PrepareBody(communityId.toString(), amount)
        val request = ServiceBuilder.xpayService(Xpay::class.java)
        apiKey?.let { request.prepareAmount(body, it) }
            ?.enqueue(object : Callback<PrepareAmountResponse> {
                override fun onResponse(
                    call: Call<PrepareAmountResponse>,
                    response: Response<PrepareAmountResponse>
                ) {
                    if (response.body() != null && response.isSuccessful) {

                        if (response.body()!!.data != null) {
                            val preparedData = response.body()!!.data
                            onSuccess(preparedData)
                            preparedData.total_amount.let { activePaymentMethods.add(PaymentMethods.CARD) }
                            preparedData.CASH.let { activePaymentMethods.add(PaymentMethods.CASH) }
                            preparedData.KIOSK.let { activePaymentMethods.add(PaymentMethods.KIOSK) }

                            PaymentOptionsTotalAmounts = PaymentOptionsTotalAmounts(
                                preparedData.total_amount,
                                preparedData.CASH.total_amount,
                                preparedData.KIOSK.total_amount
                            )
                        }

                    } else {
                        response.body()?.status?.errors?.get(0)?.let { onFail(it) }
                    }
                }

                override fun onFailure(call: Call<PrepareAmountResponse>, t: Throwable) {
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

        variableAmountID?.let { body= RequestBody(it) }
        communityId?.let { body?.community_id = it }

        // Payment method
        payUsing?.let {
            if (it in activePaymentMethods) {
                body?.pay_using = it
            }

            when (it) {
                PaymentMethods.CARD -> body?.amount = PaymentOptionsTotalAmounts?.card!!
                PaymentMethods.CASH -> body?.amount = PaymentOptionsTotalAmounts?.cash!!
                PaymentMethods.KIOSK -> body?.amount = PaymentOptionsTotalAmounts?.kiosk!!
            }
        } ?: throwError("Payment method is not set")

        // Billing information
        val user: User = userInfo ?: throwError("User information is not set")
        val billingData: HashMap<String, Any> = HashMap()
        billingData["name"] = user.name
        billingData["email"] = user.email
        billingData["phone_number"] = user.phone

        PaymentOptionsTotalAmounts
            ?: throwError("Total amount is not set, call prepareAmount method")
        currency?.let { body?.currency=it }

        if (payUsing == PaymentMethods.CASH) {
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
        body?.billing_data = billingData

        // custom fields
        val customBody: List<CustomField>
        if (customFields.size > 0) {
            body?.custom_fields = customFields
        }

        // making a request
        val request = ServiceBuilder.xpayService(Xpay::class.java)
        apiKey?.let { body?.let { it1 -> request.pay(it, it1) } }
            ?.enqueue(object : Callback<PayResponse> {
                override fun onResponse(call: Call<PayResponse>, response: Response<PayResponse>) {
                    if (response.body()?.data != null && response.isSuccessful) {
                        onSuccess(response.body()!!.data)
                    } else {
                        response.body()?.status?.errors?.get(0)?.let { onFail(it) }
                    }
                }

                override fun onFailure(call: Call<PayResponse>, t: Throwable) {
                    onFail(t.message.toString())
                }
            })
    }

    // Custom Fields related methods

    fun addCustomField(fieldName: String, fieldValue: String) {
        customFields.add(CustomField(fieldName, fieldValue))
    }

    fun clearCustomField() {
        customFields.clear()
    }

    // Transaction info related methods

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

    // Private Methods

    private fun throwError(message: String): Nothing {
        throw IllegalArgumentException(message)
    }

    private fun checkAPISettings() {
        apiKey ?: throwError("API key is not set")
        communityId ?: throwError("Community ID is not set")
        variableAmountID ?: throwError("API Payment ID is not set")
    }
}