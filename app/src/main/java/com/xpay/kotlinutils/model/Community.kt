package com.xpay.kotlin.models

import com.google.gson.annotations.SerializedName

data class Community (

    @SerializedName("id") val id : String,
    @SerializedName("name") val name : String,
    @SerializedName("logo") val logo : String,
    @SerializedName("app_modules_to_see") val app_modules_to_see : List<String>,
    @SerializedName("payment_methods") val payment_methods : List<String>,
    @SerializedName("address") val address : String,
    @SerializedName("login_process") val login_process : String,
    @SerializedName("cash_collection_fees_amount") val cash_collection_fees_amount : Int,
    @SerializedName("cash_collection_fees_amount_currency") val cash_collection_fees_amount_currency : String,
    @SerializedName("payment_processor") val payment_processor : String,
    @SerializedName("bm_merchant_id") val bm_merchant_id : String
)