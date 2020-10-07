package com.xpay.kotlinutils.models.api.transaction

import com.google.gson.annotations.SerializedName
import com.xpay.kotlinutils.models.api.Status

data class TransactionResponse (

    @SerializedName("status") val status : Status,
    @SerializedName("data") val data : TransactionData,
    @SerializedName("count") val count : String,
    @SerializedName("next") val next : String,
    @SerializedName("previous") val previous : String
)