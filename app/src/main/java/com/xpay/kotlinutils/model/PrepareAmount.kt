package com.xpay.kotlin.models

import com.google.gson.annotations.SerializedName


data class PrepareAmount (
    @SerializedName("status") val status : Status,
    @SerializedName("data") val data : PrepareData,
    @SerializedName("count") val count : String,
    @SerializedName("next") val next : String,
    @SerializedName("previous") val previous : String
)