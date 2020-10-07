package com.xpay.kotlinutils.models.api

import com.google.gson.annotations.SerializedName

data class Status (
    @SerializedName("code") val code : Int,
    @SerializedName("message") val message : String,
    @SerializedName("errors") val errors : List<String>
)
