package com.xpay.kotlinutils.model

import com.google.gson.annotations.SerializedName
import com.xpay.kotlin.models.PayData
import com.xpay.kotlin.models.Status

data class PayResponse (

    @SerializedName("status") val status : Status,
    @SerializedName("data") val data : PayData,
    @SerializedName("count") val count : String,
    @SerializedName("next") val next : String,
    @SerializedName("previous") val previous : String
)