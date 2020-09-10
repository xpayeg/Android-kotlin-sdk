package com.xpay.kotlinutils.model

class Request {
    lateinit var billInfo: BillingInfo
    var amount: Float = 0.0F
    var currency: String = "EGP"
    var variable_amount_id: Int = 0
    var communityId: String = "0"
    var pay_using: String = "card"
    var membership_id: String = ""
}