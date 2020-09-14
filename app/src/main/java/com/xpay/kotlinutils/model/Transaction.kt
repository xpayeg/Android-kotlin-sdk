package com.xpay.kotlin.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class Transaction (

    @SerializedName("status") val status : Status,
    @SerializedName("data") val data : TransactionData,
    @SerializedName("count") val count : String,
    @SerializedName("next") val next : String,
    @SerializedName("previous") val previous : String
)