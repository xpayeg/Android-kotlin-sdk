package com.xpay.kotlinutils.models

data class PaymentOptionsTotalAmounts(
    val card: Number? = null,
    val cash: Number? = null,
    val kiosk: Number? = null,
    val meeza: Number? = null,
    val fawry: Number? = null,
    val valu: Number? = null
)

