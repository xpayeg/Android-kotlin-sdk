package com.xpay.kotlinutils.models.api.prepare

import com.google.gson.annotations.SerializedName
import com.xpay.kotlinutils.models.api.Status


data class PrepareAmountResponse (
    @SerializedName("status") val status : Status,
    @SerializedName("data") val data : PrepareAmountData,
    @SerializedName("count") val count : String,
    @SerializedName("next") val next : String,
    @SerializedName("previous") val previous : String
)