package com.xpay.kotlinutils.models.api.pay

import com.google.gson.annotations.SerializedName
import com.xpay.kotlinutils.models.api.Status

data class PayResponse (

    @SerializedName("status") val status : Status,
    @SerializedName("data") val data : PayData,
    @SerializedName("count") val count : String,
    @SerializedName("next") val next : String,
    @SerializedName("previous") val previous : String
)