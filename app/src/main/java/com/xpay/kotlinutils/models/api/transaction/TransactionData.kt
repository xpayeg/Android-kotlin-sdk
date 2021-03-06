package com.xpay.kotlinutils.models.api.transaction

import com.google.gson.annotations.SerializedName
import com.xpay.kotlinutils.models.CustomField

data class TransactionData (
    @SerializedName("created") val created : String,
    @SerializedName("id") val id : Int,
    @SerializedName("uuid") val uuid : String,
    @SerializedName("member_id") val member_id : String?,
    @SerializedName("total_amount") val total_amount : Double,
    @SerializedName("total_amount_currency") val total_amount_currency : String,
    @SerializedName("payment_for") val payment_for : String,
    @SerializedName("quantity") val quantity : String?,
    @SerializedName("status") val status : String,
    @SerializedName("custom_fields_json") val custom_fields_json : Array<CustomField>?,
    @SerializedName("total_amount_piasters") val total_amount_piasters : Int
)