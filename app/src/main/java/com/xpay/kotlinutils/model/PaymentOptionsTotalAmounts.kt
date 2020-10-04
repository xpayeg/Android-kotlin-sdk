package com.xpay.kotlinutils.model

data class PaymentOptionsTotalAmounts(
    val card: Number? = null,
    val cash: Number? = null,
    val kiosk: Number? = null
)

