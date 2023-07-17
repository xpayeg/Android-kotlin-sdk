package com.xpay.kotlinutils.models.api.prepare

import com.google.gson.annotations.SerializedName

data class PrepareAmountData (
    @SerializedName("total_amount") val total_amount : Double?,
    @SerializedName("total_amount_currency") val total_amount_currency : String?,
    @SerializedName("CASH") val CASH : TotalAmount?,
    @SerializedName("KIOSK") val KIOSK : TotalAmount?,
    @SerializedName("MEEZA") val MEEZA : TotalAmount?,
    @SerializedName("FAWRY") val FAWRY : TotalAmount?,
    @SerializedName("VALU") val VALU : TotalAmount?
)