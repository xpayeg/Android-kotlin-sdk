package com.xpay.kotlinutils

import android.content.Context
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xpay.kotlinutils.api.ServiceBuilder
import com.xpay.kotlinutils.api.Xpay
import com.xpay.kotlinutils.models.*
import com.xpay.kotlinutils.models.api.pay.PayData
import com.xpay.kotlinutils.models.api.pay.PayRequestBody
import com.xpay.kotlinutils.models.api.pay.PayResponse
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountData
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountResponse
import com.xpay.kotlinutils.models.api.prepare.PrepareRequestBody
import com.xpay.kotlinutils.models.api.transaction.TransactionData
import com.xpay.kotlinutils.models.api.transaction.TransactionResponse
import okhttp3.ResponseBody
import okio.IOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.util.*
import kotlin.collections.HashMap


object XpayUtils {

    // API required settings
    var apiKey: String? = null
    var communityId: String? = null
    var variableAmountID: Number? = null
    var serverSetting: ServerSetting = ServerSetting.TEST
        set(value) {
            field = value
            request = ServiceBuilder(serverSetting).xpayService(Xpay::class.java)
        }

    // Payment methods data
    var PaymentOptionsTotalAmounts: PaymentOptionsTotalAmounts? = null
        internal set

    var payUsing: PaymentMethods? = null

    // Pay request body
    var activePaymentMethods = mutableListOf<PaymentMethods>()
        internal set

    private val currency: String? = "EGP"
    var customFields = mutableListOf<CustomField>()
//        private set

    // User data
    var userInfo: User? = null
    var ShippingInfo: ShippingInfo? = null

    // private settings
    internal var request = ServiceBuilder(serverSetting).xpayService(Xpay::class.java)


    fun welcomeMessage(context: Context) {
        Toast.makeText(context, "Welcome To XPay Sdk", Toast.LENGTH_LONG).show()
    }

    // Payments related methods

    suspend fun prepareAmount(
        amount: Number
    ): PrepareAmountData? {
        checkAPISettings()
        var preparedData: PrepareAmountData? = null
        val body = PrepareRequestBody(communityId.toString(), amount)
        val res = apiKey?.let { request.prepareAmount(body, it) }
        if (res?.body() != null && res.isSuccessful) {
            preparedData = res.body()!!.data
            preparedData.total_amount.let { activePaymentMethods.add(PaymentMethods.CARD) }
            preparedData.CASH.let { activePaymentMethods.add(PaymentMethods.CASH) }
            preparedData.KIOSK.let { activePaymentMethods.add(PaymentMethods.KIOSK) }
            PaymentOptionsTotalAmounts = PaymentOptionsTotalAmounts(
                preparedData.total_amount,
                preparedData.CASH?.total_amount,
                preparedData.KIOSK?.total_amount
            )
        } else {
            val gson = Gson()
            val type = object : TypeToken<PrepareAmountResponse>() {}.type
            val errorResponse: PrepareAmountResponse? =
                gson.fromJson(res?.errorBody()?.charStream(), type)
            errorResponse?.status?.errors?.get(0)?.let { throwError(it.toString()) }
        }
        return preparedData
    }

    suspend fun pay(): PayData? {
        // check for API settings
        checkAPISettings()
        var preparedData: PayData? = null
        val bodyPay = PayRequestBody()

        variableAmountID?.let { bodyPay.variable_amount_id = it }
        communityId?.let { bodyPay.community_id = it }

        // Payment method
        payUsing?.let {
            if (it in activePaymentMethods) {
                bodyPay.pay_using = it.toString().toLowerCase(Locale.ROOT)
                when (it) {
                    PaymentMethods.CARD -> bodyPay.amount = PaymentOptionsTotalAmounts?.card!!
                    PaymentMethods.CASH -> bodyPay.amount = PaymentOptionsTotalAmounts?.cash!!
                    PaymentMethods.KIOSK -> bodyPay.amount = PaymentOptionsTotalAmounts?.kiosk!!
                }
            } else {
                throwError("Payment method is not available")
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
        currency?.let { bodyPay.currency = it }

        if (payUsing == PaymentMethods.CASH) {
            ShippingInfo?.let {
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
        if (customFields.size > 0) {
            bodyPay.custom_fields = customFields
        }
        val res = apiKey?.let { request.pay(it, bodyPay) }
        if (res?.body() != null && res.isSuccessful) {
            preparedData = res.body()!!.data
        } else {
            val gson = Gson()
            val type = object : TypeToken<PayResponse>() {}.type
            val errorResponse: PayResponse? =
                gson.fromJson(res?.errorBody()?.charStream(), type)
            errorResponse?.status?.errors?.get(0)?.let { throwError(it.toString()) }
        }
        return preparedData
    }

    // Custom Fields related methods

    fun addCustomField(fieldName: String, fieldValue: String) {
        customFields.add(CustomField(fieldName, fieldValue))
    }

    fun clearCustomField() {
        customFields.clear()
    }

    // Transaction info related methods

    suspend fun getTransaction(
        transactionUid: String
    ): TransactionData? {
        checkAPISettings()
        var transactionData: TransactionData? = null

        val res = apiKey?.let {
            this.communityId?.let { it1 ->
                request.getTransaction(
                    it,
                    it1, transactionUid
                )
            }
        }
        if (res?.body() != null && res.isSuccessful) {
            transactionData = res.body()!!.data
        } else {
            val gson = Gson()
            val type = object : TypeToken<TransactionResponse>() {}.type
            val errorResponse: TransactionResponse? =
                gson.fromJson(res?.errorBody()?.charStream(), type)
            errorResponse?.status?.errors?.get(0)?.let { throwError(it.toString()) }
        }
        return transactionData
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