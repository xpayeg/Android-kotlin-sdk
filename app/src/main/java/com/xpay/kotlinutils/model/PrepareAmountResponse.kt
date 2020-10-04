package com.xpay.kotlinutils.model

import com.google.gson.annotations.SerializedName
import com.xpay.kotlin.models.Status


data class PrepareAmountResponse (
    @SerializedName("status") val status : Status,
    @SerializedName("data") val data : PreparedAmounts,
    @SerializedName("count") val count : String,
    @SerializedName("next") val next : String,
    @SerializedName("previous") val previous : String
)