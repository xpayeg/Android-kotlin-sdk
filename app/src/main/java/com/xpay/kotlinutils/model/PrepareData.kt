package com.xpay.kotlin.models

import com.google.gson.annotations.SerializedName

data class PrepareData (
    @SerializedName("total_amount") val total_amount : Double,
    @SerializedName("total_amount_currency") val total_amount_currency : String,
    @SerializedName("CASH") val CASH : CASH,
    @SerializedName("KIOSK") val KIOSK : KIOSK
)