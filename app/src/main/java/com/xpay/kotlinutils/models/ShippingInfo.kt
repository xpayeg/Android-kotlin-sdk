package com.xpay.kotlinutils.models

data class ShippingInfo(
    var country: String,
    var city: String,
    var state: String,
    var apartment: String,
    var building: String,
    var floor: String,
    var street: String
)
