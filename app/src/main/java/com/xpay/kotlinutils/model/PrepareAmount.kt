package com.xpay.kotlinutils.model

import com.google.gson.annotations.SerializedName
import com.xpay.kotlin.models.PrepareData
import com.xpay.kotlin.models.Status


data class PrepareAmount (
    @SerializedName("status") val status : Status,
    @SerializedName("data") val data : PrepareData,
    @SerializedName("count") val count : String,
    @SerializedName("next") val next : String,
    @SerializedName("previous") val previous : String
)