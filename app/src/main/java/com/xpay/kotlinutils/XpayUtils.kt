package com.xpay.kotlinutils

import android.content.Context
import android.widget.Toast
import com.xpay.kotlinutils.api.ServiceBuilder
import com.xpay.kotlinutils.api.Xpay
import com.xpay.kotlinutils.models.*
import com.xpay.kotlinutils.models.api.pay.PayData
import com.xpay.kotlinutils.models.api.pay.PayRequestBody
import com.xpay.kotlinutils.models.api.pay.PayResponse
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountResponse
import com.xpay.kotlinutils.models.api.prepare.PrepareRequestBody
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountData
import com.xpay.kotlinutils.models.api.transaction.TransactionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
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
    var activePaymentMethods = mutableListOf<PaymentMethods>()
        private set
    private val currency: String? = "EGP"
    var customFields = mutableListOf<CustomField>()
        private set

    // User data
    var userInfo: User? = null
    var shippingShippingInfo: ShippingInfo? = null


    fun welcomeMessage(context: Context) {
        Toast.makeText(context, "Welcome To XPay Sdk", Toast.LENGTH_LONG).show()
    }

    // Payments related methods

    fun prepareAmount(
        amount: Number,
        onSuccess: (PrepareAmountData) -> Unit,
        onFail: (String) -> Unit
    ) {
        checkAPISettings()

        val body = PrepareRequestBody(communityId.toString(), amount)
        val request = ServiceBuilder.xpayService(Xpay::class.java)
        apiKey?.let { request.prepareAmount(body, it) }
            ?.enqueue(object : Callback<PrepareAmountResponse> {
                override fun onResponse(
                    call: Call<PrepareAmountResponse>,
                    response: Response<PrepareAmountResponse>
                ) {
                    if (response.body() != null && response.isSuccessful) {
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

        val bodyPay = PayRequestBody()

        variableAmountID?.let { bodyPay.variable_amount_id = it }
        communityId?.let { bodyPay.community_id = it }

        // Payment method
        payUsing?.let {
            if (it in activePaymentMethods) {
                bodyPay.pay_using = it.toString().toLowerCase(Locale.ROOT)
            }

            when (it) {
                PaymentMethods.CARD -> bodyPay.amount = PaymentOptionsTotalAmounts?.card!!
                PaymentMethods.CASH -> bodyPay.amount = PaymentOptionsTotalAmounts?.cash!!
                PaymentMethods.KIOSK -> bodyPay.amount = PaymentOptionsTotalAmounts?.kiosk!!
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
        currency?.let { bodyPay.currency =it }

        if (payUsing == PaymentMethods.CASH) {
            shippingShippingInfo?.let {
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
        bodyPay.billing_data = billingData

        // custom fields
        val customBody: List<CustomField>
        if (customFields.size > 0) {
            bodyPay.custom_fields = customFields
        }

        // making a request
        val request = ServiceBuilder.xpayService(Xpay::class.java)
        apiKey?.let { bodyPay.let { it1 -> request.pay(it, it1) } }
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
        onSuccess: (TransactionResponse) -> Unit,
        onFail: (String) -> Unit
    ) {
        val request = ServiceBuilder.xpayService(Xpay::class.java)
        val call = request.getTransaction(token, communityID, transactionUid)
        call.enqueue(object : Callback<TransactionResponse> {
            override fun onResponse(call: Call<TransactionResponse>, response: Response<TransactionResponse>) {
                if (response.body() != null && response.isSuccessful && response.code() != 404) {
                    onSuccess(response.body()!!)
                } else {
                    response.body()?.status?.errors?.get(0)?.let { onFail(it) }
                }
            }

            override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
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