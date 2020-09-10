package com.xpay.kotlinutils.model

import com.google.gson.annotations.SerializedName

data class Transaction(

    @SerializedName("created") val created : String,
    @SerializedName("id") val id : Int,
    @SerializedName("uuid") val uuid : String,
    @SerializedName("total_amount") val total_amount : Double,
    @SerializedName("total_amount_currency") val total_amount_currency : String,
    @SerializedName("status") val status : String
)