package com.xpay.kotlinutils

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
import java.util.*
import kotlin.collections.HashMap


object XpayUtils {

    // API required settings
    var apiKey: String? = null
    var communityId: String? = null
    var apiPaymentId: Number? = null
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

    var customFields = mutableListOf<CustomField>()

    // User data
    var billingInfo: BillingInfo? = null
    var ShippingInfo: ShippingInfo? = null

    // private/internal settings
    internal var request = ServiceBuilder(serverSetting).xpayService(Xpay::class.java)
    private val currency: String? = "EGP"

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
            preparedData.total_amount?.let { activePaymentMethods.add(PaymentMethods.CARD) }
            preparedData.CASH?.let { activePaymentMethods.add(PaymentMethods.CASH) }
            preparedData.KIOSK?.let { activePaymentMethods.add(PaymentMethods.KIOSK) }
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
        checkNotNull(PaymentOptionsTotalAmounts) { "PaymentOptionsTotalAmounts is not set" }
        check(activePaymentMethods.isNotEmpty()) { "activePaymentMethods is empty" }
        checkNotNull(payUsing) { "Payment method is not set" }
        checkNotNull(billingInfo) { "Billing information is not found" }

        var preparedData: PayData? = null
        val bodyPay = PayRequestBody()

        apiPaymentId?.let { bodyPay.variable_amount_id = it }
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
                throwError("Payment method chosen is not available")
            }
        }

        // Billing information
        val billingInfo: BillingInfo = billingInfo!!
        val billingData: HashMap<String, Any> = HashMap()
        billingData["name"] = billingInfo.name
        billingData["email"] = billingInfo.email
        billingData["phone_number"] = billingInfo.phone

        currency?.let { bodyPay.currency = it }

        if (payUsing == PaymentMethods.CASH) {
            ShippingInfo?.let {
                billingData["country"] = "EG"
                billingData["apartment"] = it.apartment
                billingData["city"] = it.city
                billingData["state"] = it.state
                billingData["floor"] = it.floor
                billingData["street"] = it.street
                billingData["building"] = it.building
            } ?: throw IllegalStateException("Shipping Information is not found")
        }
        bodyPay.billing_data = billingData

        // custom fields
        if (customFields.size > 0) {
            bodyPay.custom_fields = customFields
        }
        val res = apiKey?.let { request.pay(it, bodyPay) }
        if (res?.body() != null && res.isSuccessful) {
            preparedData = res.body()!!.data
            clearCustomFields()
            PaymentOptionsTotalAmounts = null
            activePaymentMethods.clear()
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

    fun clearCustomFields() {
        customFields.clear()
    }

    // Transaction info related methods

    suspend fun getTransaction(
        transactionUid: String
    ): TransactionData? {
        checkAPISettings()
        var response: TransactionData? = null

        val res = apiKey?.let {
            this.communityId?.let { it1 ->
                request.getTransaction(
                    it,
                    it1, transactionUid
                )
            }
        }
        if (res?.body() != null && res.isSuccessful) {
            response = res.body()!!.data
        } else {
            val gson = Gson()
            val type = object : TypeToken<TransactionResponse>() {}.type
            val errorResponse: TransactionResponse? =
                gson.fromJson(res?.errorBody()?.charStream(), type)
            errorResponse?.status?.errors?.get(0)?.let { throwError(it.toString()) }
        }
        return response
    }

    // Private Methods

    private fun throwError(message: String): Nothing {
        throw IllegalArgumentException(message)
    }

    private fun checkAPISettings() {
        checkNotNull(apiKey) { "apiKey is required" }
        checkNotNull(communityId) { "communityId is required" }
        checkNotNull(apiPaymentId) { "variableAmountID is required" }
    }

}