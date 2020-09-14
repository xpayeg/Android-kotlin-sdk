package com.xpay.kotlin.models

import com.google.gson.annotations.SerializedName

data class PayData (

    @SerializedName("iframe_url") val iframe_url : String,
    @SerializedName("transaction_id") val transaction_id : Int,
    @SerializedName("transaction_status") val transaction_status : String,
    @SerializedName("transaction_uuid") val transaction_uuid : String,
    @SerializedName("message") val message : String,
    @SerializedName("bill_reference") val bill_reference : String,
    @SerializedName("bm_session") val bm_session : String
)